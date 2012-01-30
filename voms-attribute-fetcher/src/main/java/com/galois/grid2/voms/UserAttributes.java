/**
 * 
 */
package com.galois.grid2.voms;

import java.util.Collection;
import java.util.Collections;

import com.galois.grid2.voms.soap.AttributeValue;

/**
 * @author cygnus
 * 
 */
public class UserAttributes {

	private Collection<Role> roles;
	private Collection<Group> groups;
	private Collection<AttributeValue> attributes;

	public UserAttributes(Collection<Role> roles, Collection<Group> groups,
			Collection<AttributeValue> attrs) {
		super();
		this.roles = Collections.unmodifiableCollection(roles);
		this.groups = Collections.unmodifiableCollection(groups);
		this.attributes = Collections.unmodifiableCollection(attrs);
	}

	public UserAttributes() {
		this(Collections.<Role> emptyList(),
				Collections.<Group> emptyList(),
				Collections.<AttributeValue> emptyList());
	}

	public Collection<AttributeValue> getAttributes() {
		return this.attributes;
	}

	public Collection<Role> getRoles() {
		return this.roles;
	}

	public Collection<Group> getGroups() {
		return this.groups;
	}
}
