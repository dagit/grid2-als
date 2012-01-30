package com.galois.grid2;

import java.util.Collection;


public interface AccountLinkingStorage {

	String getLocalName(final Namespaced<RemoteName> attributes);

	Collection<Namespaced<RemoteName>> getRemoteNames(final String localName);
}
