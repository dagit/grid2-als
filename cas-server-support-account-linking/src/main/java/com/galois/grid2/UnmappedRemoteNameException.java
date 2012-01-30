package com.galois.grid2;


/**
 * This exception indicates that authentication succeeded for credentials that
 * we know how to handle, but no local name has yet been set for the remote
 * name.
 * 
 */
public class UnmappedRemoteNameException extends RuntimeException {
	private final Namespaced<RemoteName> remoteName;

	public UnmappedRemoteNameException(Namespaced<RemoteName> remoteName) {
		this.remoteName = remoteName;
	}

	public Namespaced<RemoteName> getRemoteName() {
		return remoteName;
	}

	@Override
	public String toString() {
		return "UnmappedRemoteNameException [remoteName=" + remoteName + "]";
	}

	private static final long serialVersionUID = 2443341690064116491L;

}
