package com.galois.grid2.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.galois.grid2.store.AttributeEncoding;

import junit.framework.TestCase;

public class AttributeEncodingTest extends TestCase {

	public AttributeEncodingTest(String name) {
		super(name);
	}

	/**
	 * Ensure that encoding of namespaced attributes works and that the encoded
	 * attributes are sorted by key, alphanumerically.
	 */
	public void testBasicEncoding() {
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put("foo", "bar");
		attrs.put("ba&z", "stuff=");

		String expectedValue = "ba%26z=stuff%3D&foo=bar";

		assertEquals(expectedValue, AttributeEncoding.encode(attrs));
	}

	/**
	 * Ensure that decoding of a properly-encoded string results in the expected
	 * set of attribute values.
	 */
	public void testBasicDecoding() {
		Map<String, Object> expectedAttrs = new HashMap<String, Object>();
		expectedAttrs.put("foo", "bar");
		expectedAttrs.put("ba&z", "stuff=");

		String inputValue = "ba%26z=stuff%3D&foo=bar";

		assertEquals(expectedAttrs, new AttributeEncoding(inputValue).decode());
	}

	/**
	 * Test that an improperly-encoded string cannot be decoded.
	 */
	public void testBadEncoding() {
		List<String> badEncodingCases = new ArrayList<String>();
		badEncodingCases.add("stuff");
		badEncodingCases.add("");
		badEncodingCases.add("foo=");
		badEncodingCases.add("&foo=bar");

		for (String badEncodingCase : badEncodingCases) {
			final AttributeEncoding encoded = new AttributeEncoding(
					badEncodingCase);
			assertNull(badEncodingCase, encoded.decode());
		}
	}
}
