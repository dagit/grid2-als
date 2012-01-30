package com.galois.grid2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.InvalidNameException;

import org.jasig.cas.authentication.principal.Credentials;

import com.galois.grid2.LabeledField;
import com.galois.grid2.RemoteName;

public class GenericRemoteName implements RemoteName, Credentials {

	private static final long serialVersionUID = -802678842259823100L;

	public static class ConverterSet implements RemoteNameFactory {
		public Namespaced<RemoteName> convert(Credentials credentials) {
			throw new RuntimeException("Not implemented");
		}

		public boolean supports(Credentials credentials) {
			throw new RuntimeException("Not implemented");
		}

		public Namespaced<RemoteName> fromSerialized(String ns,
				Map<String, String> serialized) throws InvalidNameException {
			return new Namespaced<RemoteName>(ns, new GenericRemoteName(
					serialized));
		}
	}

	private final Map<String, String> attrs;

	public GenericRemoteName(Map<String, String> attrs) {
		super();
		this.attrs = attrs;
	}

	public Map<String, String> getAttributeMap() {
		return attrs;
	}

	public List<LabeledField> getUserFields() {
		List<LabeledField> fields = new ArrayList<LabeledField>();
		for (Entry<String, String> e:attrs.entrySet()) {
			fields.add(new LabeledField(e.getKey(), e.getValue()));
		}
		return fields;
	}

	public List<LabeledField> getAuthorityFields() {
		return Collections.emptyList();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericRemoteName other = (GenericRemoteName) obj;
		if (attrs == null) {
			if (other.attrs != null)
				return false;
		} else if (!attrs.equals(other.attrs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GenericRemoteName [attrs=" + attrs + "]";
	}

}