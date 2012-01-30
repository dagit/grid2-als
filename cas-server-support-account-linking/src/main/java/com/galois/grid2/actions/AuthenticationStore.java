package com.galois.grid2.actions;

import org.springframework.webflow.execution.RequestContext;

/**
 * The interface that MapUnmappedRemoteNameAction uses to interact with the
 * existing authentication state.
 * 
 * @author j3h
 */
public interface AuthenticationStore {
	/**
	 * Handle to a particular authentication.
	 * 
	 * @author j3h
	 * 
	 */
	public interface AuthenticationHandle {
		/**
		 * 
		 * @return The identifier for the entity for this authentication.
		 */
		String getLocalName();

		/**
		 * Perform the necessary effects to refresh the set of attributes for
		 * this ticket's principal.
		 */
		void updateAttributes();
	}

	/**
	 * Use settings in the request context to build an authentication handle.
	 * 
	 * @param context
	 *            The current request's context.
	 * @return An authentication handle that allows for updating the
	 *         authentication's attributes, or null if no authentication could
	 *         be extracted from the context.
	 */
	AuthenticationHandle getAuthenticationHandle(RequestContext context);
}