package com.galois.grid2.converters;

import java.io.Serializable;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.galois.grid2.LabeledField;
import com.galois.grid2.RemoteName;
import com.galois.grid2.converters.x509.DNDisplayResolver;
import com.galois.grid2.converters.x509.DefaultDNDisplayResolver;

/**
 * Remote name for X.509 credentials.
 * 
 * @author j3h
 * 
 */
public class X509RemoteName implements RemoteName, Serializable {

	private static final long serialVersionUID = 3283014953857679150L;
	public static final String SUBJECT_DN = "subject-dn";
	public static final String ISSUER_DN = "issuer-dn";

	private DNDisplayResolver dnDisplayResolver = null;

	private final LdapName subjectDN;
	private final LdapName issuerDN;

	public X509RemoteName(LdapName subjectDN, LdapName issuerDN) {
		super();
		this.subjectDN = subjectDN;
		this.issuerDN = issuerDN;
	}

	public DNDisplayResolver getDNDisplayResolver() {
		if (dnDisplayResolver == null) {
			return new DefaultDNDisplayResolver();
		} else {
			return dnDisplayResolver;
		}
	}

	public void setDNDisplayResolver(DNDisplayResolver dnDisplayResolver) {
		this.dnDisplayResolver = dnDisplayResolver;
	}

	public LdapName getSubjectDN() {
		return subjectDN;
	}

	public LdapName getIssuerDN() {
		return issuerDN;
	}

	/**
	 * Extract the subject DN and the issuer DN from this certificate to
	 * generate a RemoteName.
	 * 
	 * @param cert
	 *            The X.509 certificate used for authentication.
	 * @return the instantiated remote name.
	 * @throws RuntimeException
	 *             when the issuer or subject DN of the certificate is not
	 *             syntactically-valid. This should not happen during normal
	 *             operation.
	 */
	public static X509RemoteName fromCertificate(X509Certificate cert) {
		try {
			return new X509RemoteName(principalToLdapName(cert.getSubjectDN()),
					principalToLdapName(cert.getIssuerDN()));
		} catch (InvalidNameException e) {
			throw new RuntimeException(
					"Certificate issuer or subject not a valid DN", e);
		}
	}

	public static X509RemoteName fromAttributeMap(Map<String, String> attrs)
			throws InvalidNameException {
		return new X509RemoteName(getAttributeDN(attrs, SUBJECT_DN),
				getAttributeDN(attrs, ISSUER_DN));
	}

	public Map<String, String> getAttributeMap() {
		final HashMap<String, String> remoteName = new HashMap<String, String>();
		remoteName.put(ISSUER_DN, issuerDN.toString());
		remoteName.put(SUBJECT_DN, subjectDN.toString());
		return remoteName;
	}

	public List<LabeledField> getUserFields() {
		return getDNDisplayResolver().dnToFields(subjectDN);
	}

	public List<LabeledField> getAuthorityFields() {
		return getDNDisplayResolver().dnToFields(issuerDN);
	}

	private static LdapName getAttributeDN(Map<String, String> attrs, String key)
			throws InvalidNameException {
		String issuerStr = attrs.get(key);
		if (issuerStr == null) {
			throw new InvalidNameException("Missing key: " + key);
		}
		LdapName issuerDN = new LdapName(issuerStr);
		return issuerDN;
	}

	private static LdapName principalToLdapName(Principal p)
			throws InvalidNameException {
		return new LdapName(p.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((issuerDN == null) ? 0 : issuerDN.hashCode());
		result = prime * result
				+ ((subjectDN == null) ? 0 : subjectDN.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		X509RemoteName other = (X509RemoteName) obj;
		if (issuerDN == null) {
			if (other.issuerDN != null)
				return false;
		} else if (!issuerDN.equals(other.issuerDN))
			return false;
		if (subjectDN == null) {
			if (other.subjectDN != null)
				return false;
		} else if (!subjectDN.equals(other.subjectDN))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "X509RemoteName [subjectDN=" + subjectDN + ", issuerDN="
				+ issuerDN + "]";
	}

}
