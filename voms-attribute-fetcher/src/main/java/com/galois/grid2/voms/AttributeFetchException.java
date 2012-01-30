package com.galois.grid2.voms;

/**
 * Wrapper for exceptions from interacting with VOMS in a generic wrapper, since
 * they will all need to be handled in the same way at runtime.
 */
public class AttributeFetchException extends Exception {

	private static final long serialVersionUID = -2296918745861699653L;

	public AttributeFetchException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributeFetchException(String message) {
		super(message);
	}

	public AttributeFetchException(Throwable cause) {
		super(cause);
	}
}
