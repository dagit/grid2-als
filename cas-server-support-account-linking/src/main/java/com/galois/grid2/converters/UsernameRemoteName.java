package com.galois.grid2.converters;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;

import com.galois.grid2.LabeledField;
import com.galois.grid2.RemoteName;

public class UsernameRemoteName implements RemoteName, Serializable {

	public static final String USERNAME_LABEL = "Username";

	private static final long serialVersionUID = -1009178130358629267L;

	final String username;

	public static final String USERNAME = "username";

	public String getUsername() {
		return username;
	}

	public UsernameRemoteName(String username) {
		super();
		this.username = username;
	}

	public Map<String, String> getAttributeMap() {
		return Collections.<String, String> singletonMap(USERNAME, username);
	}

	public List<LabeledField> getUserFields() {
		return Collections.<LabeledField> singletonList(new LabeledField(
				USERNAME_LABEL, username));
	}

	public List<LabeledField> getAuthorityFields() {
		return Collections.emptyList();
	}

	public static RemoteName fromAttributes(Map<String, String> serialized)
			throws InvalidNameException {
		String username = serialized.get(USERNAME);
		if (username == null) {
			throw new InvalidNameException("Failed to find username: "
					+ username);
		}
		return new UsernameRemoteName(username);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
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
		UsernameRemoteName other = (UsernameRemoteName) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
