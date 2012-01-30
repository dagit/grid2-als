/**
 * 
 */
package com.galois.grid2.voms;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisProperties;
import org.apache.axis.ConfigurationException;
import org.apache.axis.Handler;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.HTTPTransport;

/**
 * Simple configuration of Apache Axis for interacting with a single VOMS
 * server.
 * 
 */
public class SoapProvider extends SimpleProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.axis.configuration.SimpleProvider#configureEngine(org.apache
	 * .axis.AxisEngine)
	 */
	@Override
	public void configureEngine(AxisEngine engine)
			throws ConfigurationException {
		super.configureEngine(engine);
		AxisProperties.setProperty("axis.socketSecureFactory",
				"org.apache.axis.components.net.SunJSSESocketFactory");

		engine.refreshGlobalOptions();
	}

	/**
	 * Note that the handlerOptions are not namespaced. For example, the key for
	 * this Map to set the key store's password is "keystorePassword", rather
	 * than "javax.net.ssl.keystorePassword". These settings are passed directly
	 * to Axis.
	 * 
	 * See <a href=
	 * "http://download.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#Customization"
	 * >the JSSE Security Guide</a> for more information.
	 * 
	 * @param handlerOptions
	 *            A Map that supplies settings for the SSL socket factory for
	 *            configuring client and CA certificates.
	 */
	public SoapProvider(Map<String, Object> handlerOptions) {
		Hashtable<String, Object> opts = new Hashtable<String, Object>();
		this.setGlobalOptions(opts);
		Handler pivot = (Handler) new HTTPSender();
		for (Entry<String, Object> e : handlerOptions.entrySet()) {
			pivot.setOption(e.getKey(), e.getValue());
		}
		Handler transport = new SimpleTargetedChain(pivot);
		this.deployTransport(HTTPTransport.DEFAULT_TRANSPORT_NAME, transport);
	}

}
