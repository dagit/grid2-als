package com.galois.grid2;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.galois.grid2.converters.X509RemoteName;
import com.galois.grid2.converters.x509.DNDisplayResolver;

import junit.framework.TestCase;

public class X509RemoteNameTest extends TestCase {

	public void testX509RemoteName() throws InvalidNameException {
		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn = new X509RemoteName(subject, issuer);
		assertEquals(subject, rn.getSubjectDN());
		assertEquals(issuer, rn.getIssuerDN());
	}

	public void testGetDNDisplayResolver() throws InvalidNameException {
		final List<LabeledField> sentinel = new ArrayList<LabeledField>();

		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn = new X509RemoteName(subject, issuer);
		DNDisplayResolver dnDisplayResolver = new DNDisplayResolver() {
			public List<LabeledField> dnToFields(LdapName dn) {
				return sentinel;
			}
		};
		rn.setDNDisplayResolver(dnDisplayResolver);
		assertEquals(dnDisplayResolver, rn.getDNDisplayResolver());

		assertTrue(sentinel == rn.getUserFields());
		assertTrue(sentinel == rn.getAuthorityFields());
	}

	public void testFromCertificate() throws InvalidNameException {
		String issuerDN = "CN=foo";
		String subjectDN = "CN=bar";
		X509Certificate cert = new BogusX509Certificate(subjectDN, issuerDN);

		X509RemoteName result = X509RemoteName.fromCertificate(cert);
		assertEquals(result, new X509RemoteName(new LdapName(subjectDN),
				new LdapName(issuerDN)));
	}

	public void testFromCertificateInvalidName() {
		String issuerDN = "invalid";
		String subjectDN = "CN=bar";
		X509Certificate cert = new BogusX509Certificate(subjectDN, issuerDN);

		try {
			X509RemoteName.fromCertificate(cert);
			fail("Expected RuntimeException");
		} catch (RuntimeException e) {
			// Pass.
		}
	}

	public void testFromAttributeMap() throws InvalidNameException {
		String subjectDN = "CN=subject";
		String issuerDN = "CN=issuer";
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(X509RemoteName.SUBJECT_DN, subjectDN);
		attrs.put(X509RemoteName.ISSUER_DN, issuerDN);

		X509RemoteName result = X509RemoteName.fromAttributeMap(attrs);
		assertEquals(result, new X509RemoteName(new LdapName(subjectDN),
				new LdapName(issuerDN)));
	}

	public void testFromAttributeMapFailures() {
		assertInvalidAttributeMap(Collections.<String, String> emptyMap());
		assertInvalidAttributeMap(Collections.singletonMap(
				X509RemoteName.SUBJECT_DN, "CN=subject"));
		assertInvalidAttributeMap(Collections.singletonMap(
				X509RemoteName.ISSUER_DN, "CN=issuer"));

		String subjectDN = "invalid";
		String issuerDN = "invalid";
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(X509RemoteName.SUBJECT_DN, subjectDN);
		attrs.put(X509RemoteName.ISSUER_DN, issuerDN);

		assertInvalidAttributeMap(attrs);
	}

	private void assertInvalidAttributeMap(Map<String, String> badMap) {
		try {
			X509RemoteName.fromAttributeMap(badMap);
			fail("Expected InvalidNameException");
		} catch (InvalidNameException e) {
			// Pass.
		}
	}

	public void testGetAttributeMap() throws InvalidNameException {
		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn = new X509RemoteName(subject, issuer);
		
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(X509RemoteName.SUBJECT_DN, "CN=foo");
		attrs.put(X509RemoteName.ISSUER_DN, "CN=bar");
		
		assertEquals(attrs, rn.getAttributeMap());
	}

	public void testGetUserFields() throws InvalidNameException {
		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn = new X509RemoteName(subject, issuer);

		List<LabeledField> userFields = rn.getUserFields();
		assertEquals(1, userFields.size());
		assertEquals(new LabeledField("CN", "foo"), userFields.get(0));
	}

	public void testGetAuthorityFields() throws InvalidNameException {
		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn = new X509RemoteName(subject, issuer);

		List<LabeledField> authorityFields = rn.getAuthorityFields();
		assertEquals(1, authorityFields.size());
		assertEquals(new LabeledField("CN", "bar"), authorityFields.get(0));
	}

	public void testEqualsObject() throws InvalidNameException {
		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn1 = new X509RemoteName(subject, issuer);
		X509RemoteName rn2 = new X509RemoteName(subject, issuer);

		assertEquals(rn1, rn2);
	}

	public void testRoundTrip() throws InvalidNameException {
		LdapName subject = new LdapName("CN=foo");
		LdapName issuer = new LdapName("CN=bar");
		X509RemoteName rn = new X509RemoteName(subject, issuer);

		assertEquals(rn, X509RemoteName.fromAttributeMap(rn.getAttributeMap()));
	}
}
