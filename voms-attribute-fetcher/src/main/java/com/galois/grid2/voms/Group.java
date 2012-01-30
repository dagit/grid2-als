package com.galois.grid2.voms;

import java.util.ArrayList;
import java.util.Collection;

public class Group {
	private final String groupStr;

	public Group(String groupStr) {
		super();
		this.groupStr = groupStr;
	}

	public String getGroupStr() {
		return groupStr;
	}

	@Override
	public String toString() {
		return "Group [groupStr=" + groupStr + "]";
	}

	public static Collection<Group> toGroups(Collection<String> groupStrs) {
		ArrayList<Group> groups = new ArrayList<Group>();
		for (String groupStr:groupStrs) {
			groups.add(new Group(groupStr));
		}
		return groups;
	}

	public static Collection<String> toStrings(Collection<Group> groups) {
		ArrayList<String> groupStrs = new ArrayList<String>();
		for (Group group:groups) {
			groupStrs.add(group.getGroupStr());
		}
		return groupStrs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupStr == null) ? 0 : groupStr.hashCode());
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
		Group other = (Group) obj;
		if (groupStr == null) {
			if (other.groupStr != null)
				return false;
		} else if (!groupStr.equals(other.groupStr))
			return false;
		return true;
	}
}
