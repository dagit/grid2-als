package com.galois.grid2.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.ActionExecutionException;
import org.springframework.webflow.execution.RequestContext;

import com.galois.grid2.LabeledField;
import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;
import com.galois.grid2.RemoteNameAlreadyMappedException;
import com.galois.grid2.UnmappedRemoteNameException;
import com.galois.grid2.actions.AuthenticationStore.AuthenticationHandle;

/**
 * Controller code that handles storing successful authentication with
 * credentials that do not have a local account mapped.
 */
public final class MapUnmappedRemoteNamesAction {

	public static final String UNMAPPED_REMOTE_NAMES = "com.galois.grid2.unmappedRemoteNames";

	private static final Logger log = LoggerFactory
			.getLogger(MapUnmappedRemoteNamesAction.class);

	private final MutableAccountLinkingStorage storage;
	private final AuthenticationStore authenticationStore;

	public MapUnmappedRemoteNamesAction(
			final MutableAccountLinkingStorage storage,
			AuthenticationStore authenticationStore) {
		super();
		this.storage = storage;
		this.authenticationStore = authenticationStore;
	}

	/**
	 * This method should be called once a CAS ticket granting ticket has been
	 * created and is ready to send to the relying party. It will examine the
	 * ticket granting ticket to find out the local name, and look at the flow
	 * scope to see if there were any preceeding authentications that should now
	 * be linked to that name. The list of successful authentications is
	 * populated by calls to storeUnmappedName().
	 * 
	 * The ticket granting ticket is obtained from the ticket registry using the
	 * ticket granting ticket id stored in the flow scope. See {@link WebUtils}.
	 * 
	 * The new linkages are stored in the account linking resolver set for this
	 * object.
	 * 
	 * For example, if the bean for this object is called
	 * mapUnmappedRemoteNamesAction:
	 * 
	 * <pre>
	 * {@code
	 * <evaluate
	 * 	 expression="mapUnmappedRemoteNamesAction.linkAccounts(flowRequestContext)" />
	 * }
	 * </pre>
	 * 
	 * @param context
	 *            The current request's context.
	 */
	public void linkAccounts(RequestContext context) {
		UnmappedRemoteNames unmappedNames;
		try {
			unmappedNames = getUnmappedNames(context.getFlowScope());
		} catch (RemoteNameHandlingException e) {
			log.error(e.getMessage(), e.getCause());
			return;
		}
		if (unmappedNames.isEmpty()) {
			log.debug("No unmapped names found, so not attempting any mapping.");
			return;
		}
		final AuthenticationHandle handle = authenticationStore
				.getAuthenticationHandle(context);
		if (handle == null) {
			log.error("In order to link names, this action must be called after a "
					+ "successful authentication, but we failed to find a successful "
					+ "authentication in the flow scope.");
		} else {
			final boolean newLinkage = linkAccounts(handle.getLocalName(),
					unmappedNames);
			if (newLinkage) {
				handle.updateAttributes();
			}
		}
	}

	/**
	 * Note a successful authentication for later linkage to a local account. If
	 * storing the authentication fails for any reason, it will be logged, but
	 * no exception will be raised.
	 * 
	 * This method must be called in an on-exception block or somewhere else
	 * that flowExecutionException containing a
	 * {@link UnmappedRemoteNameException} will be set in the flow scope.
	 * 
	 * For example, if the bean for this object is called
	 * mapUnmappedRemoteNamesAction:
	 * 
	 * <pre>
	 * {@code
	 * <transition
	 *   to="generateLoginTicket"
	 * 	 on-exception="com.galois.grid2.UnmappedRemoteNameException">
	 *     <evaluate
	 * 	     expression="mapUnmappedRemoteNamesAction.storeUnmappedName(flowRequestContext)" />
	 * </transition>
	 * }
	 * </pre>
	 * 
	 * @param context
	 *            This request's context.
	 */
	public String storeUnmappedName(RequestContext context) {
		try {
			boolean added = addUnmappedRemoteName(
					getUnmappedRemoteName(context.getFlashScope()),
					context.getFlowScope());
			if (added) {
				return "new-name";
			} else {
				return "already-tried";
			}
		} catch (RemoteNameHandlingException e) {
			log.error(e.getMessage(), e.getCause());
			return "error";
		}
	}

	public List<AttributeModel> getModel(MutableAttributeMap flowScope)
			throws RemoteNameHandlingException {
		List<AttributeModel> items = new ArrayList<AttributeModel>();
		for (Namespaced<RemoteName> name : getUnmappedNames(flowScope)) {
			items.add(new AttributeModel(name.getValue().getUserFields(),
						name.getValue().getAuthorityFields()));
		}
		return items;
	}

	public static class AttributeModel implements Serializable {
		private static final long serialVersionUID = -3870505264786056150L;

		public AttributeModel(List<LabeledField> userAttributes,
				List<LabeledField> authorityAttributes) {
			super();
			this.userAttributes = userAttributes;
			this.authorityAttributes = authorityAttributes;
		}

		private final List<LabeledField> userAttributes;
		private final List<LabeledField> authorityAttributes;

