package com.galois.grid2.persondir;

import java.util.Collections;
import java.util.Enumeration;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.SearchExecutor;

/**
 * An LdapTemplate that supports querying an attribute across multiple LDAP
 * objects. For instance, membership in a group is often represented by a group
 * having a member attribute with the CN of the user. In that case, to get all
 * of the groups that a user is a member of, you must find all of the groups by
 * querying on their members. The resulting search results must then be merged
 * together to form a single attribute for the user containing all of the group
 * names.
 */
public class GroupLdapTemplate extends LdapTemplate {
	private String groupAttrName = "cn";
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The attribute of the LDAP objects in the result set that will get merged.
	 * 
	 * Defaults to "cn".
	 * 
	 * @return The current groupAttrName.
	 */
	public String getGroupAttrName() {
		return groupAttrName;
	}

	public void setGroupAttrName(String groupAttrName) {
		this.groupAttrName = groupAttrName;
	}

	@Override
	public void search(final SearchExecutor se,
			NameClassPairCallbackHandler handler, DirContextProcessor processor) {
		final SearchExecutor groupSE = new SearchExecutor() {

			public NamingEnumeration<SearchResult> executeSearch(DirContext ctx)
					throws NamingException {
			
				log.debug("Executing search");

				// Delegate to the other search executor for the initial search
				@SuppressWarnings("unchecked")
				NamingEnumeration<SearchResult> groupsEnum = se
						.executeSearch(ctx);

				// Merge the results into one element with multi-valued
				// attributes
				BasicAttribute groups = new BasicAttribute(groupAttrName);

				// Build the merged attributes
				while (groupsEnum.hasMoreElements()) {
					SearchResult result = groupsEnum.nextElement();
					log.debug("Processing next group query result: " + result);
					Attribute resultGroupAttr = result.getAttributes().get(
							groupAttrName);
					if( resultGroupAttr == null ) {
						log.error("Null Attributes while trying to retrieve attributes for " + groupAttrName);
						throw new NamingException();
					}
					NamingEnumeration<?> valuesEnum = resultGroupAttr.getAll();
					while (valuesEnum.hasMoreElements()) {
						Object next = valuesEnum.next();
						log.debug("Processing next group name attribute value: " + next);
						groups.add(next);
					}
				}

				Attributes attrs = new BasicAttributes();
				attrs.put(groups);

				SearchResult mergedResult = new SearchResult(null, null, attrs);

				final Enumeration<SearchResult> usersEnum = Collections
						.enumeration(Collections.singletonList(mergedResult));

				// Turn it into an enumeration
				NamingEnumeration<SearchResult> coalesced = new NamingEnumeration<SearchResult>() {
					public boolean hasMoreElements() {
						return usersEnum.hasMoreElements();
					}

					public SearchResult nextElement() {
						return usersEnum.nextElement();
					}

					public void close() throws NamingException {

					}

					public boolean hasMore() throws NamingException {
						return usersEnum.hasMoreElements();
					}

					public SearchResult next() throws NamingException {
						return usersEnum.nextElement();
					}

				};
				return coalesced;
			}

		};
		super.search(groupSE, handler, processor);
	}
}
