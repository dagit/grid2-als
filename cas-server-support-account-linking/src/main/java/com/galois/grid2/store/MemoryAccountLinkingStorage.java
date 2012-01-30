/**
 * 
 */
package com.galois.grid2.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;
import com.galois.grid2.RemoteNameAlreadyMappedException;

/**
 * A memory-backed implementation of an account linking storage backend.
 * 
 * @author cygnus
 */
public class MemoryAccountLinkingStorage implements
		MutableAccountLinkingStorage {

	/**
	 * Map of local name to list of namespaced attributes. We use a concrete
	 * type for the lists so that when we clone the results to return in
	 * getRemoteNames, the compiler knows which clone() implementation to use.
	 */
	private final Map<String, ArrayList<Namespaced<RemoteName>>> linkedAccounts;

	public MemoryAccountLinkingStorage() {
		linkedAccounts = new HashMap<String, ArrayList<Namespaced<RemoteName>>>();
	}

	public static MemoryAccountLinkingStorage prebuilt(
			Map<String, List<Namespaced<RemoteName>>> initialMappings)
			throws RemoteNameAlreadyMappedException {
		MemoryAccountLinkingStorage store = new MemoryAccountLinkingStorage();
		for (Entry<String, List<Namespaced<RemoteName>>> e : initialMappings
				.entrySet()) {
			for (Namespaced<RemoteName> nsAttrs : e.getValue()) {
				store.linkAccounts(e.getKey(), nsAttrs);
			}
		}

		return store;

	}

	public boolean linkAccounts(final String localName,
			final Namespaced<RemoteName> attrs)
			throws RemoteNameAlreadyMappedException {
		// Get the list of mappings for the specified local name.
		ArrayList<Namespaced<RemoteName>> mappedAttributes = linkedAccounts
				.get(localName);

		// First, check that the remote name does not already have a mapping
		// anywhere.
		for (final Entry<String, ArrayList<Namespaced<RemoteName>>> entry : linkedAccounts
				.entrySet()) {
			if (entry.getValue().contains(attrs)
					&& (!entry.getKey().equals(localName))) {
				throw new RemoteNameAlreadyMappedException(entry.getKey());
			}
		}

		// If the local name has no mappings, create a list for them.
		if (mappedAttributes == null) {
			mappedAttributes = new ArrayList<Namespaced<RemoteName>>();
			linkedAccounts.put(localName, mappedAttributes);
		}

		// Add the mapping if it isn't already in the set.
		if (!mappedAttributes.contains(attrs)) {
			mappedAttributes.add(attrs);
			return true;
		} else {
			return false;
		}
	}

	public String getLocalName(final Namespaced<RemoteName> attrs) {
		for (final Entry<String, ArrayList<Namespaced<RemoteName>>> e : linkedAccounts
				.entrySet()) {
			for (final Namespaced<RemoteName> other : e.getValue()) {
				if (attrs.equals(other)) {
					return e.getKey();
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Namespaced<RemoteName>> getRemoteNames(
			final String localName) {
		final ArrayList<Namespaced<RemoteName>> result = linkedAccounts
				.get(localName);

		if (result == null) {
			return new ArrayList<Namespaced<RemoteName>>();
		} else {
			return (List<Namespaced<RemoteName>>) result.clone();
		}
	}

}
