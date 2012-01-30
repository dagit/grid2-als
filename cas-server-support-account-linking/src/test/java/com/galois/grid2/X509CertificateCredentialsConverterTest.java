package com.galois.grid2;

import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.authentication.principal.Credentials;

import com.galois.grid2.converters.X509CertificateCredentialsConverter;
import com.galois.grid2.converters.X509RemoteName;

public class X509CertificateCredentialsConverterTest extends TestCase {
	private final RemoteNameConverter converter;

	public X509CertificateCredentialsConverterTest() {
		super();
		converter = new X509CertificateCredentialsConverter();
	}

	@SuppressWarnings("serial")
	public void testSupports() {
		assertFalse(converter.supportsCredentials(new Credentials() {
		}));

		final X509CertificateCredentials credentials = new X509CertificateCredentials(
				null);
		assertTrue(converter.supportsCredentials(credentials));
	}

	public void testConversion() {
		final String issuerDN = "CN=My CA";
		final String subjectDN = "CN=Me";
		final X509CertificateCredentials credentials = new X509CertificateCredentials(
				null);
		final X509Certificate bogusX509Certificate = new BogusX509Certificate(
				subjectDN, issuerDN);
		credentials.setCertificate(bogusX509Certificate);

		final RemoteName expected = X509RemoteName.fromCertificate(bogusX509Certificate);
		assertEquals(converter.fromCredentials(credentials), expected);
	}
}
