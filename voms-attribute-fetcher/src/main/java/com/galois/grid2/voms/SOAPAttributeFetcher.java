package com.galois.grid2.voms;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.axis.EngineConfiguration;

import com.galois.grid2.voms.soap.AttributeValue;
import com.galois.grid2.voms.soap.User;
import com.galois.grid2.voms.soap.VOMSAdminServiceLocator;
import com.galois.grid2.voms.soap.VOMSAdmin_PortType;
import com.galois.grid2.voms.soap.VOMSAttributesServiceLocator;
import com.galois.grid2.voms.soap.VOMSAttributes_PortType;
import com.galois.grid2.voms.soap.VOMSException;

/**
 * VOMS-Admin 2.X SOAP API implementation of the AttributeFetcher.
 */
public class SOAPAttributeFetcher implements AttributeFetcher {

	public static final String DEFAULT_VOMS_ADMIN_PATH = "/VOMSAdmin";
	public static final String DEFAULT_VOMS_ATTRIBUTES_PATH = "/VOMSAttributes";
	private final EngineConfiguration soapProvider;
	private final String vomsAttributeServiceEndpoint;
	private final String vomsAdminServiceEndpoint;

	/**
	 * 
	 * @param soapProvider
	 *            The SOAP configuration for contacting the VOMS-Admin server.
	 * @param vomsAttributeServiceEndpoint
	 *            The URL for VOMS-Admin's SOAP Attribute Service
	 * @param vomsAdminServiceEndpoint
	 *            The URL for VOMS-Admin's SOAP Admin Service
	 */
	public SOAPAttributeFetcher(EngineConfiguration soapProvider,
			String vomsAttributeServiceEndpoint, String vomsAdminServiceEndpoint) {
		super();
		this.soapProvider = soapProvider;
		this.vomsAttributeServiceEndpoint = vomsAttributeServiceEndpoint;
		this.vomsAdminServiceEndpoint = vomsAdminServiceEndpoint;
	}

	/**
	 * Factory method for succinctly creating instances that use the default
	 * settings to interface with VOMS-Admin via the SOAP API.
	 * 
	 * @param handlerOptions
	 *            Options that are used to configure Axis, primarily useful for
	 *            configuring TLS client certificates and CA certificates.
	 * @param vomsAttrEndpoint
	 * @param vomsServiceEndpoint
	 * @return An instance of {@link SOAPAttributeFetcher} with this
	 *         configuration.
	 */
	public static SOAPAttributeFetcher defaultConfig(
			Map<String, Object> handlerOptions, String vomsAttrEndpoint,
			String vomsServiceEndpoint) {
		SoapProvider provider = new SoapProvider(handlerOptions);
		return new SOAPAttributeFetcher(provider, vomsAttrEndpoint,
				vomsServiceEndpoint);
	}

	/**
	 * Factory method for succinctly creating instances that use the default
	 * settings to interface with VOMS-Admin via the SOAP API. This method
	 * assumes the installation of VOMS that will be contacted has its SOAP
	 * endpoints at standard locations.
	 * 
	 * This method is a convenience wrapper around the more complex factory.
	 * 
	 * @param handlerOptions
	 * @param vomsBase
	 *            The base URL for a standard VOMS server.
	 * @return An instance of {@link SOAPAttributeFetcher} with this
	 *         configuration.
	 */
	public static SOAPAttributeFetcher defaultConfig(
			Map<String, Object> handlerOptions, String vomsBase) {
		String vomsAttrEndpoint = vomsBase + DEFAULT_VOMS_ATTRIBUTES_PATH;
		String vomsServiceEndpoint = vomsBase + DEFAULT_VOMS_ADMIN_PATH;
		return defaultConfig(handlerOptions, vomsAttrEndpoint,
				vomsServiceEndpoint);
	}

	/**
	 * @param userInfo
	 *            The identifying information for the subject user.
	 * @return
	 * @throws AttributeFetchException
	 */
	public UserAttributes fetchAttributes(UserInfo userInfo)
			throws AttributeFetchException {
		User user = new User();
		user.setCA(userInfo.getSlashedIssuerDN());
		user.setDN(userInfo.getSlashedSubjectDN());

		Collection<AttributeValue> attrs;
		Collection<Role> roles;
		Collection<Group> groups;
		try {
			roles = this.fetchRoleList(user);
			attrs = this.fetchAttributeList(user);
			groups = this.fetchGroupList(user);
		} catch (VOMSException e) {
			throw new AttributeFetchException(e);
		} catch (RemoteException e) {
			throw new AttributeFetchException(e);
		} catch (ServiceException e) {
			throw new AttributeFetchException(e);
		}

		return new UserAttributes(roles, groups, attrs);
	}

	private Collection<AttributeValue> fetchAttributeList(User user)
			throws ServiceException, VOMSException, RemoteException {
		VOMSAttributes_PortType svc = this.getVomsAttributesService();
		return arrayToCollection(svc.listUserAttributes(user));
	}

	private Collection<Role> fetchRoleList(User user) throws ServiceException,
			VOMSException, RemoteException {
		VOMSAdmin_PortType svc = this.getVomsAdminService();
		String[] rawRoleArray = svc.listRoles(user.getDN(), user.getCA());
		Collection<String> roleStrs = arrayToCollection(rawRoleArray);
		return Role.toRoles(roleStrs);
	}

	private Collection<Group> fetchGroupList(User user)
			throws ServiceException, VOMSException, RemoteException {
		VOMSAdmin_PortType svc = this.getVomsAdminService();
		String[] rawGroupArray = svc.listGroups(user.getDN(), user.getCA());
		Collection<String> groupStrs = arrayToCollection(rawGroupArray);
		return Group.toGroups(groupStrs);
	}

	private static <A> Collection<A> arrayToCollection(A[] items) {
		if (items == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(items);
		}
	}

	private VOMSAttributes_PortType getVomsAttributesService()
			throws ServiceException {
		final VOMSAttributesServiceLocator service = new VOMSAttributesServiceLocator(
				this.soapProvider);
		service.setVOMSAttributesEndpointAddress(this.vomsAttributeServiceEndpoint);
		return service.getVOMSAttributes();
	}

	private VOMSAdmin_PortType getVomsAdminService() throws ServiceException {
		final VOMSAdminServiceLocator service = new VOMSAdminServiceLocator(
				this.soapProvider);
		service.setVOMSAdminEndpointAddress(this.vomsAdminServiceEndpoint);
		return service.getVOMSAdmin();
	}
}
