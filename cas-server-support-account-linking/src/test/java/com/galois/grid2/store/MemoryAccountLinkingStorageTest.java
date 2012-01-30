package com.galois.grid2.store;

import com.galois.grid2.store.MemoryAccountLinkingStorage;

public class MemoryAccountLinkingStorageTest extends
		MutableAccountLinkingStorageTest<MemoryAccountLinkingStorage> {

	public MemoryAccountLinkingStorageTest(String testName) {
		super(testName);
	}

	@Override
	public MemoryAccountLinkingStorage buildStore() {
		return new MemoryAccountLinkingStorage();
	}

	@Override
	public void shutdownStore(MemoryAccountLinkingStorage store) {
		// no-op
	}
}
