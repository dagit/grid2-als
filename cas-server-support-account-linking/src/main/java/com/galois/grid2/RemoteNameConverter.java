package com.galois.grid2;

import java.util.Map;

import javax.naming.InvalidNameException;

import org.jasig.cas.authentication.principal.Credentials;

public interface RemoteNameConverter {
	RemoteName fromCredentials(final Credentials credentials);
	
	boolean supportsCredentials(final Credentials credentials);

	RemoteName fromSerialized(final Map<String, String> serialized)
			throws InvalidNameException;
}
