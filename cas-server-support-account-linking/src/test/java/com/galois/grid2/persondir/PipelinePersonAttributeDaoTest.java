package com.galois.grid2.persondir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.jasig.services.persondir.support.StubPersonAttributeDao;

public class PipelinePersonAttributeDaoTest extends TestCase {

	public void testPipelinePersonAttributeDaoEmpty() {
		try {
			@SuppressWarnings("unused")
			PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(
					Collections.<IPersonAttributeDao> emptyList());
			fail("Expected constructor to reject empty list");
		} catch (RuntimeException e) {
			// Pass.
		}
	}

	public void testGetAvailableQueryAttributesMultipleDaos() {
		StubPersonAttributeDao dao1 = new StubPersonAttributeDao() {
			public Set<String> getPossibleUserAttributeNames() {
				return Collections.singleton("attr1");
			}

			public Set<String> getAvailableQueryAttributes() {
				return Collections.singleton("q1");
			}
		};

		StubPersonAttributeDao dao2 = new StubPersonAttributeDao() {
			public Set<String> getPossibleUserAttributeNames() {
				return Collections.singleton("attr2");
			}

			public Set<String> getAvailableQueryAttributes() {
				return Collections.singleton("q2");
			}
		};

		ArrayList<IPersonAttributeDao> daos = new ArrayList<IPersonAttributeDao>();
		daos.add(dao1);
		daos.add(dao2);

		PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(daos);
		assertEquals(Collections.singleton("attr2"),
				dao.getPossibleUserAttributeNames());
		assertEquals(Collections.singleton("q1"),
				dao.getAvailableQueryAttributes());
	}

	public void testGetAvailableQueryAttributesSingleDao() {
		StubPersonAttributeDao dao1 = new StubPersonAttributeDao() {
			public Set<String> getPossibleUserAttributeNames() {
				return Collections.singleton("attr1");
			}

			public Set<String> getAvailableQueryAttributes() {
				return Collections.singleton("q1");
			}
		};

		PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(
				Collections.<IPersonAttributeDao> singletonList(dao1));
		assertEquals(Collections.singleton("attr1"),
				dao.getPossibleUserAttributeNames());
		assertEquals(Collections.singleton("q1"),
				dao.getAvailableQueryAttributes());
	}

	public void testGetPeopleWithMultivaluedAttributesSingleDao() {
		Map<String, List<Object>> attrs = new HashMap<String, List<Object>>();
		attrs.put("attr1", Collections.<Object> singletonList("value1"));
		attrs.put("attr2", Collections.<Object> singletonList("value2"));

		StubPersonAttributeDao dao1 = new StubPersonAttributeDao(attrs);
		PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(
				Collections.<IPersonAttributeDao> singletonList(dao1));

		Set<IPersonAttributes> result = dao
				.getPeopleWithMultivaluedAttributes(Collections
						.<String, List<Object>> emptyMap());

		assertEquals(1, result.size());
		IPersonAttributes expectedAttrs = new NamedPersonImpl(null, attrs);

		assertEquals(Collections.singleton(expectedAttrs), result);
	}

	public void testGetPeopleWithMultivaluedAttributesMultipleDaos() {
		Map<String, List<Object>> attrs1 = new HashMap<String, List<Object>>();
		attrs1.put("attr1", Collections.<Object> singletonList("value1"));
		attrs1.put("attr2", Collections.<Object> singletonList("value2"));

		StubPersonAttributeDao dao1 = new StubPersonAttributeDao(attrs1);

		Map<String, List<Object>> attrs2 = new HashMap<String, List<Object>>();
		attrs2.put("attr3", Collections.<Object> singletonList("value3"));
		attrs2.put("attr4", Collections.<Object> singletonList("value4"));

		StubPersonAttributeDao dao2 = new StubPersonAttributeDao() {
			public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
					Map<String, List<Object>> attrs) {
				Map<String, List<Object>> newAttrs = new HashMap<String, List<Object>>(
						attrs);
				newAttrs.put("attr3",
						Collections.<Object> singletonList("value3"));
				IPersonAttributes p = new NamedPersonImpl(null, newAttrs);
				return Collections.singleton(p);
			}
		};

		List<IPersonAttributeDao> daos = new ArrayList<IPersonAttributeDao>();
		daos.add(dao1);
		daos.add(dao2);

		PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(daos);

		Set<IPersonAttributes> result = dao
				.getPeopleWithMultivaluedAttributes(new HashMap<String, List<Object>>());

