package com.galois.grid2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.BasePersonAttributeDao;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link IPersonAttributeDao} that uses a database of
 * linked accounts to access many attribute stores.
 * 
 * @author j3h
 * 
 */
public class AccountLinkingDao extends BasePersonAttributeDao {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	private final AccountLinkingStorage storage;
	private final IAttributeMerger merger;
	private final Map<String, IPersonAttributeDao> daos;

	public AccountLinkingDao(AccountLinkingStorage storage,
			Map<String, IPersonAttributeDao> daos, IAttributeMerger merger) {
		super();
		this.storage = storage;
		this.merger = merger;
		this.daos = daos;
	}

	/**
	 * Use the appropriate {@link IPersonAttributeDao} to find the attributes
	 * for this remote name.
	 * 
	 * @param rn
	 *            The set of attributes necessary for this
	 *            {@link IPersonAttributeDao} to find attributes.
	 * @return The set of attributes for this remote name. If we fail to find
	 *         attributes, an empty map will be returned.
	 */
	IPersonAttributes getAttributesForRemoteName(final Namespaced<RemoteName> rn) {
		final String ns = rn.getNamespace();
		final IPersonAttributeDao dao = daos.get(ns);
		if (dao == null) {
			log.info("No suitable DAO configured for namespace = '" + ns + "'");
		} else {
			// We have to copy into the new kind of map since it's illegal to
			// cast the generic.
			Map<String, Object> attrs = new HashMap<String, Object>(rn
					.getValue().getAttributeMap());

			final Set<IPersonAttributes> people;

			// We found a way to get and/or convert attributes for this
			// namespace. Do it.
			try {
				people = dao.getPeople(attrs);
			} catch (RuntimeException e) {
				log.error("DAO for " + ns + " failed", e);
				return null;
			}

			if (people == null) {
				log.info("IPersonAttributeDao indicated query failure for: remoteName="
						+ rn);
				return null;
			}

			switch (people.size()) {
			case 0: // No results
				break;

			case 1: // Unambiguous results
				return people.iterator().next();

			default: // Ambiguous results

				// XXX: We got more than one person. What do we do here? For
				// now, we assume that it's better to return no attributes than
				// potentially wrong attributes.

				// XXX: to allow administrators to troubleshoot why multiple
				// people were returned from the attribute source, it would be
				// good to log the key/value pairs that were used to do the
				// lookup. However, as those pairs were built from Credentials,
				// they could contain sensitive information. What is the
				// safest/most useful level of logging we can do here?
				log.warn("IPersonAttributeDao returned multiple person entries, which is ambiguous; "
						+ "returning null (DAO class in use: "
						+ dao.getClass().getName() + ")");
			}
		}
		return null;
	}

	public IPersonAttributes getPerson(String localName) {
		// Use the local name to get all linked remote names (at least one, if
		// we got this far).
		log.debug("Getting attributes for: " + localName);

		final Iterable<Namespaced<RemoteName>> remoteNames = storage
				.getRemoteNames(localName);

		Map<String, List<Object>> merged = new HashMap<String, List<Object>>();

		// For each remote name, fetch attributes from the appropriate attribute
		// authority and merge all of the results.
		for (final Namespaced<RemoteName> rn : remoteNames) {
			IPersonAttributes newAttributes = getAttributesForRemoteName(rn);
			log.debug("Got attributes for remote name [" + rn + "]: "
					+ newAttributes);
			if (newAttributes != null) {
				merged = merger.mergeAttributes(merged,
						newAttributes.getAttributes());
			}
		}

		return new NamedPersonImpl(localName, merged);
	}

	public Set<String> getPossibleUserAttributeNames() {
		Set<String> result = new HashSet<String>();

		for (final IPersonAttributeDao dao : daos.values()) {
			result.addAll(dao.getPossibleUserAttributeNames());
		}

		return result;
	}

	/**
	 * Return null since this class does not support querying for users by
	 * attribute.
	 */
	public Set<String> getAvailableQueryAttributes() {
		return null;

	}

	/**
	 * Return null since this class does not support querying for users by
	 * attribute map.
	 */
	public Set<IPersonAttributes> getPeople(Map<String, Object> arg0) {
		return null;
	}

	/**
	 * Return null since this class does not support querying for users by
	 * attribute map.
	 */
	public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
			Map<String, List<Object>> arg0) {
		return null;
	}
}
