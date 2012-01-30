package com.galois.grid2;

import java.io.Serializable;

/**
 * A pair of strings explicitly marked for display to an end user. The label
 * describes the value.
 * 
 * @author j3h
 * 
 */
public class LabeledField implements Serializable {
	private static final long serialVersionUID = -4014147212467845649L;
	private final String label;
	private final String val;

	public LabeledField(String key, String val) {
		super();
		this.label = key;
		this.val = val;
	}

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((val == null) ? 0 : val.hashCode());
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
		LabeledField other = (LabeledField) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (val == null) {
			if (other.val != null)
				return false;
		} else if (!val.equals(other.val))
			return false;
		return true;
	}

}