package com.galois.grid2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InvalidNameException;

import junit.framework.TestCase;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import com.galois.grid2.store.MemoryAccountLinkingStorage;

public class AccountLinkingCredentialsToPrincipalResolverTest extends TestCase {

	private MutableAccountLinkingStorage storage;
	private Map<String, RemoteNameConverter> converters;
	private Credentials dummyCredentials;

	protected void setUp() {
		storage = new MemoryAccountLinkingStorage();
		converters = new HashMap<String, RemoteNameConverter>();
		dummyCredentials = new Credentials() {
			private static final long serialVersionUID = 1L;
		};
	}

	class SingletonConverter implements RemoteNameConverter {
		private final Credentials credentials;
		private final RemoteName remoteName;
		private final String namespace;

		public SingletonConverter(final String ns, final RemoteName remoteName) {
			super();
			this.credentials = new Credentials() {
				private static final long serialVersionUID = 1L;
			};
			this.remoteName = remoteName;
			this.namespace = ns;
		}

		public RemoteName fromCredentials(Credentials credentials) {
			return supportsCredentials(credentials) ? this.remoteName : null;
		}

		public boolean supportsCredentials(Credentials credentials) {
			return credentials == this.credentials;
		}

		public Credentials getCredentials() {
			return credentials;
		}

		public RemoteName getAttrs() {
			return remoteName;
		}

		public Namespaced<RemoteName> getRemoteName() {
			return new Namespaced<RemoteName>(namespace, remoteName);
		}

		public String getNamespace() {
			return namespace;
		}

		public RemoteName fromSerialized(Map<String, String> serialized)
				throws InvalidNameException {
			return this.remoteName;
		}
	}

	public void testEmptyResolver() {
		Principal actual = this.getResolver()
				.resolvePrincipal(dummyCredentials);
		assertNull(actual);
		assertFalse(this.getResolver().supports(dummyCredentials));
	}

	private AccountLinkingCredentialsToPrincipalResolver getResolver() {
		return new AccountLinkingCredentialsToPrincipalResolver(
				new MapRemoteNameFactory(converters), storage);
	}

	private SingletonConverter addTrivialConverter(final String ns1,
			final Map<String, String> attrs) {
		final SingletonConverter converter = new SingletonConverter(ns1,
				new GenericRemoteName(attrs));
		this.converters.put(ns1, converter);
		return converter;
	}

	public void testNoLinkage() {
		final SingletonConverter converter = addTrivialConverter("ns1",
				Collections.<String, String> singletonMap("foo", "bar"));
		try {
			final Principal actual = this.getResolver().resolvePrincipal(
					converter.getCredentials());
			fail("Expected an UnmappedRemoteName exception. Got: " + actual);
		} catch (UnmappedRemoteNameException e) {
			assertEquals(e.getRemoteName(), converter.getRemoteName());
		}
	}

	public void testSupports() {
		final SingletonConverter converter = addTrivialConverter("ns1",
				Collections.<String, String> singletonMap("foo", "bar"));
		assertTrue(this.getResolver().supports(converter.getCredentials()));
		assertFalse(this.getResolver().supports(dummyCredentials));
	}

	public void testLocalNameFound() {
		final SingletonConverter converter = addTrivialConverter("ns1",
				Collections.<String, String> singletonMap("foo", "bar"));
		final String localName = "aLocalName";
		storage.linkAccounts(localName, converter.getRemoteName());
		final Principal actual = this.getResolver().resolvePrincipal(
				converter.getCredentials());
		final Principal expected = new SimplePrincipal(localName,
				Collections.<String, Object> emptyMap());
		assertEquals(expected, actual);
	}
}