		assertEquals(1, result.size());
		Map<String, List<Object>> expectedAttrs = new HashMap<String, List<Object>>(
				attrs1);
		expectedAttrs
				.put("attr3", Collections.<Object> singletonList("value3"));
		IPersonAttributes p = new NamedPersonImpl(null, expectedAttrs);

		assertEquals(Collections.singleton(p), result);
	}

	public void testGetPeopleWithMultivaluedAttributesMultipleDaosWithFailure() {
		Map<String, List<Object>> attrs1 = new HashMap<String, List<Object>>();
		attrs1.put("attr1", Collections.<Object> singletonList("value1"));
		attrs1.put("attr2", Collections.<Object> singletonList("value2"));

		StubPersonAttributeDao dao1 = new StubPersonAttributeDao(attrs1);

		Map<String, List<Object>> attrs2 = new HashMap<String, List<Object>>();
		attrs2.put("attr3", Collections.<Object> singletonList("value3"));
		attrs2.put("attr4", Collections.<Object> singletonList("value4"));

		StubPersonAttributeDao dao2 = new StubPersonAttributeDao() {
			public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
					Map<String, List<Object>> attrs) {
				return null;
			}
		};

		List<IPersonAttributeDao> daos = new ArrayList<IPersonAttributeDao>();
		daos.add(dao1);
		daos.add(dao2);

		PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(daos);

		Set<IPersonAttributes> result = dao
				.getPeopleWithMultivaluedAttributes(new HashMap<String, List<Object>>());

		assertEquals(0, result.size());
	}

	public void testGetPeopleWithMultivaluedAttributesMultipleDaosWithFanout() {
		StubPersonAttributeDao dao1 = new StubPersonAttributeDao() {
			public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
					Map<String, List<Object>> attrs) {
				Set<IPersonAttributes> result = new HashSet<IPersonAttributes>();

				Map<String, List<Object>> newAttrs = new HashMap<String, List<Object>>(
						attrs);
				newAttrs.put("attr1",
						Collections.<Object> singletonList("value1"));
				newAttrs.put("username",
						Collections.<Object> singletonList("name1"));

				IPersonAttributes p = new NamedPersonImpl("name1", newAttrs);
				result.add(p);

				Map<String, List<Object>> newAttrs2 = new HashMap<String, List<Object>>(
						attrs);
				newAttrs2.put("attr1",
						Collections.<Object> singletonList("value1"));
				newAttrs2.put("username",
						Collections.<Object> singletonList("name2"));

				IPersonAttributes p2 = new NamedPersonImpl("name2", newAttrs2);
				result.add(p2);

				return result;
			}
		};

		StubPersonAttributeDao dao2 = new StubPersonAttributeDao() {
			public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
					Map<String, List<Object>> attrs) {
				Map<String, List<Object>> newAttrs = new HashMap<String, List<Object>>(
						attrs);
				newAttrs.put("attr2",
						Collections.<Object> singletonList("value2"));

				System.out.println("Old attrs: " + attrs);

				IPersonAttributes p = new NamedPersonImpl((String) attrs.get(
						"username").get(0), newAttrs);

				System.out.println("Returning attributes: " + p);

				return Collections.singleton(p);
			}
		};

		List<IPersonAttributeDao> daos = new ArrayList<IPersonAttributeDao>();
		daos.add(dao1);
		daos.add(dao2);

		PipelinePersonAttributeDao dao = new PipelinePersonAttributeDao(daos);

		Set<IPersonAttributes> result = dao
				.getPeopleWithMultivaluedAttributes(new HashMap<String, List<Object>>());

		System.out.println("Final result of pipeline: " + result);

		assertEquals(2, result.size());

		Map<String, List<Object>> expectedAttrs = new HashMap<String, List<Object>>();
		expectedAttrs
				.put("attr1", Collections.<Object> singletonList("value1"));
		expectedAttrs
				.put("attr2", Collections.<Object> singletonList("value2"));
		IPersonAttributes p1 = new NamedPersonImpl("name1", expectedAttrs);

		Map<String, List<Object>> expectedAttrs2 = new HashMap<String, List<Object>>();
		expectedAttrs2.put("attr1",
				Collections.<Object> singletonList("value1"));
		expectedAttrs2.put("attr2",
				Collections.<Object> singletonList("value2"));
		IPersonAttributes p2 = new NamedPersonImpl("name2", expectedAttrs2);

		Set<IPersonAttributes> expected = new HashSet<IPersonAttributes>();
		expected.add(p1);
		expected.add(p2);
		assertEquals(expected, result);
	}
}
