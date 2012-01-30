package com.galois.grid2;

import java.util.Map;

import javax.naming.InvalidNameException;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Implementation of RemoteNameFactory.
 * 
 * This implementation selects the first converter in the map that supports the
 * specified credentials.
 * 
 * @author j3h
 * 
 */
public class MapRemoteNameFactory implements RemoteNameFactory {

	private final Map<String, RemoteNameConverter> converters;

	public MapRemoteNameFactory(Map<String, RemoteNameConverter> converters) {
		super();
		this.converters = converters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.galois.grid2.NamespacedConverterSet#convert(org.jasig.cas.authentication
	 * .principal.Credentials)
	 */
	public Namespaced<RemoteName> convert(Credentials credentials) {
		for (Map.Entry<String, RemoteNameConverter> cvt : converters.entrySet()) {
			final RemoteNameConverter converter = cvt.getValue();
			if (converter.supportsCredentials(credentials)) {
				RemoteName converted = converter.fromCredentials(credentials);
				if (converted == null) {
					return null;
				} else {
					return new Namespaced<RemoteName>(cvt.getKey(), converted);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.galois.grid2.NamespacedConverterSet#supports(org.jasig.cas.authentication
	 * .principal.Credentials)
	 */
	public boolean supports(Credentials credentials) {
		for (Map.Entry<String, RemoteNameConverter> cvt : converters.entrySet()) {
			if (cvt.getValue().supportsCredentials(credentials)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Look up the converter for the namespace and use that to select a
	 * deserializer for the dictionary.
	 * 
	 * @see com.galois.grid2.NamespacedConverterSet#fromSerialized(com.galois.grid2
	 *      .Namespaced)
	 */
	public Namespaced<RemoteName> fromSerialized(String ns,
			Map<String, String> serialized) throws InvalidNameException {
		final RemoteNameConverter converter = this.converters.get(ns);
		if (converter == null) {
			throw new InvalidNameException("Unknown namespace " + ns
					+ " when deserializing " + serialized);
		}
		RemoteName converted = converter.fromSerialized(serialized);
		return new Namespaced<RemoteName>(ns, converted);
	}

}
