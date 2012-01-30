package com.galois.grid2;

public interface LocalNameExtractor {
	/**
	 * @param remoteName
	 * @return null if this remote name cannot be automatically converted to a
	 *         local name (this is the common case).
	 * 
	 */
	String getLocalName(Namespaced<RemoteName> remoteName);
}
