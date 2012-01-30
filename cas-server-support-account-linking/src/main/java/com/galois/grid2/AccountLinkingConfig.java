package com.galois.grid2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galois.grid2.persondir.UniquifyingMultivaluedAttributeMerger;

public class AccountLinkingConfig {
	private final String localAccountProvisioningURL;
	private final MapRemoteNameFactory converters;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	private final HashMap<String, IPersonAttributeDao> daos;
	private final IAttributeMerger merger;

	public AccountLinkingConfig(String localAccountProvisioningURL,
			List<NamespaceDefinition> namespaces, IAttributeMerger merger) {
		this.localAccountProvisioningURL = localAccountProvisioningURL;

		this.daos = new HashMap<String, IPersonAttributeDao>();

		Map<String, RemoteNameConverter> converterMap = new HashMap<String, RemoteNameConverter>();
		for (NamespaceDefinition def : namespaces) {
			converterMap.put(def.getNamespace(), def.getConverter());
			IPersonAttributeDao dao = def.getDao();
			if (dao != null) {
				daos.put(def.getNamespace(), dao);
			}
		}
		this.converters = new MapRemoteNameFactory(converterMap);
		this.merger = merger;
	}

	public AccountLinkingConfig(String localAccountProvisioningURL,
			List<NamespaceDefinition> namespaces) {
		this(localAccountProvisioningURL, namespaces, defaultAttributeMerger());
	}

	private static IAttributeMerger defaultAttributeMerger() {
		return new UniquifyingMultivaluedAttributeMerger();
	}

	public CredentialsToPrincipalResolver buildCredentialsToPrincipalResolver(
			MutableAccountLinkingStorage storage) {
		final AccountLinkingCredentialsToPrincipalResolver resolver = new AccountLinkingCredentialsToPrincipalResolver(
				converters, storage);
		resolver.setAttributeRepository(buildAttributeRepository(storage));
		return resolver;
	}

	public AccountLinkingDao buildAttributeRepository(
			MutableAccountLinkingStorage storage) {
		return new AccountLinkingDao(storage, daos, merger);
	}

	public String getLocalAccountProvisioningURL() {
		log.debug("Returning local account provisioning URL: "
				+ this.localAccountProvisioningURL);
		return localAccountProvisioningURL;
	}

	public RemoteNameFactory getConverters() {
		return converters;
	}
}
