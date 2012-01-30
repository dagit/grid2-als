package com.galois.grid2.converters.x509;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Convert DNs to a display representation using a lookup table for the field
 * names.
 * 
 * @author j3h
 * 
 */
public class MapDNDisplayResolver extends DefaultDNDisplayResolver {

	private static final long serialVersionUID = 1525636664305692458L;
	private static final String PROPERTIES_FILENAME = "x500-display-names.properties";
	private final Map<String, String> labelTable;

	public MapDNDisplayResolver(Map<String, String> labelTable) {
		super();
		this.labelTable = foldKeyCase(labelTable);
	}
	
	private static Map<String, String> foldKeyCase(Map<String, String> m) {
		final HashMap<String, String> folded = new HashMap<String, String>(m.size());
		for (Entry<String, String> e:m.entrySet()) {
			folded.put(e.getKey().toLowerCase(), e.getValue());
		}
		return folded;
	}

	protected String displayLabel(String label) {
		String display = labelTable.get(label.toLowerCase());
		if (display == null) {
			return label;
		} else {
			return display;
		}
	}

	public static MapDNDisplayResolver loadFromProperties() {
		URL propsURL = MapDNDisplayResolver.class
				.getResource("/" + PROPERTIES_FILENAME);
		if (propsURL == null) {
			throw new RuntimeException(
					"Failed to find DN display properties resource: "
							+ PROPERTIES_FILENAME);
		}
		return loadFromProperties(propsURL);
	}

	public static MapDNDisplayResolver loadFromProperties(String filename)
			throws MalformedURLException {
		return loadFromProperties(new URL("file://" + filename));
	}

	public static MapDNDisplayResolver loadFromProperties(URL propsURL) {
		Properties props = new Properties();
		try {
			InputStream stream = propsURL.openStream();
			props.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(
					"Error loading DN display properties file: " + propsURL, e);
		}
		Map<String, String> labelTable = new HashMap<String, String>();
		for (Entry<Object, Object> e : props.entrySet()) {
			try {
				final String k = (String) e.getKey();
				final String v = (String) e.getValue();
				labelTable.put(k, v);
			} catch (ClassCastException exc) {
				throw new RuntimeException(
						"Unexpected key or value in DN properties " + propsURL,
						exc);
			}
		}
		return new MapDNDisplayResolver(labelTable);
	}
}