package com.galois.grid2;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.NotImplementedException;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.BasePersonAttributeDao;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;

import com.galois.grid2.store.MemoryAccountLinkingStorage;

public class AccountLinkingDaoTest extends TestCase {

	private MutableAccountLinkingStorage storage;
	private IAttributeMerger merger;
	private Map<String, IPersonAttributeDao> wrappedDaos;
	private AccountLinkingDao dao;

	public AccountLinkingDaoTest(String name) {
		super(name);
	}

	public void setUp() {
		wrappedDaos = new HashMap<String, IPersonAttributeDao>();
		storage = new MemoryAccountLinkingStorage();
		merger = new NoncollidingAttributeAdder();
		dao = new AccountLinkingDao(storage, wrappedDaos, merger);
	}

	private Map<String, List<Object>> addDaoAttribute(String localName,
			String namespace, String attrKey, String attrValue) {
		IPersonAttributeDao innerDao = wrappedDaos.get(namespace);
		if (null == innerDao) {
			innerDao = new SimplePersonAttributeDao(
					new HashSet<IPersonAttributes>());
			wrappedDaos.put(namespace, innerDao);
		}
		Set<IPersonAttributes> people = innerDao.getPeople(null);
		Map<String, List<Object>> personAttrs = null;
		for (IPersonAttributes person : people) {
			if (person.getName() == localName) {
				people.remove(person);
				personAttrs = new HashMap<String, List<Object>>(
						person.getAttributes());
			}
		}

		if (personAttrs == null) {
			personAttrs = new HashMap<String, List<Object>>();
		}
		personAttrs.put(attrKey, Collections.<Object> singletonList(attrValue));
		people.add(new NamedPersonImpl(localName, personAttrs));
		return personAttrs;
	}

	public void testUnsupported() {
		assertNull(dao.getAvailableQueryAttributes());
		assertNull(dao.getPeople(null));
		assertNull(dao.getPeopleWithMultivaluedAttributes(null));
	}

	public void testIOException() {
		final NotImplementedException notImplemented = new NotImplementedException();
		wrappedDaos.put("ns", new BasePersonAttributeDao() {
			public Set<String> getPossibleUserAttributeNames() {
				throw notImplemented;
			}

			public IPersonAttributes getPerson(String arg0) {
				throw notImplemented;
			}

			public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
					Map<String, List<Object>> arg0) {
				throw notImplemented;
			}

			public Set<IPersonAttributes> getPeople(Map<String, Object> arg0) {
				throw new RuntimeException("I can't get people!");
			}

			public Set<String> getAvailableQueryAttributes() {
				throw notImplemented;
			}
		});

		String localName = "localName";
		String namespace = "ns";

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("nameAttr", "nameValue");

		// Link the local name to a remote name.
		Namespaced<RemoteName> remoteName = mkName(namespace, attributes);

		storage.linkAccounts(localName, remoteName);

		// Check that the returned IPersonAttributes contains the corresponding
		// attributes.
		IPersonAttributes actual = dao.getPerson(localName);

		assertTrue(actual.getAttributes().isEmpty());
		assertEquals(localName, actual.getName());
	}

	/**
	 * Test that we get a sane result when a local name doesn't correspond to
	 * any attributes.
	 */
	public void testGetPersonNotFound() {
		String localName = "localName";
		Map<String, List<Object>> expectedAttrs = new HashMap<String, List<Object>>();
		IPersonAttributes expected = new NamedPersonImpl(localName,
				expectedAttrs);
		assertEquals(expected, dao.getPerson(localName));
	}

	/**
	 * Test that when remote names are mapped to a local name, those remote
	 * names' attributes are merged and returned.
	 */
	public void testGetPersonFound() {
		String localName = "localName";
		String namespace = "ns";

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("nameAttr", "nameValue");

		// Link the local name to a remote name.
		Namespaced<RemoteName> remoteName = mkName(namespace, attributes);

		storage.linkAccounts(localName, remoteName);

		Map<String, List<Object>> personAttrs = addDaoAttribute(localName,
				namespace, "personAttr", "value");

		// Check that the returned IPersonAttributes contains the corresponding
		// attributes.
		IPersonAttributes actual = dao.getPerson(localName);

		assertEquals(localName, actual.getName());
		assertEquals(personAttrs, actual.getAttributes());
	}

	public void testGetPersonRemoteNameNoAttributes() {
		String localName = "localName";
		String namespace = "ns";

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("nameAttr", "value");

		// Link the local name to a remote name.
		Namespaced<RemoteName> remoteName = mkName(namespace, attributes);

		storage.linkAccounts(localName, remoteName);

		// Set up a DAO for the remote name's namespace which will return no
		// attributes.
		SimplePersonAttributeDao nsDao = new SimplePersonAttributeDao(null);
		wrappedDaos.put(namespace, nsDao);

		// Check that the returned IPersonAttributes contains the corresponding
		// attributes.
		IPersonAttributes actual = dao.getPerson(localName);

		assertEquals(localName, actual.getName());
		assertEquals(Collections.EMPTY_MAP, actual.getAttributes());
	}

	public void testGetPossibleUserAttributeNamesEmpty() {
		assertEquals(Collections.EMPTY_SET, dao.getPossibleUserAttributeNames());
	}

	public void testGetPossibleUserAttributeNamesNontrivial() {
		String localName = "localName";
		String ns1Attr = "ns1Attr";
		String ns2Attr = "ns2Attr";

		addDaoAttribute(localName, "ns1", ns1Attr, "unusedValue1");
		addDaoAttribute(localName, "ns2", ns2Attr, "unusedValue2");

		Set<String> expected = new HashSet<String>();
		expected.add(ns1Attr);
		expected.add(ns2Attr);

		assertEquals(expected, dao.getPossibleUserAttributeNames());
	}

	public void testGetAttributesForRemoteNameNoDao() {
		String ns = "ns1";
		Map<String, String> attrs = new HashMap<String, String>();
		Namespaced<RemoteName> rn = mkName(ns, attrs);
		IPersonAttributes result = dao.getAttributesForRemoteName(rn);
		assertNull(result);
	}

	public void testGetAttributesForRemoteNameDaoNull() {
		String ns = "ns1";
		Map<String, String> attrs = new HashMap<String, String>();
		Namespaced<RemoteName> rn = mkName(ns, attrs);
		wrappedDaos.put(ns, new SimplePersonAttributeDao(null));
		IPersonAttributes result = dao.getAttributesForRemoteName(rn);
		assertNull(result);
	}

	public void testGetAttributesForRemoteNameDaoEmpty() {
		String ns = "ns1";
		Map<String, String> attrs = new HashMap<String, String>();
		Namespaced<RemoteName> rn = mkName(ns, attrs);
		wrappedDaos.put(
				ns,
				new SimplePersonAttributeDao(Collections
						.<IPersonAttributes> emptySet()));
		IPersonAttributes result = dao.getAttributesForRemoteName(rn);
		assertNull(result);
	}

	public void testGetAttributesForRemoteNameDaoMulti() {
		String ns = "ns1";
		addDaoAttribute("localName", ns, "foo", "bar");
		addDaoAttribute("otherLocalName", ns, "foo", "baz");
		Map<String, String> attrs = new HashMap<String, String>();
		Namespaced<RemoteName> rn = mkName(ns, attrs);
		IPersonAttributes result = dao.getAttributesForRemoteName(rn);
		assertNull(result);
	}

	private static Namespaced<RemoteName> mkName(String namespace,
			Map<String, String> attrs) {
		return new Namespaced<RemoteName>(namespace, new GenericRemoteName(
				attrs));
	}
}
