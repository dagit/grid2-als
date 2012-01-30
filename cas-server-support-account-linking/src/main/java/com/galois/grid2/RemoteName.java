package com.galois.grid2;

import java.util.List;
import java.util.Map;

public interface RemoteName {
	public Map<String, String> getAttributeMap();
	public List<LabeledField> getUserFields();
	public List<LabeledField> getAuthorityFields();
}
