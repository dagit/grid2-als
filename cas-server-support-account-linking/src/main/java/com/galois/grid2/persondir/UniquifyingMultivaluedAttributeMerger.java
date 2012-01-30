package com.galois.grid2.persondir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;

/**
 * {@link MultivaluedAttributeMerger} that removes duplicate values.
 * 
 * @author j3h
 * 
 */
public final class UniquifyingMultivaluedAttributeMerger extends
		MultivaluedAttributeMerger {
	@Override
	protected Map<String, List<Object>> mergePersonAttributes(
			Map<String, List<Object>> toModify,
			Map<String, List<Object>> toConsider) {
		return uniquifyAttributes(super.mergePersonAttributes(toModify, toConsider));
	}

	static Map<String, List<Object>> uniquifyAttributes(
			Map<String, List<Object>> intermediateResult) {
		Map<String, List<Object>> result = new HashMap<String, List<Object>>();
		for (Entry<String, List<Object>> entry : intermediateResult.entrySet()) {
			result.put(entry.getKey(), uniquify(entry.getValue()));
		}
		return result;
	}

	static List<Object> uniquify(List<Object> values) {
		return new ArrayList<Object>(new LinkedHashSet<Object>(values));
	}
}