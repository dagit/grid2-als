/**
 * 
 */
package com.galois.grid2;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link CredentialsToPrincipalResolver} that uses a database of linked
 * accounts to identify a local user from remote credentials.
 */
public class AccountLinkingCredentialsToPrincipalResolver extends
		AbstractPersonDirectoryCredentialsToPrincipalResolver {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final RemoteNameFactory converters;
	private final MutableAccountLinkingStorage storage;

	public AccountLinkingCredentialsToPrincipalResolver(
			final RemoteNameFactory converters,
			final MutableAccountLinkingStorage storage) {
		super();
		this.converters = converters;
		this.storage = storage;
	}

	/**
	 * Convert a set of credentials into a set of namespaced attributes that
	 * identify a particular remote user.
	 * 
	 * @param credentials
	 *            The login credentials
	 * @return The remote user identifier or null if the credentials cannot be
	 *         converted into a remote user identifier
	 */
	Namespaced<RemoteName> getRemoteNameForCredentials(
			final Credentials credentials) {
		if (converters.supports(credentials)) {
			return converters.convert(credentials);
		}
		return null;
	}

	public boolean supports(final Credentials credentials) {
		return converters.supports(credentials);
	}

	/**
	 * Attempt to determine the local user identifier for the supplied set of
	 * credentials. This implementation converts the credentials to a remote
	 * user identifier and then consults a store of account linking information
	 * to determine whether the remote user is linked to a local account.
	 * 
	 * If the account is not linked, an unchecked
	 * {@link UnmappedRemoteNameException} is thrown.
	 */
	@Override
	protected String extractPrincipalId(Credentials credentials) {
		final Namespaced<RemoteName> remoteName = getRemoteNameForCredentials(credentials);
		if (remoteName == null) {
			return null;
		} else {
			final String localName = storage.getLocalName(remoteName);
			if (localName == null) {
				throw new UnmappedRemoteNameException(remoteName);
			}
			return localName;
		}
	}
}
