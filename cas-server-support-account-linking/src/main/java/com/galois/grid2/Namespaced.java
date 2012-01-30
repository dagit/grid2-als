package com.galois.grid2;

import java.io.Serializable;

public class Namespaced<T> implements Serializable {
	private static final long serialVersionUID = -7607828200768749459L;

	private final T value;
	private final String namespace;

	public String getNamespace() {
		return namespace;
	}

	public T getValue() {
		return value;
	}

	public Namespaced(String namespace, T value) {
		super();
		this.value = value;
		this.namespace = namespace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Namespaced<?> other = (Namespaced<?>) obj;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Namespaced [value=" + value + ", namespace=" + namespace + "]";
	}
	
}
