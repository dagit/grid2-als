package com.galois.grid2;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;

import junit.framework.TestCase;

import org.jasig.cas.authentication.principal.Credentials;

public class MapRemoteNameFactoryTest extends TestCase {

	public MapRemoteNameFactoryTest(String name) {
		super(name);
	}

	@SuppressWarnings("serial")
	public void testNoConverters() {
		RemoteNameFactory rnf = new MapRemoteNameFactory(
				Collections.<String, RemoteNameConverter> emptyMap());

		assertNull(rnf.convert(new Credentials() {
		}));
	}

	@SuppressWarnings("serial")
	public void testSupportedFailedConversion() {
		RemoteNameConverter conv = new RemoteNameConverter() {

			public RemoteName fromCredentials(Credentials credentials) {
				return null;
			}

			public boolean supportsCredentials(Credentials credentials) {
				return true;
			}

			public RemoteName fromSerialized(Map<String, String> serialized)
					throws InvalidNameException {
				throw new RuntimeException("Should be unused in this test");
			}
		};

		RemoteNameFactory rnf = new MapRemoteNameFactory(
				Collections.singletonMap("conv", conv));

		assertNull(rnf.convert(new Credentials() {
		}));
	}

	@SuppressWarnings("serial")
	public void testSupportedSuccessfulConversion() {
		
		final RemoteName rn = new RemoteName() {

			public Map<String, String> getAttributeMap() {
				return Collections.singletonMap("key", "value");
			}

			public List<LabeledField> getUserFields() {
				return Collections.singletonList(new LabeledField("Key",
						"value"));
			}

			public List<LabeledField> getAuthorityFields() {
				return Collections.emptyList();
			}
		};
		
		RemoteNameConverter conv = new RemoteNameConverter() {

			public RemoteName fromCredentials(Credentials credentials) {
				return rn;
			}

			public boolean supportsCredentials(Credentials credentials) {
				return true;
			}

			public RemoteName fromSerialized(Map<String, String> serialized)
					throws InvalidNameException {
				throw new RuntimeException("Should be unused in this test");
			}
		};

		RemoteNameFactory rnf = new MapRemoteNameFactory(
				Collections.singletonMap("conv", conv));

		Namespaced<RemoteName> expected = new Namespaced<RemoteName>("conv", rn);
		assertEquals(expected, rnf.convert(new Credentials() {
		}));
	}
	
	@SuppressWarnings("serial")
	public void testSupports() {
		RemoteNameConverter conv1 = new RemoteNameConverter() {

			public RemoteName fromCredentials(Credentials credentials) {
				throw new RuntimeException("Should be unused in this test");
			}

			public boolean supportsCredentials(Credentials credentials) {
				return false;
			}

			public RemoteName fromSerialized(Map<String, String> serialized)
					throws InvalidNameException {
				throw new RuntimeException("Should be unused in this test");
			}
		};

		RemoteNameConverter conv2 = new RemoteNameConverter() {

			public RemoteName fromCredentials(Credentials credentials) {
				throw new RuntimeException("Should be unused in this test");
			}

			public boolean supportsCredentials(Credentials credentials) {
				return true;
			}

			public RemoteName fromSerialized(Map<String, String> serialized)
					throws InvalidNameException {
				throw new RuntimeException("Should be unused in this test");
			}
		};

		Map<String, RemoteNameConverter> converters = new HashMap<String, RemoteNameConverter>();
		converters.put("conv1", conv1);
		converters.put("conv2", conv2);
		RemoteNameFactory rnf = new MapRemoteNameFactory(converters);

		assertTrue(rnf.supports(new Credentials() {
		}));

		assertFalse(new MapRemoteNameFactory(Collections.<String, RemoteNameConverter> emptyMap())
				.supports(new Credentials() {
				}));
	}

	public void testFromSerializedInvalid() {
		RemoteNameFactory rnf = new MapRemoteNameFactory(
				Collections.<String, RemoteNameConverter> emptyMap());
		try {
			Map<String, String> serialized = Collections.singletonMap("key",
					"value");
			rnf.fromSerialized("bogus", serialized);
			fail("InvalidNameException expected, but was not thrown");
		} catch (InvalidNameException e) {

		}
	}

	public void testFromSerializedConverterFailed() {
		// In which a converter for the namespace exists, but failed to
		// unserialize the remote name.

		RemoteNameConverter conv = new RemoteNameConverter() {

			public RemoteName fromCredentials(Credentials credentials) {
				throw new RuntimeException("Should be unused in this test");
			}

			public boolean supportsCredentials(Credentials credentials) {
				return true;
			}

			public RemoteName fromSerialized(Map<String, String> serialized)
					throws InvalidNameException {
				throw new InvalidNameException("error");
			}
		};

		RemoteNameFactory rnf = new MapRemoteNameFactory(
				Collections.singletonMap("ns", conv));
		Map<String, String> serialized = Collections.singletonMap("key",
				"value");

		try {
			rnf.fromSerialized("ns", serialized);
			fail("InvalidNameException expected, but was not thrown");
		} catch (InvalidNameException e) {

		}
	}

	public void testFromSerializedValid() throws InvalidNameException {
		final RemoteName rn = new RemoteName() {

			public Map<String, String> getAttributeMap() {
				return Collections.singletonMap("key", "value");
			}

			public List<LabeledField> getUserFields() {
				return Collections.singletonList(new LabeledField("Key",
						"value"));
			}

			public List<LabeledField> getAuthorityFields() {
				return Collections.emptyList();
			}
		};

		RemoteNameConverter conv = new RemoteNameConverter() {

			public RemoteName fromCredentials(Credentials credentials) {
				throw new RuntimeException("Should be unused in this test");
			}

			public boolean supportsCredentials(Credentials credentials) {
				return true;
			}

			public RemoteName fromSerialized(Map<String, String> serialized)
					throws InvalidNameException {
				return rn;
			}
		};

		RemoteNameFactory rnf = new MapRemoteNameFactory(
				Collections.singletonMap("ns", conv));
		Map<String, String> serialized = Collections.singletonMap("key",
				"value");
		Namespaced<RemoteName> expected = new Namespaced<RemoteName>("ns", rn);

		assertEquals(expected, rnf.fromSerialized("ns", serialized));
	}
}
