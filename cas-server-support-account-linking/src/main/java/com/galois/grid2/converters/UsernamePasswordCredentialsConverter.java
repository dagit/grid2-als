package com.galois.grid2.converters;

import java.util.Map;

import javax.naming.InvalidNameException;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import com.galois.grid2.RemoteNameConverter;
import com.galois.grid2.RemoteName;

public class UsernamePasswordCredentialsConverter implements
		RemoteNameConverter {


	public RemoteName fromCredentials(Credentials credentials) {
		final UsernamePasswordCredentials upCredentials = (UsernamePasswordCredentials) credentials;
		return new UsernameRemoteName(upCredentials.getUsername());
	}

	public boolean supportsCredentials(Credentials credentials) {
		return credentials instanceof UsernamePasswordCredentials;
	}

	public RemoteName fromSerialized(Map<String, String> serialized) throws InvalidNameException {
		return UsernameRemoteName.fromAttributes(serialized);
	}

}
