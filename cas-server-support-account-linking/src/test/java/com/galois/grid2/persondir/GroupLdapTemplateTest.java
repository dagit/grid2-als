package com.galois.grid2.persondir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import junit.framework.TestCase;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.SearchExecutor;

import com.mockobjects.naming.directory.MockDirContext;

public class GroupLdapTemplateTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDefaultGroupAttrName() {
		GroupLdapTemplate glt = new GroupLdapTemplate();
		assertEquals("cn", glt.getGroupAttrName());
	}

	public void testSetGroupAttrName() {
		GroupLdapTemplate glt = new GroupLdapTemplate();
		String expected = "foo";
		glt.setGroupAttrName(expected);
		assertEquals(expected, glt.getGroupAttrName());
	}

	public void testSearch() {
		// Create a fake ldap source that has multiple groups.
		// There should be:
		// * one user that is in at least 3 groups
		// * one user that is in exactly one group
		// * one user that is in no groups
		final Map<String, List<String>> store = new HashMap<String, List<String>>();
		final List<String> groups = Arrays.asList("group1", "group2", "group3");
		final String userA = "userA";
		final String userB = "userB";
		final String userC = "userC";

		store.put(userA, groups);
		store.put(userB, Collections.singletonList(groups.get(0)));
		store.put(userC, new ArrayList<String>());

		executeAndTestSearch(userA, store);
		executeAndTestSearch(userB, store);
		executeAndTestSearch(userC, store);
	}

	public void executeAndTestSearch(final String searchString,
			final Map<String, List<String>> store) {
		final GroupLdapTemplate glt = new GroupLdapTemplate();
		glt.setContextSource(new MapContextSource<String, List<String>>(store,
				glt.getGroupAttrName()));

		final SearchExecutor search = new SearchExecutor() {
			@SuppressWarnings("rawtypes")
			public NamingEnumeration executeSearch(DirContext ctx)
					throws NamingException {
				return ctx.search(searchString, "", null);
			}
		};

		glt.search(search, new NameClassPairCallbackHandler() {
			public void handleNameClassPair(NameClassPair arg0) {
				// We cast arg0 to SearchResult so we can call getAttributes(),
				// see
				// the comment below about assertEquals
				try {
					final SearchResult sr = (SearchResult) arg0;
					final Attributes expected = new BasicAttributes();
					final Attribute attr = new BasicAttribute(glt
							.getGroupAttrName());
					for (final String s : store.get(searchString)) {
						attr.add(s);
					}
					expected.put(attr);
					// For some reason we cannot directly compare two
					// SearchResults
					// that equality check will fail even when the attributes
					// are the same.
					// We are most interested in the returned attributes anyway,
					// so we test those.
					assertEquals(expected, sr.getAttributes());
				} catch (ClassCastException c) {
					assertTrue("Class cast failed", false);
				}
			}
		}, new NullDirContextProcessor());
	}

	public void testMissingAttr() {
		// Create a fake ldap source with a "cn" attribute but search for "missingAttr"
		final Map<String, List<String>> store = new HashMap<String, List<String>>();
		final List<String> groups = Arrays.asList("group1", "group2", "group3");
		final String userA = "userA";
		final String userB = "userB";
		final String userC = "userC";

		store.put(userA, groups);
		store.put(userB, Collections.singletonList(groups.get(0)));
		store.put(userC, new ArrayList<String>());
		final GroupLdapTemplate glt = new GroupLdapTemplate();
		glt.setContextSource(new MapContextSource<String, List<String>>(store,
				"missingAttr"));

		final SearchExecutor search = new SearchExecutor() {
			@SuppressWarnings("rawtypes")
			public NamingEnumeration executeSearch(DirContext ctx)
					throws NamingException {
				return ctx.search(userA, "", null);
			}
		};

		try {
			glt.search(search, new NameClassPairCallbackHandler() {
				public void handleNameClassPair(NameClassPair arg0) {
					// Do Nothing
					assertTrue(false);
				}
			}, new NullDirContextProcessor());
			assertTrue(false);
		} catch (UncategorizedLdapException e) {
			System.out.println(e);
			assertTrue(e.getCause() instanceof NamingException);
		}
	}

}

class NullDirContextProcessor implements DirContextProcessor {

	public void postProcess(DirContext arg0) throws NamingException {
		// Intentionally does nothing

	}

	public void preProcess(DirContext arg0) throws NamingException {
		// Intentionally does nothing

	}

}

@SuppressWarnings("unchecked")
class SimpleDirContext<K, V extends Iterable<?>> extends MockDirContext {
	private final Map<K, V> internalStore;
	private final String attrName;

	@Override
	public void close() {
		// Do Nothing
	}

	public SimpleDirContext(Map<K, V> map, String name) {
		internalStore = map;
		attrName = name;
	}

	@Override
	public NamingEnumeration<SearchResult> search(String aSearchName,
			String aFilter, SearchControls searchControls)
			throws NamingException {
		final MockNamingEnumeration<SearchResult> ne = new MockNamingEnumeration<SearchResult>();

		for (final Object v : internalStore.get(aSearchName)) {
			final Attributes attrs = new BasicAttributes();
			final Attribute a = new BasicAttribute(attrName, v);
			attrs.put(a);
			final SearchResult sr = new SearchResult(aSearchName, null, attrs);
			ne.add(sr);
		}
		return ne;
	}
}

class MockNamingEnumeration<V> implements NamingEnumeration<V> {
	private final List<V> list;

	MockNamingEnumeration() {
		list = new ArrayList<V>();
	}

	public boolean hasMoreElements() {
		return list.size() > 0;
	}

	public V nextElement() {
		return list.remove(0);
	}

	public void close() throws NamingException {
	}

	public boolean hasMore() throws NamingException {
		return list.size() > 0;
	}

	public V next() throws NamingException {
		return list.remove(0);
	}

	public void add(final V v) {
		list.add(v);
	}

}

// This class simply exists to return our custom mock dir context
class MapContextSource<K, V extends Iterable<?>> implements ContextSource {
	private final Map<K, V> internalStore;
	private final String attrName;

	public MapContextSource(Map<K, V> map, String attrName) {
		internalStore = map;
		this.attrName = attrName;
	}

	public DirContext getContext(String arg0, String arg1)
			throws org.springframework.ldap.NamingException {
		return new SimpleDirContext<K, V>(internalStore, attrName);
	}

	public DirContext getReadOnlyContext()
			throws org.springframework.ldap.NamingException {
		return new SimpleDirContext<K, V>(internalStore, attrName);
	}

	public DirContext getReadWriteContext()
			throws org.springframework.ldap.NamingException {
		return new SimpleDirContext<K, V>(internalStore, attrName);
	}

}