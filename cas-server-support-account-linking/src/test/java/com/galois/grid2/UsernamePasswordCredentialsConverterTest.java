package com.galois.grid2;

import junit.framework.TestCase;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import com.galois.grid2.converters.UsernamePasswordCredentialsConverter;
import com.galois.grid2.converters.UsernameRemoteName;

public class UsernamePasswordCredentialsConverterTest extends TestCase {
	private final RemoteNameConverter converter;

	public UsernamePasswordCredentialsConverterTest() {
		super();
		converter = new UsernamePasswordCredentialsConverter();
	}

	@SuppressWarnings("serial")
	public void testSupports() {
		assertFalse(converter.supportsCredentials(new Credentials() {
		}));

		final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
		assertTrue(converter.supportsCredentials(credentials));
	}

	public void testConversion() {
		final String username = "cygnus";
		final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
		credentials.setUsername(username);

		final RemoteName expectedMap = new UsernameRemoteName(username);

		final RemoteName result = converter.fromCredentials(credentials);
		assertEquals(expectedMap, result);
	}
}
