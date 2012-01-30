package com.galois.grid2.converters.x509;

import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.galois.grid2.LabeledField;

import junit.framework.TestCase;

public class MapDNDisplayResolverTest extends TestCase {
	public void testDNToFields() throws InvalidNameException {
		String cnDisplay = "Common Name";
		String cnValue = "Josh Hoyt";
		DNDisplayResolver resolver = new MapDNDisplayResolver(
				Collections.singletonMap("CN", cnDisplay));
		List<LabeledField> result = resolver.dnToFields(new LdapName("cn="
				+ cnValue + ", unexpected=field"));
		assertEquals(2, result.size());
		assertEquals(cnDisplay, result.get(0).getLabel());
		assertEquals(cnValue, result.get(0).getValue());
		assertEquals("unexpected", result.get(1).getLabel());
		assertEquals("field", result.get(1).getValue());
	}

	public void testLoadFromProperties() throws InvalidNameException {
		DNDisplayResolver resolver = MapDNDisplayResolver.loadFromProperties();
		List<LabeledField> result = resolver.dnToFields(new LdapName(
				"cn=Josh Hoyt,o=Galois"));
		assertEquals(2, result.size());
		assertEquals("Common Name", result.get(0).getLabel());
		assertEquals("Josh Hoyt", result.get(0).getValue());
		assertEquals("Organization", result.get(1).getLabel());
		assertEquals("Galois", result.get(1).getValue());
	}
}
