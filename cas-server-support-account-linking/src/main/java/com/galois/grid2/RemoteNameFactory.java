package com.galois.grid2;

import java.util.Map;

import javax.naming.InvalidNameException;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Specifies how to build and store remote names.
 * 
 * @author j3h
 * 
 */
public interface RemoteNameFactory {

	public abstract Namespaced<RemoteName> convert(Credentials credentials);

	public abstract boolean supports(Credentials credentials);

	public abstract Namespaced<RemoteName> fromSerialized(String ns,
			Map<String, String> serialized) throws InvalidNameException;

}