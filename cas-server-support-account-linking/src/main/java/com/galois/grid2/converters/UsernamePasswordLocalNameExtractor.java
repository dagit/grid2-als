package com.galois.grid2.converters;

import com.galois.grid2.LocalNameExtractor;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteName;

public class UsernamePasswordLocalNameExtractor implements LocalNameExtractor {
	public String getLocalName(Namespaced<RemoteName> nsName) {
		final RemoteName name = nsName.getValue();
		if (name instanceof UsernameRemoteName) {
			return ((UsernameRemoteName)name).getUsername();
		} else {
			return null;
		}
	}
}
