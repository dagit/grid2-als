package com.galois.grid2.store;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.security.core.codec.Hex;
import org.springframework.util.StringUtils;


class AttributeEncoding {
	public static final String ENCODING = "UTF-8";

	private final byte[] hash;
	private final String normalized;

	public byte[] getHash() {
		return hash;
	}
	
	public String getHexHash() {
		return new String(Hex.encode(getHash()));
	}

	public String getNormalized() {
		return normalized;
	}

	public AttributeEncoding(Map<String, String> attributes)
			throws AttributeEncodingException {
		this(encode(attributes));
	}

	public AttributeEncoding(String normalized)
			throws AttributeEncodingException {
		this.normalized = normalized;
		this.hash = computeHash(this.normalized);
	}

	private static byte[] computeHash(String normalizedName)
			throws AttributeEncodingException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new AttributeEncodingException("Hashing failed", e);
		}

		try {
			md.update(normalizedName.getBytes(ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new AttributeEncodingException("Hashing failed", e);
		}
		return md.digest();
	}

	public static String encode(Map<String, String> attributes)
			throws AttributeEncodingException {
		final List<String> result = new ArrayList<String>();

		// Very important: Sort the keys
		// We need the keys to be sorted so that when they are concatenated
		// together they are concatenated in a consistent order.
		final Map<String, String> sortedAttrs = new TreeMap<String, String>(
				attributes);

		// URL encoding provides both escaping, as well as allowing us to
		// easily
		// invert the process. We additionally concatenate the pairs using
		// '&'
		for (final Entry<String, String> entry : sortedAttrs.entrySet()) {
			try {
				result.add(URLEncoder.encode(entry.getKey(), ENCODING)
						+ "="
						+ URLEncoder.encode(entry.getValue().toString(),
								ENCODING));
			} catch (UnsupportedEncodingException e) {
				throw new AttributeEncodingException(
						"Could not encode namespace attributes; encoding "
								+ ENCODING + " unsupported", e);
			}
		}

		return StringUtils.collectionToDelimitedString(result, "&");
	}

	public Map<String, String> decode() {
		final Map<String, String> attrMap = new HashMap<String, String>();

		for (final String pair : normalized.split("&")) {
			final String[] items = pair.split("=");

			if (items.length != 2) {
				return null;
			}

			final String key = items[0];
			final String value = items[1];

			try {
				attrMap.put(URLDecoder.decode(key, ENCODING),
						URLDecoder.decode(value, ENCODING));
			} catch (UnsupportedEncodingException e) {
				throw new AttributeEncodingException("Failed to decode", e);
			}
		}

		return attrMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result
				+ ((normalized == null) ? 0 : normalized.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AttributeEncoding)) {
			return false;
		}
		AttributeEncoding other = (AttributeEncoding) obj;
		if (hash == null) {
			if (other.hash != null) {
				return false;
			}
		} else if (!hash.equals(other.hash)) {
			return false;
		}
		if (normalized == null) {
			if (other.normalized != null) {
				return false;
			}
		} else if (!normalized.equals(other.normalized)) {
			return false;
		}
		return true;
	}

}