package com.galois.grid2.store;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galois.grid2.LocalNameExtractor;
import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;
import com.galois.grid2.RemoteNameAlreadyMappedException;

public class AutomaticLinkingStorage implements MutableAccountLinkingStorage {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final MutableAccountLinkingStorage storage;
	private final LocalNameExtractor localNameExtractor;

	public AutomaticLinkingStorage(MutableAccountLinkingStorage storage,
			LocalNameExtractor localNameExtractor) {
		super();
		this.storage = storage;
		this.localNameExtractor = localNameExtractor;
	}

	public MutableAccountLinkingStorage getStore() {
		return storage;
	}

	public LocalNameExtractor getLocalNameExtractor() {
		return localNameExtractor;
	}

	public Collection<Namespaced<RemoteName>> getRemoteNames(String localName) {
		return this.storage.getRemoteNames(localName);
	}

	public boolean linkAccounts(String localName, Namespaced<RemoteName> attrs)
			throws RemoteNameAlreadyMappedException {
		return this.storage.linkAccounts(localName, attrs);
	}

	/**
	 * Find the appropriate local name for the specified remote name. If the
	 * localNameExtractor returns a local name, then a mapping is automatically
	 * established. Otherwise, the account linking database is consulted.
	 * 
	 * @param remoteName
	 *            The namespaced set of attributes that identifies a remote user
	 * @return The local name for the specified remote name, or null if no local
	 *         name is found.
	 */
	public String getLocalName(final Namespaced<RemoteName> remoteName) {
		String localName = this.localNameExtractor.getLocalName(remoteName);

		if (localName == null) {
			// Use the remote name to get its synonymous local name, if any.
			return storage.getLocalName(remoteName);
		} else {
			try {
				storage.linkAccounts(localName, remoteName);
				return localName;
			} catch (RemoteNameAlreadyMappedException e) {
				// If the database has a value that differs from the
				// automatically-derived mapping, then:
				// 1. The database wins
				// 2. We log an error
				log.error(
						"Generated local name does not match database for remote name: "
								+ remoteName, e);
				return e.getExistingMapping();
			}
		}
	}
}
