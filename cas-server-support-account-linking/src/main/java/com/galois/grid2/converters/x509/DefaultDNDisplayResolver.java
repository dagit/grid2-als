package com.galois.grid2.converters.x509;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.galois.grid2.LabeledField;

/**
 * This resolver directly displays to the end user the text of the fields of the
 * DN (no transformation occurs).
 * 
 * @author j3h
 * 
 */
public class DefaultDNDisplayResolver implements DNDisplayResolver, Serializable {

	private static final long serialVersionUID = -6922316062329335493L;

	public List<LabeledField> dnToFields(LdapName dn) {
		ArrayList<LabeledField> fields = new ArrayList<LabeledField>();
		for (Rdn rdn : dn.getRdns()) {
			String k = rdn.getType();
			String v = rdn.getValue().toString();
			fields.add(new LabeledField(displayLabel(k), v));
		}
		Collections.reverse(fields);
		return fields;
	}

	/**
	 * This implementation just returns the input, but this is a natural
	 * extension point for displaying DNs.
	 * 
	 * @param rdnType
	 *            the name of the type of this component of the DN.
	 * @return a display name for this component.
	 */
	protected String displayLabel(String rdnType) {
		return rdnType;
	}
}