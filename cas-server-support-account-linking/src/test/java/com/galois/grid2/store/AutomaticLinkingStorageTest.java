package com.galois.grid2.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.galois.grid2.LocalNameExtractor;
import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;
import com.galois.grid2.GenericRemoteName;

public class AutomaticLinkingStorageTest extends
		MutableAccountLinkingStorageTest<AutomaticLinkingStorage> {

	private String localName;

	public AutomaticLinkingStorageTest(String testName) {
		super(testName);
	}

	@Override
	public AutomaticLinkingStorage buildStore() {
		this.localName = null;
		LocalNameExtractor localNameExtractor = new LocalNameExtractor() {
			public String getLocalName(Namespaced<RemoteName> remoteName) {
				return AutomaticLinkingStorageTest.this.localName;
			}
		};
		MutableAccountLinkingStorage mutableStorage = new MemoryAccountLinkingStorage();
		return new AutomaticLinkingStorage(mutableStorage, localNameExtractor);
	}

	@Override
	public void shutdownStore(AutomaticLinkingStorage store) {
	}

	public void testAutomaticLinkingSuccess() {
		this.localName = "localName";

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("unused", "value");
		Namespaced<RemoteName> remoteName = mkName("ns", attributes);

		assertEquals(this.localName, this.store.getLocalName(remoteName));
		assertEquals(Collections.singletonList(remoteName),
				this.store.getRemoteNames(this.localName));
	}

	public void testAutomaticLinkingFailureAlreadyMapped() {
		this.localName = "localName";

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("unused", "value");
		Namespaced<RemoteName> remoteName = mkName("ns", attributes);

		// Pre-establish a linkage to a different local name to cause a mapping
		// failure.
		this.store.linkAccounts("otherLocalName", remoteName);

		String actual = this.store.getLocalName(remoteName);
		assertEquals("otherLocalName", actual);
	}

	private Namespaced<RemoteName> mkName(String ns, Map<String, String> attrs) {
		return new Namespaced<RemoteName>(ns, new GenericRemoteName(attrs));
	}
}
