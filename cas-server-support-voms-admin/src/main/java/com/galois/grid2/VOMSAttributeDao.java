/**
 * 
 */
package com.galois.grid2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.collections.SetUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AttributeNamedPersonImpl;
import org.jasig.services.persondir.support.BasePersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galois.grid2.voms.AttributeFetchException;
import com.galois.grid2.voms.AttributeFetcher;
import com.galois.grid2.voms.Group;
import com.galois.grid2.voms.Role;
import com.galois.grid2.voms.UserAttributes;
import com.galois.grid2.voms.UserInfo;

/**
 * @author j3h
 * 
 */
public class VOMSAttributeDao extends BasePersonAttributeDao {

	// Attribute names
	public static final String ISSUER_DN = "issuer-dn";
	public static final String SUBJECT_DN = "subject-dn";
	public static final String VOMS_GROUPS = "voms-group";
	public static final String VOMS_ROLES = "voms-role";

	private static final Set<String> VOMS_QUERY_ATTRIBUTES = buildVOMSQueryAttribues();
	private static final Set<String> VOMS_RESPONSE_ATTRIBUTES = buildVOMSResponseAttributes();

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final AttributeFetcher attributeFetcher;

	public VOMSAttributeDao(AttributeFetcher attributeFetcher) {
		super();
		this.attributeFetcher = attributeFetcher;
	}

	// Static initializer for the set of possible response attributes
	@SuppressWarnings("unchecked")
	private static Set<String> buildVOMSResponseAttributes() {
		Set<String> attrs = new HashSet<String>();
		attrs.add(VOMS_GROUPS);
		attrs.add(VOMS_ROLES);
		return (Set<String>) SetUtils.unmodifiableSet(attrs);
	}

	// Static initializer for the set of valid query attributes
	@SuppressWarnings("unchecked")
	private static Set<String> buildVOMSQueryAttribues() {
		Set<String> attrs = new HashSet<String>();
		attrs.add(SUBJECT_DN);
		attrs.add(ISSUER_DN);
		return (Set<String>) SetUtils.unmodifiableSet(attrs);
	}

	public Set<IPersonAttributes> getPeople(Map<String, Object> attrsIn) {
		UserInfo userInfo;
		try {
			userInfo = extractUserInfo(attrsIn);
		} catch (InvalidNameException e) {
			log.error("Error extracting user info", e);
			return null;
		}
		
		if (userInfo == null) {
			return null;
		}
		
		UserAttributes vomsUserData;
		try {
			vomsUserData = attributeFetcher.fetchAttributes(userInfo);
		} catch (AttributeFetchException e) {
			return null;
		}
		// XXX: What happens if we query for a user that doesn't exist in VOMS?
		// In that case, we should probably return an empty set.
		return Collections.singleton(convertVOMSAttributes(userInfo,
				vomsUserData));
	}

	/**
	 * Convert VOMS UserAttributes into IPersonAttributes.
	 * 
	 * @param vomsUserData
	 *            The user data returned from VOMS about a particular user.
	 * @return
	 */
	private static IPersonAttributes convertVOMSAttributes(UserInfo userInfo,
			UserAttributes vomsUserData) {
		final Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
		final List<Object> groups = new ArrayList<Object>(
				Group.toStrings(vomsUserData.getGroups()));
		attributes.put(VOMS_GROUPS, groups);
		final List<Object> roles = new ArrayList<Object>(
				Role.toStrings(vomsUserData.getRoles()));
		attributes.put(VOMS_ROLES, roles);

		// TODO: Figure out a sane way to convert VOMS Attributes.

		return new AttributeNamedPersonImpl(attributes);
	}

	/**
	 * Build the {@link UserInfo} that's needed to query VOMS from a generic
	 * query.
	 * 
	 * @param query
	 *            The generic query. This query must have the "issuerDN" and
	 *            "subjectDN" fields set to those values, respectively.
	 * @return a {@link UserInfo} object to use to query VOMS, or null if the
	 *         information in the query was not sufficient to build the
	 *         {@link UserInfo}.
	 * @throws InvalidNameException
	 */
	private static UserInfo extractUserInfo(Map<String, Object> query)
			throws InvalidNameException {
		final String subjectDN;
		final String issuerDN;
		try {
			subjectDN = (String) query.get(SUBJECT_DN);
			issuerDN = (String) query.get(ISSUER_DN);
		} catch (ClassCastException e) {
			return null;
		}
		if (subjectDN == null || issuerDN == null) {
			return null;
		}
		return new UserInfo(new LdapName(subjectDN), new LdapName(issuerDN));
	}

	/*
	 * The only valid query is one with a subject DN and issuer DN that both
	 * have exactly one String value.
	 */
	public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
			Map<String, List<Object>> query) {
		List<Object> issuerDN = query.get(ISSUER_DN);
		List<Object> subjectDN = query.get(SUBJECT_DN);
		if (issuerDN == null || subjectDN == null || issuerDN.size() != 1
				|| subjectDN.size() != 1) {
			return null;
		}
		HashMap<String, Object> singleQuery = new HashMap<String, Object>();
		singleQuery.put(ISSUER_DN, issuerDN.get(0));
		singleQuery.put(SUBJECT_DN, subjectDN.get(0));
		return getPeople(singleQuery);
	}

	/*
	 * Since VOMS does not identify users by a single username, there is no
	 * reasonable implementation of this method aside from returning null,
	 * indicating that the user does not exist.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jasig.services.persondir.IPersonAttributeDao#getPerson(java.lang.
	 * String)
	 */
	public IPersonAttributes getPerson(String unused_identifier) {
		return null;
	}

	public Set<String> getPossibleUserAttributeNames() {
		// TODO: Allow for attributes. What this means depends on the way that
		// we decide to add them in the convertVOMSAttributes implementation.
		return VOMS_RESPONSE_ATTRIBUTES;
	}

	public Set<String> getAvailableQueryAttributes() {
		return VOMS_QUERY_ATTRIBUTES;
	}
}
