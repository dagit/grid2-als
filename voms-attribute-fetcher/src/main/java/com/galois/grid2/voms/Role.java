package com.galois.grid2.voms;

import java.util.ArrayList;
import java.util.Collection;

public final class Role {
	private final String roleStr;

	public Role(String roleStr) {
		super();
		this.roleStr = roleStr;
	}

	public String getRoleStr() {
		return roleStr;
	}

	public static Collection<Role> toRoles(Collection<String> roleStrs) {
		ArrayList<Role> roles = new ArrayList<Role>();
		for (String roleStr : roleStrs) {
			roles.add(new Role(roleStr));
		}
		return roles;
	}

	public static Collection<String> toStrings(Collection<Role> roles) {
		ArrayList<String> roleStrs = new ArrayList<String>();
		for (Role role : roles) {
			roleStrs.add(role.getRoleStr());
		}
		return roleStrs;
	}

	@Override
	public String toString() {
		return "Role [roleStr=" + roleStr + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roleStr == null) ? 0 : roleStr.hashCode());
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
		Role other = (Role) obj;
		if (roleStr == null) {
			if (other.roleStr != null)
				return false;
		} else if (!roleStr.equals(other.roleStr))
			return false;
		return true;
	}
}
