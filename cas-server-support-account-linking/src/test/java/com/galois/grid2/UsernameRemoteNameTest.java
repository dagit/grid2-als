package com.galois.grid2;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;

import com.galois.grid2.converters.UsernameRemoteName;

import junit.framework.TestCase;

public class UsernameRemoteNameTest extends TestCase {

	public void testUsernameRemoteName() {
		String username = "user";
		assertEquals(username, new UsernameRemoteName(username).getUsername());
	}

	public void testGetAttributeMap() {
		String username = "user";
		UsernameRemoteName u = new UsernameRemoteName(username);
		Map<String, String> result = u.getAttributeMap();
		assertEquals(1, result.size());
		assertEquals(username, result.get(UsernameRemoteName.USERNAME));
	}

	public void testGetUserFields() {
		String username = "user";
		UsernameRemoteName u = new UsernameRemoteName(username);
		List<LabeledField> result = u.getUserFields();
		assertEquals(1, result.size());
		assertEquals(UsernameRemoteName.USERNAME_LABEL, result.get(0)
				.getLabel());
		assertEquals(username, result.get(0).getValue());
	}

	public void testGetAuthorityFields() {
		UsernameRemoteName u = new UsernameRemoteName("user");
		assertTrue(u.getAuthorityFields().isEmpty());
	}

	public void testFromAttributesSuccess() throws InvalidNameException {
		String username = "user";
		Map<String, String> attributes = Collections.singletonMap(
				UsernameRemoteName.USERNAME, username);
		assertEquals(new UsernameRemoteName(username),
				UsernameRemoteName.fromAttributes(attributes));
	}

	public void testFromAttributesFailure() {
		String username = "user";
		Map<String, String> attributes = Collections.singletonMap(
				"not-a-username", username);
		try {
			UsernameRemoteName.fromAttributes(attributes);
			fail("Expected InvalidNameException");
		} catch (InvalidNameException e) {
			// Pass.
		}
	}

	public void testRoundTrip() throws InvalidNameException {
		String username = "user";
		UsernameRemoteName u = new UsernameRemoteName(username);
		assertEquals(u, UsernameRemoteName.fromAttributes(u.getAttributeMap()));
	}

	public void testEquals() {
		String username = "user";
		assertEquals(new UsernameRemoteName(username), new UsernameRemoteName(
				username));
	}
}
