package com.galois.grid2;

import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * A 3-tuple containing the configuration information for an identity namespace.
 * This class exists for ease of configuration.
 * 
 * To use, create a sequence of NamespaceDefinitions and call
 * buildAttributeConfig to get a value usable in the constructor for
 * {@link AccountLinkingCredentialsToPrincipalResolver}.
 * 
 * @author j3h
 * 
 */
public class NamespaceDefinition {

	private final String namespace;

	private final RemoteNameConverter converter;

	private final IPersonAttributeDao dao;

	public NamespaceDefinition(String namespace,
			RemoteNameConverter converter, IPersonAttributeDao dao) {
		super();
		this.namespace = namespace;
		this.converter = converter;
		this.dao = dao;
	}

	public NamespaceDefinition(String namespace, RemoteNameConverter converter) {
		this(namespace, converter, null);
	}

	public String getNamespace() {
		return namespace;
	}

	public RemoteNameConverter getConverter() {
		return converter;
	}

	public IPersonAttributeDao getDao() {
		return dao;
	}
}
