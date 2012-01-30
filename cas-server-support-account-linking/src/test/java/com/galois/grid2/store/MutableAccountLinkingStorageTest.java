package com.galois.grid2.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.galois.grid2.GenericRemoteName;
import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;
import com.galois.grid2.RemoteNameAlreadyMappedException;
import com.galois.grid2.store.entities.RemoteNameEntity;

public abstract class MutableAccountLinkingStorageTest<T extends MutableAccountLinkingStorage>
		extends TestCase {

	protected T store;

	public abstract T buildStore();

	public abstract void shutdownStore(T store);

	public MutableAccountLinkingStorageTest(String testName) {
		super(testName);
	}

	@Override
	public void setUp() {
		store = this.buildStore();
	}

	@Override
	public void tearDown() {
		this.shutdownStore(store);
	}

	/**
	 * Ensure that an empty store returns a reasonable result (not null) when
	 * queried for an unknown local name.
	 */
	public void testNoRemoteNames() {
		final Collection<Namespaced<RemoteName>> actual = store
				.getRemoteNames("bogus");
		assertEquals(actual.size(), 0);
	}

	/**
	 * Register various remote name attributes under different local names and
	 * ensure that the store returns a coherent result when queried.
	 * 
	 * @throws RemoteNameAlreadyMappedException
	 */
	public void testMultipleRemoteNamesMultipleNamespaces()
			throws RemoteNameAlreadyMappedException {
		final String localName = "local";
		final String otherLocalName = "local2";
		final String namespace1 = "ns1";
		final String namespace2 = "ns2";
		final String namespace3 = "ns3";

		final Map<String, String> attrs1data = new HashMap<String, String>();
		attrs1data.put("key1", "value1");
		attrs1data.put("key2", "value2");
		final Namespaced<RemoteName> attrs1 = mkName(namespace1, attrs1data);

		final Map<String, String> attrs2data = new HashMap<String, String>();
		attrs2data.put("key3", "value3");
		attrs2data.put("key4", "value4");
		final Namespaced<RemoteName> attrs2 = mkName(namespace2, attrs2data);

		final Map<String, String> attrs3data = new HashMap<String, String>();
		attrs2data.put("key5", "value5");
		attrs2data.put("key6", "value6");
		final Namespaced<RemoteName> attrs3 = mkName(namespace3, attrs3data);

		store.linkAccounts(localName, attrs1);
		store.linkAccounts(localName, attrs2);
		store.linkAccounts(otherLocalName, attrs3);

		final Collection<Namespaced<RemoteName>> actual = store
				.getRemoteNames(localName);

		assertEquals(actual.size(), 2);
		assertTrue(actual.contains(attrs1));
		assertTrue(actual.contains(attrs2));

		// This is implied by the size comparison above, but we test for this
		// anyway to make it clear that this is expected to be absent from the
		// result.
		assertFalse(actual.contains(attrs3));
	}

	private Namespaced<RemoteName> mkName(String namespace1,
			Map<String, String> attrs1data) {
		return new Namespaced<RemoteName>(namespace1, new GenericRemoteName(
				attrs1data));
	}

	public void testMissingLocalName() {
		final String namespace = "ns1";

		final Map<String, String> attrsdata = new HashMap<String, String>();
		attrsdata.put("key1", "value1");
		attrsdata.put("key2", "value2");
		final Namespaced<RemoteName> attrs = mkName(namespace, attrsdata);

		final String localName = store.getLocalName(attrs);

		assertNull(localName);
	}

	public void testFoundLocalName() throws RemoteNameAlreadyMappedException {
		String namespace = "ns1";
		final String expectedLocalName = "foobar";

		Map<String, String> attrsdata = new HashMap<String, String>();
		attrsdata.put("key1", "value1");
		attrsdata.put("key2", "value2");
		final Namespaced<RemoteName> attrs = mkName(namespace, attrsdata);

		store.linkAccounts(expectedLocalName, attrs);
		final String actualLocalName = store.getLocalName(attrs);

		assertEquals(expectedLocalName, actualLocalName);
	}

	/**
	 * Test that multiple attempts to map the same local and remote names result
	 * in a single mapping entry in storage and no errors.
	 * 
	 * @throws RemoteNameAlreadyMappedException
	 */
	public void testLocalNameMultipleAttempts()
			throws RemoteNameAlreadyMappedException {
		final String localName = "bob";
		final String ns = "ns";
		final Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("attr1", "val1");

		final Namespaced<RemoteName> expectedAttrs = mkName(ns, attrs);

		// If we try to map the same local and remote names more than once, all
		// attempts should succeed since they request the same mapping.
		final boolean firstLinkResult = store.linkAccounts(localName,
				expectedAttrs);
		assertTrue(firstLinkResult);

		final boolean secondLinkResult = store.linkAccounts(localName,
				expectedAttrs);
		assertFalse(secondLinkResult);

		assertEquals(Collections.singletonList(expectedAttrs),
				store.getRemoteNames(localName));
	}

	public void testLocalNameMappedToDifferentRemoteName()
			throws RemoteNameAlreadyMappedException {
		final String oldLocalName = "bob";
		final String newLocalName = "cygnus";
		final String ns = "ns";

		final Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("attr1", "val1");

		final Namespaced<RemoteName> nsAttrs = mkName(ns, attrs);

		// If we try to map the same local and remote names more than once, all
		// attempts should succeed since they request the same mapping.
		store.linkAccounts(oldLocalName, nsAttrs);
		try {
			store.linkAccounts(newLocalName, nsAttrs);
			fail("Second linkAccounts attempt with different local name did not fail!");
		} catch (RemoteNameAlreadyMappedException e) {
			assertEquals(oldLocalName, e.getExistingMapping());
		}
	}

	/**
	 * Test that when the requested remote name's namespace exists but no remote
	 * name record exist, null is returned.
	 */
	public void testGetLocalNameNoSuchRemoteName()
			throws RemoteNameAlreadyMappedException {
		// Create a remote name in the namespace
		final String ns = "namespace";
		final String localName = "localName";

		final Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("attr1", "val1");
		final Namespaced<RemoteName> nsAttrs = mkName(ns, attrs);

		store.linkAccounts(localName, nsAttrs);

		// Request the local name for a different remote name in the same
		// namespace
		final Map<String, String> attrs2 = new HashMap<String, String>();
		attrs.put("attr2", "val2");
		final Namespaced<RemoteName> nsAttrs2 = mkName(ns, attrs2);

		assertNull(store.getLocalName(nsAttrs2));
	}

	/**
	 * This test is possibly out of place, in that it is testing a limitation of
	 * our Hibernate backend, but since we want to ensure that all our backends
	 * allow for arbitrary length attributes, we put the test here.
	 * 
	 * @throws RemoteNameAlreadyMappedException
	 */
	public void testReallyLongAttributes()
			throws RemoteNameAlreadyMappedException {
		final String ns = "namespace";
		final String localName = "localName";
		final String attrKey = "attr1";

		// We calculate the max length of the string based on the limitations
		// set in the Hibernate backend. The column size for the attributes is
		// length RemoteName.MAX_ATTR_STR_LEN and we need to save room for the
		// attrKey after URLEncoding
		StringBuffer longStringBuffer = new StringBuffer();
		final int length = attrKey.length();
		for (int i = 0; i < RemoteNameEntity.MAX_ATTR_STR_LEN - 1 - length; i++) {
			longStringBuffer.append('a');
		}

		final Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("attr1", longStringBuffer.toString());
		final Namespaced<RemoteName> nsAttrs = mkName(ns, attrs);

		store.linkAccounts(localName, nsAttrs);
	}
}
