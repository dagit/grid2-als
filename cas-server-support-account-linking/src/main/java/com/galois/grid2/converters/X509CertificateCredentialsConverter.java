package com.galois.grid2.converters;

import java.util.Map;

import javax.naming.InvalidNameException;

import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.authentication.principal.Credentials;

import com.galois.grid2.RemoteNameConverter;
import com.galois.grid2.RemoteName;
import com.galois.grid2.converters.x509.DNDisplayResolver;
import com.galois.grid2.converters.x509.MapDNDisplayResolver;

/**
 * RemoteNameConverter that builds a remote name out of the certificate issuer
 * DN and the subject DN.
 * 
 * This converter depends on the trustworthiness of the issuer DN, which will
 * depend on the full certificate chain. Whether it is reasonable to trust the
 * issuer is a matter of policy for the PKI that you are using. You will have to
 * implement your own converter if you wish to take the full chain of signing
 * certificates into account.
 * 
 * @author cygnus
 * 
 */
public class X509CertificateCredentialsConverter implements RemoteNameConverter {
	private final DNDisplayResolver displayResolver;

	public X509CertificateCredentialsConverter() {
		this(MapDNDisplayResolver.loadFromProperties());
	}

	public X509CertificateCredentialsConverter(DNDisplayResolver displayResolver) {
		super();
		this.displayResolver = displayResolver;
	}

	public DNDisplayResolver getDisplayResolver() {
		return displayResolver;
	}

	protected X509RemoteName processRemoteName(X509RemoteName rn) {
		rn.setDNDisplayResolver(displayResolver);
		return rn;
	}

	public RemoteName fromCredentials(Credentials credentials) {
		final X509CertificateCredentials x509Credentials = (X509CertificateCredentials) credentials;
		return processRemoteName(X509RemoteName.fromCertificate(x509Credentials
				.getCertificate()));
	}

	public boolean supportsCredentials(Credentials credentials) {
		return credentials instanceof X509CertificateCredentials;
	}

	public RemoteName fromSerialized(Map<String, String> serialized)
			throws InvalidNameException {
		return processRemoteName(X509RemoteName.fromAttributeMap(serialized));
	}

}