		public List<LabeledField> getUserAttributes() {
			return userAttributes;
		}

		public List<LabeledField> getAuthorityAttributes() {
			return authorityAttributes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((authorityAttributes == null) ? 0 : authorityAttributes
							.hashCode());
			result = prime
					* result
					+ ((userAttributes == null) ? 0 : userAttributes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof AttributeModel)) {
				return false;
			}
			AttributeModel other = (AttributeModel) obj;
			if (authorityAttributes == null) {
				if (other.authorityAttributes != null) {
					return false;
				}
			} else if (!authorityAttributes.equals(other.authorityAttributes)) {
				return false;
			}
			if (userAttributes == null) {
				if (other.userAttributes != null) {
					return false;
				}
			} else if (!userAttributes.equals(other.userAttributes)) {
				return false;
			}
			return true;
		}
	}

	public UnmappedRemoteNames getUnmappedNames(
			final MutableAttributeMap flowScope)
			throws RemoteNameHandlingException {

		UnmappedRemoteNames unmapped;
		try {
			unmapped = (UnmappedRemoteNames) flowScope
					.get(UNMAPPED_REMOTE_NAMES);
		} catch (ClassCastException e) {
			throw new RemoteNameHandlingException(
					"Unexpected type for unmapped remote names", e);
		}
		if (unmapped == null) {
			log.debug("Creating new set of unmapped remote names.");
			unmapped = new UnmappedRemoteNames();
			flowScope.put(UNMAPPED_REMOTE_NAMES, unmapped);
		}
		return unmapped;
	}

	boolean linkAccounts(String localName, UnmappedRemoteNames unmappedNames) {
		boolean storeUpdated = false;
		for (Namespaced<RemoteName> unmappedRemoteName : unmappedNames) {
			try {
				storeUpdated = storage.linkAccounts(localName,
						unmappedRemoteName) | storeUpdated;
				log.debug("Successfully created linkage: localName="
						+ localName + ", remoteName=" + unmappedRemoteName);
			} catch (RemoteNameAlreadyMappedException e) {
				log.warn("Attempted to map a remote name that was already mapped: localName="
						+ localName
						+ ", remoteName="
						+ unmappedRemoteName
						+ ", existingMapping=" + e.getExistingMapping());
				log.info("Continuing despite this descrepancy.");
			}
		}
		return storeUpdated;
	}

	boolean addUnmappedRemoteName(Namespaced<RemoteName> remoteName,
			MutableAttributeMap flowScope) throws RemoteNameHandlingException {
		UnmappedRemoteNames remoteNames = getUnmappedNames(flowScope);

		if (remoteNames.contains(remoteName)) {
			log.debug("Remote name already seen: " + remoteName);
			return false;
		} else {
			log.debug("Adding remote name to unmapped remote name list: "
					+ remoteName);
			remoteNames.add(remoteName);
			return true;
		}
	}

	/**
	 * Get the remote name that triggered the account linking flow. This remote
	 * name is looked up in the flow ActionExecutionException as an
	 * UnmappedRemoteNameAction.
	 * 
	 * @param flashScope
	 *            The flashScope of the flow
	 * @return The remote name that triggered the unmapped remote name exception
	 * @throws RemoteNameHandlingException
	 *             if the expected exception was not found in the flow scope.
	 */
	Namespaced<RemoteName> getUnmappedRemoteName(MutableAttributeMap flashScope)
			throws RemoteNameHandlingException {
		final String excKey = TransitionExecutingFlowExecutionExceptionHandler.FLOW_EXECUTION_EXCEPTION_ATTRIBUTE;
		Object val = flashScope.get(excKey);
		ActionExecutionException exc = null;
		try {
			exc = (ActionExecutionException) val;
		} catch (ClassCastException e) {
			throw new RemoteNameHandlingException(
					"Expected an ActionExecutionException", e);
		}

		if (exc == null) {
			throw new RemoteNameHandlingException(
					"No exception was in the flash scope.");
		}

		UnmappedRemoteNameException unmappedExc;
		try {
			unmappedExc = (UnmappedRemoteNameException) exc.getCause();
		} catch (ClassCastException e) {
			throw new RemoteNameHandlingException(
					"This action is a handler for UnmappedRemoteNameException, "
							+ "but the ActionExecutionException had a different cause",
					e);
		}

		if (unmappedExc == null) {
			throw new RemoteNameHandlingException(
					"This action is a handler for UnmappedRemoteNameException, "
							+ "but no exception was in the flash scope.");
		} else {
			return unmappedExc.getRemoteName();
		}
	}

	/**
	 * This exception type never escapes this class. It is just used as a
	 * convenient flow control mechanism. When one of these exceptions is
	 * caught, the message should be logged as an error, with a possible cause.
	 */
	static final class RemoteNameHandlingException extends Exception {
		private static final long serialVersionUID = -1683936298377123182L;

		public RemoteNameHandlingException(String string, ClassCastException e) {
			super(string, e);
		}

		public RemoteNameHandlingException(String string) {
			super(string);
		}

	}

}
