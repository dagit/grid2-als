package com.galois.grid2.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import com.galois.grid2.actions.AuthenticationStore.AuthenticationHandle;

public class TicketRegistryAuthenticationStoreTest extends TestCase {
	private ExpirationPolicy tgtExpirationPolicy;
	private RequestContext context;
	private TicketRegistry registry;
	private String ticketId;
	private StubPersonAttributeDao dao;

	@Override
	public void setUp() {
		tgtExpirationPolicy = new NeverExpiresExpirationPolicy();
		context = new MockRequestContext();
		registry = new DefaultTicketRegistry();
		ticketId = "tgtId";
		dao = new StubPersonAttributeDao();
	}

	private void putTicketInContext() {
		context.getRequestScope().put("ticketGrantingTicketId", ticketId);
	}

	public void testGetAuthenticationHandleEmptyContext() {
		TicketRegistryAuthenticationStore store = new TicketRegistryAuthenticationStore(
				null, null, null);
		assertNull(store.getAuthenticationHandle(context));
	}

	public void testGetAuthenticationHandleNoAttributes() {
		// The attributes following conversion
		HashMap<String, Object> expectedAttributes = new HashMap<String, Object>();
		HashMap<String, List<Object>> daoAttributes = new HashMap<String, List<Object>>();

		String userId = "username";
		HashMap<String, Object> oldAttributes = new HashMap<String, Object>();
		oldAttributes.put("attr1", "some-value");

		assertAttributesUpdated(expectedAttributes, daoAttributes, userId,
				oldAttributes);
	}

	public void testGetAuthenticationHandleWithAttributes() {
		// The attributes following conversion
		HashMap<String, Object> expectedAttributes = new HashMap<String, Object>();
		expectedAttributes.put("foo", "bar");
		expectedAttributes.put("foos",
				Arrays.asList(new String[] { "ball", "table" }));
		HashMap<String, List<Object>> daoAttributes = new HashMap<String, List<Object>>();
		daoAttributes.put("foo", Collections.<Object> singletonList("bar"));
		daoAttributes.put("foos",
				Arrays.asList(new Object[] { "ball", "table" }));

		String userId = "username";
		HashMap<String, Object> oldAttributes = new HashMap<String, Object>();
		oldAttributes.put("attr1", "some-value");

		assertAttributesUpdated(expectedAttributes, daoAttributes, userId,
				oldAttributes);
	}

	private void assertAttributesUpdated(
			HashMap<String, Object> expectedAttributes,
			HashMap<String, List<Object>> daoAttributes, String userId,
			HashMap<String, Object> oldAttributes) {
		dao.setBackingMap(daoAttributes);
		TicketRegistryAuthenticationStore store = new TicketRegistryAuthenticationStore(
				registry, tgtExpirationPolicy, dao);

		putTicketInContext();
		final Authentication oldAuthentication = new ImmutableAuthentication(
				new SimplePrincipal(userId, oldAttributes));
		TicketGrantingTicketImpl ticket = new TicketGrantingTicketImpl(
				ticketId, oldAuthentication, tgtExpirationPolicy);
		registry.addTicket(ticket);

		AuthenticationHandle authenticationHandle = store
				.getAuthenticationHandle(context);
		assertNotNull(authenticationHandle);
		authenticationHandle.updateAttributes();

		// The ticket was replaced
		TicketGrantingTicket ticket2 = (TicketGrantingTicket) registry
				.getTicket(ticketId);
		assertTrue(ticket2 != ticket);

		// The new ticket has the expected attributes
		assertEquals(expectedAttributes, ticket2.getAuthentication()
				.getPrincipal().getAttributes());
	}

	public void testGetAuthenticationHandleNoPerson() {
		TicketRegistryAuthenticationStore store = new TicketRegistryAuthenticationStore(
				registry, null, dao);

		putTicketInContext();

		String userId = "username";
		final Authentication oldAuthentication = new ImmutableAuthentication(
				new SimplePrincipal(userId));
		TicketGrantingTicketImpl ticket = new TicketGrantingTicketImpl(
				ticketId, oldAuthentication, tgtExpirationPolicy);
		registry.addTicket(ticket);

		AuthenticationHandle authenticationHandle = store
				.getAuthenticationHandle(context);
		assertNotNull(authenticationHandle);
		assertEquals(userId, authenticationHandle.getLocalName());
		authenticationHandle.updateAttributes();

		// The ticket was not replaced
		assertTrue(registry.getTicket(ticketId) == ticket);
	}
}
