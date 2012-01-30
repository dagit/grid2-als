package com.galois.grid2.converters.x509;

import java.util.List;

import javax.naming.ldap.LdapName;

import com.galois.grid2.LabeledField;

/**
 * Defines how distinguished names from X.509 certificates should be converted
 * to labeled fields for display to the end user.
 * 
 * @author j3h
 * 
 */
public interface DNDisplayResolver {
	List<LabeledField> dnToFields(LdapName dn);
}