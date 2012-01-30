package com.galois.grid2;

public interface MutableAccountLinkingStorage extends AccountLinkingStorage {
	/**
	 * @param localName
	 *            The domain-local identifier for the user.
	 * @param attrs
	 *            The attributes that comprise the remote name.
	 * @return true if this call modified the store, or false if this particular
	 *         linkage already existed.
	 * @throws RemoteNameAlreadyMappedException
	 *             if the specified remote name was already linked to a
	 *             different local name.
	 */
	boolean linkAccounts(final String localName,
			final Namespaced<RemoteName> attrs)
			throws RemoteNameAlreadyMappedException;
}
