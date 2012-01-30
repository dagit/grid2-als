package com.galois.grid2.voms.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.galois.grid2.voms.AttributeFetchException;
import com.galois.grid2.voms.AttributeFetcher;
import com.galois.grid2.voms.Group;
import com.galois.grid2.voms.Role;
import com.galois.grid2.voms.SOAPAttributeFetcher;
import com.galois.grid2.voms.SoapProvider;
import com.galois.grid2.voms.UserAttributes;
import com.galois.grid2.voms.UserInfo;
import com.galois.grid2.voms.soap.AttributeValue;

public class VOMSAttributeClient {
	public static void usage() {
		System.out.println("Usage: VOMSAttributeClient <properties file name>");
		System.out
				.println("The properties file will contain the information about what\n"
						+ "VOMS server to contact and how to authenticate to it\n"
						+ "(client and CA certificates)\n"
						+ "For example:\n\n"
						+ "vomsAttrEndpoint=https://grid2-vo-test.dev.galois.com:8443/voms/grid2vo/services/VOMSAttributes\n"
						+ "vomsServiceEndpoint=https://grid2-vo-test.dev.galois.com:8443/voms/grid2vo/services/VOMSAdmin\n"
						+ "userDN=/O=Grid/OU=GlobusTest/OU=simpleCA-grid2-vo-test.dev.galois.com/OU=dev.galois.com/CN=Identity Service Testing\n"
						+ "caDN=/O=Grid/OU=GlobusTest/OU=simpleCA-grid2-vo-test.dev.galois.com/CN=Globus Simple CA\n"
						+ "keystore=test.jks\n"
						+ "clientauth=true\n"
						+ "keystorePassword=idservtest\n"
						+ "keypass=idservtest\n"
						+ "keystoreType=JKS\n\n"
						+ "See gen_keystore.sh to generate a keystore.");
	}

	private static class PropertiesWrapper extends Properties {

		private static final long serialVersionUID = 3595541056214934341L;

		@Override
		public String getProperty(String name) {
			String val = super.getProperty(name);
			if (val == null) {
				throw new RuntimeException("Property not found: " + name);
			}
			return val;
		}

	}

	public static void main(String[] args) throws AttributeFetchException,
			FileNotFoundException, IOException, InvalidNameException {
		if (args.length != 1) {
			usage();
			System.out.println("\nArgs:");
			for (int i = 0; i < args.length; i++) {
				System.out.println("Arg " + i + ": " + args[i]);
			}
			System.exit(1);
		}
		Properties p = new PropertiesWrapper();
		String settingsFileName = args[0];
		p.load(new FileInputStream(settingsFileName));

		String vomsAttrEndpoint = p.getProperty("vomsAttrEndpoint");
		String vomsServiceEndpoint = p.getProperty("vomsServiceEndpoint");
		HashMap<String, Object> h = new HashMap<String, Object>();
		for (Entry<Object, Object> e : p.entrySet()) {
			h.put((String) e.getKey(), e.getValue());
		}
		SoapProvider provider = new SoapProvider(h);
		AttributeFetcher fetcher = new SOAPAttributeFetcher(provider,
				vomsAttrEndpoint, vomsServiceEndpoint);

		LdapName userDN = new LdapName(p.getProperty("userDN"));
		LdapName caDN = new LdapName(p.getProperty("caDN"));

		UserAttributes attributes = fetcher.fetchAttributes(new UserInfo(
				userDN, caDN));

		Collection<AttributeValue> attrs = attributes.getAttributes();

		if (attrs == null) {
			System.out.println("No user attributes found.");
		} else {
			for (AttributeValue attr : attrs) {
				System.out.println("Attr:");
				System.out.println("  class: "
						+ attr.getAttributeClass().getName());
				System.out.println("  context: " + attr.getContext());
				System.out.println("  value: " + attr.getValue());
			}
		}

		Collection<Group> groups = attributes.getGroups();
		if (groups == null) {
			System.out.println("No user groups found.");
		} else {
			System.out.println("Groups:");
			for (Group group : groups) {
				System.out.println("  " + group);
			}
		}

		Collection<Role> roles = attributes.getRoles();

		if (roles == null) {
			System.out.println("No user roles found.");
		} else {
			System.out.println("Roles:");
			for (Role role : roles) {
				System.out.println("  " + role);
			}
		}
	}
}