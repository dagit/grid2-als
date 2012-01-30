package com.galois.grid2.persondir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class UniquifyingMultivaluedAttributeMergerTest extends TestCase {

	private static final List<Object> expectedValues = Arrays
			.asList(new Object[] { "value", "singleton" });
	private static final List<Object> duplicateValues = Arrays
			.asList(new Object[] { "value", "value", "singleton" });

	public void testMergePersonAttributes() {
		List<Object> somethingElse = Collections
				.<Object> singletonList("something-else");
		List<Object> something = Collections
				.<Object> singletonList("something");
		List<Object> bar = Collections.<Object> singletonList("bar");

		// Beware that this is mutated into the result
		Map<String, List<Object>> toModify = new HashMap<String, List<Object>>();
		toModify.put("foo", new ArrayList<Object>(bar));
		toModify.put("quux", somethingElse);

		Map<String, List<Object>> toConsider = new HashMap<String, List<Object>>();
		toConsider.put("foo", bar);
		toConsider.put("baz", something);

		Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
		expected.put("foo", bar);
		expected.put("baz", something);
		expected.put("quux", somethingElse);

		assertEquals(expected,
				new UniquifyingMultivaluedAttributeMerger()
						.mergePersonAttributes(toModify, toConsider));
	}

	public void testUniquifyAttributes() {
		Map<String, List<Object>> actual = new HashMap<String, List<Object>>();
		List<Object> singleValue = Collections.<Object> singletonList("single");
		actual.put("key1", singleValue);
		actual.put("key2", duplicateValues);

		Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
		expected.put("key1", singleValue);
		expected.put("key2", expectedValues);

		assertEquals(expected,
				UniquifyingMultivaluedAttributeMerger
						.uniquifyAttributes(actual));
	}

	public void testUniquify() {
		assertEquals(expectedValues,
				UniquifyingMultivaluedAttributeMerger.uniquify(duplicateValues));
	}

}
