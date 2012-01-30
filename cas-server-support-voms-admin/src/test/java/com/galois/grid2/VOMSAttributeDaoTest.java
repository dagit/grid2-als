package com.galois.grid2;

import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributes;

import com.galois.grid2.voms.AttributeFetchException;
import com.galois.grid2.voms.AttributeFetcher;
import com.galois.grid2.voms.Group;
import com.galois.grid2.voms.Role;
import com.galois.grid2.voms.UserAttributes;
import com.galois.grid2.voms.UserInfo;
import com.galois.grid2.voms.soap.AttributeValue;

/**
 * Unit test for simple App.
 */
public class VOMSAttributeDaoTest extends TestCase {
	private static final class FailingAttributeFetcher implements
			AttributeFetcher {
		public UserAttributes fetchAttributes(UserInfo arg0)
				throws AttributeFetchException {
			throw new AttributeFetchException("Testing failure");
		}
	}

	private static final String ISSUER_DN = "O=Galois, CN=Unit Testing CA";
	private static final String SUBJECT_DN = "O=Galois, CN=A User";

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public VOMSAttributeDaoTest(String testName) {
		super(testName);
	}

	public void testGetPerson() {
		AttributeFetcher attributeFetcher = new FailingAttributeFetcher();
		VOMSAttributeDao dao = new VOMSAttributeDao(attributeFetcher);
		assertNull(dao.getPerson("anything"));
	}

	public void testResolvePrincipalVOMSError() {
		final VOMSAttributeDao dao = new VOMSAttributeDao(
				new FailingAttributeFetcher());
		assertNull(dao.getPeople(getQuery()));
	}

	public void testResolvePrincipalInvalidQuery() {
		final VOMSAttributeDao dao = new VOMSAttributeDao(
				new FailingAttributeFetcher());
		assertNull(dao.getPeople(Collections.<String, Object> emptyMap()));
	}

	/**
	 * @return
	 * @throws FileNotFoundException
	 * @throws CertificateException
	 */
	private static Map<String, Object> getQuery() {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put(VOMSAttributeDao.ISSUER_DN, ISSUER_DN);
		query.put(VOMSAttributeDao.SUBJECT_DN, SUBJECT_DN);
		return query;
	}

	private static Map<String, List<Object>> getMultivaluedAttributeQuery() {
		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put(VOMSAttributeDao.ISSUER_DN, Arrays.<Object> asList(ISSUER_DN));
		query.put(VOMSAttributeDao.SUBJECT_DN,
				Arrays.<Object> asList(SUBJECT_DN));
		return query;
	}

	private static Map<String, List<Object>> getMultivaluedAttributeQueryBad() {
		Map<String, List<Object>> query = new HashMap<String, List<Object>>();
		query.put(VOMSAttributeDao.ISSUER_DN,
				Arrays.<Object> asList(ISSUER_DN, ISSUER_DN));
		query.put(VOMSAttributeDao.SUBJECT_DN,
				Arrays.<Object> asList(SUBJECT_DN));
		return query;
	}

	public void testMultipleRolesAndGroups() {
		final List<String> roles = Arrays.asList(new String[] { "role1",
				"role2" });
		final List<String> groups = Arrays.asList(new String[] { "group1",
				"group2" });

		AttributeFetcher fetcher = new AttributeFetcher() {

			public UserAttributes fetchAttributes(UserInfo userInfo) {
				List<AttributeValue> attributeValues = Collections
						.<AttributeValue> emptyList();
				return new UserAttributes(Role.toRoles(roles),
						Group.toGroups(groups), attributeValues);
			}
		};
		VOMSAttributeDao resolver = new VOMSAttributeDao(fetcher);

		Set<IPersonAttributes> result = resolver.getPeople(getQuery());
		assertEquals(1, result.size());
		IPersonAttributes person = result.iterator().next();
		Map<String, Object> requiredValues = new HashMap<String, Object>();
		requiredValues.put(VOMSAttributeDao.VOMS_ROLES, roles);
		requiredValues.put(VOMSAttributeDao.VOMS_GROUPS, groups);
		for (Entry<String, Object> entry : requiredValues.entrySet()) {
			Object expected = entry.getValue();
			Object actual = person.getAttributeValues(entry.getKey());
			assertEquals(actual, expected);
		}
	}

