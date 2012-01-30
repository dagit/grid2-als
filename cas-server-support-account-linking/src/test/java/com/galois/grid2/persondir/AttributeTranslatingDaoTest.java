package com.galois.grid2.persondir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;

import junit.framework.TestCase;

public class AttributeTranslatingDaoTest extends TestCase {

	/**
	 * Test that the query attributes are consistent with the DAO's
	 * configuration.
	 */
	public void testGetAvailableQueryAttributes() {
		String nameIn = "nameIn";
		AttributeTranslatingDao dao = new AttributeTranslatingDao(nameIn,
				"nameOut", null);

		Set<String> expectedNames = new HashSet<String>();
		expectedNames.add(nameIn);
		expectedNames.add(dao.getUsernameAttributeProvider()
				.getUsernameAttribute());

		assertEquals(expectedNames, dao.getAvailableQueryAttributes());
	}

	/**
	 * Check that the output attribute name defaults to the input attribute name
	 * if no output name is specified.
	 */
	public void testGetNameOutDefault() {
		AttributeTranslatingDao dao = new AttributeTranslatingDao("nameIn",
				null, null);
		assertEquals("nameIn", dao.getNameOut());
	}

	/**
	 * Check that the output name is honored if specified.
	 */
	public void testGetNameOut() {
		AttributeTranslatingDao dao = new AttributeTranslatingDao("nameIn",
				"nameOut", null);
		assertEquals("nameOut", dao.getNameOut());
	}

	/**
	 * Check that transforming an attribute value without transforming its name
	 * is supported.
	 */
	public void testTransformPreserveName() {
		String inputAttribute = "inputAttribute";

		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put(inputAttribute,
				Collections.<Object> singletonList("input-value"));

		Map<String, String> transform = new HashMap<String, String>();
		transform.put("input-value", "translated-value");

		Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
		expected.put(inputAttribute,
				Collections.<Object> singletonList("translated-value"));

		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				inputAttribute, null, transform);

		assertEquals(expected, dao.transform(query));
	}

	/**
	 * Check that a transformation of both the name and value of an attribute is
	 * supported.
	 */
	public void testTransformNameAndValue() {
		String inputAttribute = "inputAttribute";
		String outputAttribute = "outputAttribute";

		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put(inputAttribute,
				Collections.<Object> singletonList("input-value"));

		Map<String, String> transform = new HashMap<String, String>();
		transform.put("input-value", "translated-value");

		Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
		expected.put(outputAttribute,
				Collections.<Object> singletonList("translated-value"));

		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				inputAttribute, outputAttribute, transform);

		assertEquals(expected, dao.transform(query));
	}

	/**
	 * Check that transformation of the attribute name only is supported.
	 */
	public void testTransformNameOnly() {
		String inputAttribute = "inputAttribute";
		String outputAttribute = "outputAttribute";

		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put(inputAttribute,
				Collections.<Object> singletonList("input-value"));

		Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
		expected.put(outputAttribute,
				Collections.<Object> singletonList("input-value"));

		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				inputAttribute, outputAttribute, null);

		assertEquals(expected, dao.transform(query));
	}

	/**
	 * Check that a transformation attempt fails gracefully if the input
	 * attribute is absent.
	 */
	public void testTransformInputAbsent() {
		String inputAttribute = "inputAttribute";

		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put("other-attribute",
				Collections.<Object> singletonList("input-value"));

		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				inputAttribute, null, null);

		assertEquals(null, dao.transform(query));
	}

	/**
	 * Check that transformation of a multi-valued attribute works as expected
	 * when given multiple translation possibilities.
	 */
	public void testMultivaluedTranslation() {
		String inputAttribute = "inputAttribute";
		String outputAttribute = "group";

		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		List<Object> values = new ArrayList<Object>();
		values.add("extragroup");
		values.add("ipausers");

		query.put(inputAttribute, values);

		List<Object> expectedValues = new ArrayList<Object>();
		expectedValues.add("extra-group");
		expectedValues.add("ipa-users");

		Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
		expected.put(outputAttribute, expectedValues);

		Map<String, String> trans = new HashMap<String, String>();
		trans.put("ipausers", "ipa-users");
		trans.put("extragroup", "extra-group");

		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				inputAttribute, "group", trans);
		assertEquals(expected, dao.getMultivaluedUserAttributes(query));
	}

	/**
	 * Check that the DAO returns null if no transformation took place.
	 */
	public void testGetPeopleWithMultivaluedAttributesInputAttributeAbsent() {
		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				"inputAttribute", null, null);
		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put("other-attribute",
				Collections.<Object> singletonList("input-value"));
		assertNull(dao.getPeopleWithMultivaluedAttributes(query));
	}

	public void testGetPeopleWithMultivaluedAttributes() {
		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				"inputAttribute", "username", Collections.singletonMap(
						"input-value", "user"));
		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put("inputAttribute",
				Collections.<Object> singletonList("input-value"));
		Set<IPersonAttributes> result = dao
				.getPeopleWithMultivaluedAttributes(query);
		assertEquals(1, result.size());
		IPersonAttributes person = result.iterator().next();
		assertEquals("user", person.getName());
		assertEquals(Collections.<Object> singletonList("user"), person
				.getAttributes().get("username"));
	}

	public void testGetPeopleWithMultivaluedAttributesWithUsername() {
		AttributeTranslatingDao dao = new AttributeTranslatingDao(
				"inputAttribute", "username", Collections.singletonMap(
						"input-value", "user"));

		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put("inputAttribute",
				Collections.<Object> singletonList("input-value"));
		query.put("username", Collections.<Object> singletonList("other-user"));
		Set<IPersonAttributes> result = dao
				.getPeopleWithMultivaluedAttributes(query);
		assertEquals(1, result.size());
		IPersonAttributes person = result.iterator().next();
		assertEquals("other-user", person.getName());
		assertEquals(Collections.<Object> singletonList("user"), person
				.getAttributes().get("username"));
	}

	/**
	 * Check that the user attribute names are consistent with the DAO's
	 * configuration.
	 */
	public void testGetPossibleUserAttributeNames() {
		String nameOut = "nameOut";

		AttributeTranslatingDao dao = new AttributeTranslatingDao("nameIn",
				nameOut, null);

		assertEquals(Collections.singleton(nameOut),
				dao.getPossibleUserAttributeNames());
	}

}
