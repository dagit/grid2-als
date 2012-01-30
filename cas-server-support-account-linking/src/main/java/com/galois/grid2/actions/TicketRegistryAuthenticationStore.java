package com.galois.grid2.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.WebUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * Authentication store that uses the CAS ticket registry to produce
 * authentication handles.
 * 
 * @author j3h
 * 
 */
public class TicketRegistryAuthenticationStore implements AuthenticationStore {
	private final class TicketRegistryTicket implements AuthenticationHandle {
		private final TicketGrantingTicket oldTgt;

		private TicketRegistryTicket(TicketGrantingTicket oldTgt) {
			this.oldTgt = oldTgt;
		}

		public String getLocalName() {
			return oldTgt.getAuthentication().getPrincipal().getId();
		}

		/**
		 * Update a ticket-granting-ticket by replacing its attributes with
		 * those returned by a DAO.
		 */
		public void updateAttributes() {
			log.debug("Resolving a new principal for " + getLocalName());
			final IPersonAttributes person = dao.getPerson(getLocalName());
			if (person == null || person.getAttributes() == null) {
				log.error("Failed to resolve a new principal following account "
						+ "linking. Continuing with the old principal.");
				return;
			}

			final Principal newPrincipal = convertIPersonAttributesToPrincipal(person);

			// We are about to replace this TGT, so remove it entirely
			oldTgt.expire();
			ticketRegistry.deleteTicket(oldTgt.getId());

			// Build a new authentication from the old one, but change its
			// Principal.
			Authentication oldAuthentication = oldTgt.getAuthentication();
			Authentication newAuthentication = new ImmutableAuthentication(
					newPrincipal, oldAuthentication.getAttributes());

			// Create and store a new ticket-granting ticket using the new
			// authentication information.
			//
			// The new TGT has the same TGT id as the old one, so we don't need
			// to
			// update the RequestContext.
			TicketGrantingTicket newTgt = new TicketGrantingTicketImpl(
					oldTgt.getId(), newAuthentication, tgtExpirationPolicy);
			log.debug("A new principal was resolved. Replacing TGT: "
					+ newTgt.getId());
			ticketRegistry.addTicket(newTgt);

		}

		private Principal convertIPersonAttributesToPrincipal(
				final IPersonAttributes person) {
			final Map<String, Object> convertedAttributes = new HashMap<String, Object>();
			final Map<String, List<Object>> personAttrs = person
					.getAttributes();
			for (final Map.Entry<String, List<Object>> entry : personAttrs
					.entrySet()) {
				final String key = entry.getKey();
				final Object value = entry.getValue().size() == 1 ? entry
						.getValue().get(0) : entry.getValue();
				convertedAttributes.put(key, value);
			}
			final Principal newPrincipal = new SimplePrincipal(getLocalName(),
					convertedAttributes);
			return newPrincipal;
		}
	}

	private static final Logger log = LoggerFactory
			.getLogger(TicketRegistryAuthenticationStore.class);

	public TicketRegistryAuthenticationStore(TicketRegistry ticketRegistry,
			ExpirationPolicy tgtExpirationPolicy, IPersonAttributeDao dao) {
		super();
		this.ticketRegistry = ticketRegistry;
		this.tgtExpirationPolicy = tgtExpirationPolicy;
		this.dao = dao;
	}

	private final TicketRegistry ticketRegistry;
	private final ExpirationPolicy tgtExpirationPolicy;
	private final IPersonAttributeDao dao;

	public AuthenticationHandle getAuthenticationHandle(RequestContext context) {
		final String tgtId = WebUtils.getTicketGrantingTicketId(context);
		if (tgtId == null) {
			return null;
		}
		return new TicketRegistryTicket(
				(TicketGrantingTicket) ticketRegistry.getTicket(tgtId,
						TicketGrantingTicket.class));
	}
}