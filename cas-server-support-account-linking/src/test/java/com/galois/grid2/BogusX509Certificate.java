package com.galois.grid2;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

final class BogusX509Certificate extends X509Certificate {

	public BogusX509Certificate(String subjectDN, String issuerDN) {
		super();
		this.subjectDN = subjectDN;
		this.issuerDN = issuerDN;
	}

	@Override
	public Principal getSubjectDN() {
		final String subjectDN = this.subjectDN;
		return new Principal() {
			public String getName() {
				return subjectDN;
			}
		};
	}

	@Override
	public Principal getIssuerDN() {
		final String issuerDN = this.issuerDN;
		return new Principal() {
			public String getName() {
				return issuerDN;
			}
		};
	}

	private final String subjectDN;
	private final String issuerDN;

	/***************************************************************************
	 * The remainder of this class is just here to implement the interface.
	 * Every remaining method raises an exception.
	 */

	private static final RuntimeException unusedException = new RuntimeException(
			"should not be used");

	public Set<String> getCriticalExtensionOIDs() {
		throw unusedException;
	}

	public byte[] getExtensionValue(String arg0) {
		throw unusedException;
	}

	public Set<String> getNonCriticalExtensionOIDs() {
		throw unusedException;
	}

	public boolean hasUnsupportedCriticalExtension() {
		throw unusedException;
	}

	@Override
	public void checkValidity() {
		throw unusedException;
	}

	@Override
	public void checkValidity(Date date) {
		throw unusedException;
	}

	@Override
	public int getBasicConstraints() {
		throw unusedException;
	}

	@Override
	public boolean[] getIssuerUniqueID() {
		throw unusedException;
	}

	@Override
	public boolean[] getKeyUsage() {
		throw unusedException;
	}

	@Override
	public Date getNotAfter() {
		throw unusedException;
	}

	@Override
	public Date getNotBefore() {
		throw unusedException;
	}

	@Override
	public BigInteger getSerialNumber() {
		throw unusedException;
	}

	@Override
	public String getSigAlgName() {
		throw unusedException;
	}

	@Override
	public String getSigAlgOID() {
		throw unusedException;
	}

	@Override
	public byte[] getSigAlgParams() {
		throw unusedException;
	}

	@Override
	public byte[] getSignature() {
		throw unusedException;
	}

	@Override
	public boolean[] getSubjectUniqueID() {
		throw unusedException;
	}

	@Override
	public byte[] getTBSCertificate() {
		throw unusedException;
	}

	@Override
	public int getVersion() {
		throw unusedException;
	}

	@Override
	public byte[] getEncoded() {
		throw unusedException;
	}

	@Override
	public PublicKey getPublicKey() {
		throw unusedException;
	}

	@Override
	public String toString() {
		throw unusedException;
	}

	@Override
	public void verify(PublicKey arg0) {
		throw unusedException;
	}

	@Override
	public void verify(PublicKey arg0, String arg1) {
		throw unusedException;
	}

}