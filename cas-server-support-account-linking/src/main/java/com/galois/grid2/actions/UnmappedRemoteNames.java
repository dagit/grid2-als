package com.galois.grid2.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;

/**
 * Set of unmapped remote names. This collection can be added to or iterated
 * over, but does not support the general collection interface.
 * 
 * This class is used for storing unmapped remote names during the process of a
 * user logging in. Once the user successfully logs in, then these remote names
 * can be linked. In other words, these are the identifiers for the successful
 * authentications that are bound together in this login process.
 */
public class UnmappedRemoteNames implements Serializable,
		Iterable<Namespaced<RemoteName>> {

	private static final long serialVersionUID = 6614120662039206464L;
	private final List<Namespaced<RemoteName>> names;

	public UnmappedRemoteNames() {
		this.names = new ArrayList<Namespaced<RemoteName>>();
	}

	public void add(Namespaced<RemoteName> remoteName) {
		this.names.add(remoteName);
	}

	public boolean contains(Namespaced<RemoteName> remoteName) {
		return this.names.contains(remoteName);
	}

	public Iterator<Namespaced<RemoteName>> iterator() {
		return names.iterator();
	}

	public boolean isEmpty() {
		return names.size() == 0;
	}

	@Override
	public String toString() {
		return "UnmappedRemoteNames [names=" + names + "]";
	}

}