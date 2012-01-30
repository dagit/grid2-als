package com.galois.grid2.voms;

import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * The user information that's necessary to identify a user to VOMS-Admin.
 * 
 * @author j3h
 */
public final class UserInfo {
	private final LdapName subjectDN;
	private final LdapName issuerDN;

	/**
	 * @param subjectDN
	 *            The subject user's certificate DN, in slash-separated,
	 *            most-to-least significant order. For example:
	 *            /O=Example/CN=User
	 * @param issuerDN
	 *            The CA that issued the certificate's DN, in slash-separated,
	 *            most-to-least significant order. For example: /O=Example/CN=CA
	 *            001
	 */
	public UserInfo(LdapName subjectDN, LdapName issuerDN) {
		super();
		this.subjectDN = subjectDN;
		this.issuerDN = issuerDN;
	}

	/**
	 * Converts {@link LdapName}s (DNs) to a format expected by VOMS. The VOMS
	 * format uses <code>"/"</code> as a delimiter and lists the components of
	 * the DN ordered by increasing specificity, e.g. starting with O and ending
	 * with CN.
	 */
	private static String convertDNToSlashed(LdapName dn) {
		StringBuffer b = new StringBuffer();
		for (Rdn part : dn.getRdns()) {
			b.append('/');
			b.append(part.toString());
		}
		return b.toString();
	}

	/**
	 * Extract the user information from an X.509 certificate.
	 * 
	 * @param userCert
	 *            The user's X.509 certificate.
	 * @return UserInfo containing the relevant identity information from the
	 *         certificate.
	 * @throws InvalidNameException
	 */
	public static UserInfo fromCertificate(X509Certificate userCert)
			throws InvalidNameException {
		LdapName subjectDN = certDNtoLdapName(userCert.getSubjectDN());
		final LdapName issuerDN = certDNtoLdapName(userCert.getIssuerDN());
		return new UserInfo(subjectDN, issuerDN);
	}

	private static LdapName certDNtoLdapName(final Principal certIssuerDN)
			throws InvalidNameException {
		return new LdapName(certIssuerDN.getName());
	}

	/**
	 * @return The user's DN encoded from most significant component to least
	 *         significant component, delimited with slashes.
	 */
	public String getSlashedSubjectDN() {
		return convertDNToSlashed(subjectDN);
	}

	/**
	 * @return The DN of the CA that issued the user's certificate, encoded from
	 *         most significant component to least significant component,
	 *         delimited with slashes.
	 */
	public String getSlashedIssuerDN() {
		return convertDNToSlashed(issuerDN);
	}

	public LdapName getSubjectDN() {
		return subjectDN;
	}

	public LdapName getIssuerDN() {
		return issuerDN;
	}
}