	public void testGetPeopleWithMultivaluedAttributes() {
		final List<String> roles = Arrays.asList(new String[] { "role1",
				"role2" });
		final List<String> groups = Arrays.asList(new String[] { "group1",
				"group2" });

		AttributeFetcher fetcher = new AttributeFetcher() {

			public UserAttributes fetchAttributes(UserInfo userInfo) {
				List<AttributeValue> attributeValues = Collections
						.<AttributeValue> emptyList();
				return new UserAttributes(Role.toRoles(roles),
						Group.toGroups(groups), attributeValues);
			}
		};
		VOMSAttributeDao resolver = new VOMSAttributeDao(fetcher);

		// First test that we fail to get a result when we have two issuer dns
		Set<IPersonAttributes> result = resolver
				.getPeopleWithMultivaluedAttributes(getMultivaluedAttributeQueryBad());
		assertNull(result);

		// Check that we do get a result when there is exactly one issuer dn and
		// exactly one subject dn
		result = resolver
				.getPeopleWithMultivaluedAttributes(getMultivaluedAttributeQuery());
		assertEquals(1, result.size());
		IPersonAttributes person = result.iterator().next();
		Map<String, Object> requiredValues = new HashMap<String, Object>();
		requiredValues.put(VOMSAttributeDao.VOMS_ROLES, roles);
		requiredValues.put(VOMSAttributeDao.VOMS_GROUPS, groups);
		for (Entry<String, Object> entry : requiredValues.entrySet()) {
			Object expected = entry.getValue();
			Object actual = person.getAttributeValues(entry.getKey());
			assertEquals(actual, expected);
		}
	}

	public void testGetPeopleBadTypesInMap() {

		final Map<String, Object> attrMap = new HashMap<String, Object>();
		attrMap.put(VOMSAttributeDao.SUBJECT_DN, new Integer(1));
		attrMap.put(VOMSAttributeDao.ISSUER_DN, new Integer(5));

		VOMSAttributeDao resolver = new VOMSAttributeDao(null);
		assertNull(resolver.getPeople(attrMap));

	}

	public void testGetPeopleInvalidNameException() {

		final Map<String, Object> attrMap = new HashMap<String, Object>();
		attrMap.put(VOMSAttributeDao.SUBJECT_DN, "foo,bar");
		attrMap.put(VOMSAttributeDao.ISSUER_DN, "baz,quux");

		VOMSAttributeDao resolver = new VOMSAttributeDao(null);

		// We can't easily just test for InvalidNameException.
		// We could get null here for lots of reasons, you'd need to check
		// with a debugger or code coverage to know for sure where this bails
		// and returns null.
		assertNull(resolver.getPeople(attrMap));

	}

	public void testGetPeopleMissingAttributes() {
		Map<String, Object> attrMap = null;
		final VOMSAttributeDao resolver = new VOMSAttributeDao(null);

		// Try with null SUBJECT_DN
		attrMap = new HashMap<String, Object>();
		attrMap.put(VOMSAttributeDao.ISSUER_DN, "issuerdn");
		assertNull(resolver.getPeople(attrMap));

		// Try with null ISSEUR_DN
		attrMap = new HashMap<String, Object>();
		attrMap.put(VOMSAttributeDao.SUBJECT_DN, "subject dn");
		assertNull(resolver.getPeople(attrMap));

	}

	public void testGetPossibleUserAttributeNamesIsNonEmpty() {
		VOMSAttributeDao resolver = new VOMSAttributeDao(null);
		Set<String> attrNames = resolver.getPossibleUserAttributeNames();
		assertNotNull(attrNames);
		assertFalse(attrNames.equals(Collections.emptySet()));
	}

	public void testGetAvailableQueryAttributes() {
		VOMSAttributeDao resolver = new VOMSAttributeDao(null);
		Set<String> attrs = resolver.getAvailableQueryAttributes();
		assertNotNull(attrs);
		assertFalse(attrs.equals(Collections.emptySet()));
	}

}
