package com.galois.grid2.persondir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AttributeNamedPersonImpl;
import org.jasig.services.persondir.support.IUsernameAttributeProvider;
import org.jasig.services.persondir.support.NamedPersonImpl;

public class AttributeTranslatingDao extends
		AbstractDefaultAttributePersonAttributeDao {
	private final String nameIn;
	private final String nameOut;
	private final Map<String, String> valueTransform;

	public AttributeTranslatingDao(String nameIn, String nameOut,
			Map<String, String> valueTransform) {
		super();
		this.nameIn = nameIn;
		this.nameOut = nameOut;
		this.valueTransform = valueTransform;
	}

	public Set<String> getAvailableQueryAttributes() {
		Set<String> names = new HashSet<String>();
		names.add(nameIn);
		names.add(this.getUsernameAttributeProvider().getUsernameAttribute());
		return names;
	}

	public String getNameOut() {
		return (nameOut == null ? nameIn : nameOut);
	}
	
	public String getNameIn() {
		return nameIn;
	}
	
	public Map<String, String> getValueTransform() {
		return Collections.unmodifiableMap(this.valueTransform);
	}

	public Map<String, List<Object>> transform(
			final Map<String, List<Object>> input) {
		final List<Object> inputValues = input.get(nameIn);
		List<Object> outputValues = null;
		if (inputValues != null) {
			if (valueTransform == null || valueTransform.isEmpty()) {
				// If there is no transform specified, then assume that no
				// mapping needs to occur for this attribute.
				outputValues = inputValues;
			} else {
				for (Object val : inputValues) {
					String valueOut = valueTransform.get(val);
					if (valueOut != null) {
						if (outputValues == null) {
							outputValues = new ArrayList<Object>();
						}
						outputValues.add(valueOut);
					}
				}
			}
		}
		if (outputValues == null) {
			return null;
		} else {
			return Collections.singletonMap(getNameOut(), outputValues);
		}
	}

	public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
			Map<String, List<Object>> query) {
		logger.debug("Using value mapping: " + valueTransform);
		Map<String, List<Object>> transformedAttributes = transform(query);

		// If the translation did not yield any attributes, just return null
		if (transformedAttributes == null || transformedAttributes.size() == 0) {
			return null;
		}

		// Create the person attributes object to return
		final IPersonAttributes personAttributes;
		final IUsernameAttributeProvider usernameAttributeProvider = this
				.getUsernameAttributeProvider();
		final String usernameFromQuery = usernameAttributeProvider
				.getUsernameFromQuery(query);
		if (usernameFromQuery != null) {
			personAttributes = new NamedPersonImpl(usernameFromQuery,
					transformedAttributes);
		} else {
			final String usernameAttribute = usernameAttributeProvider
					.getUsernameAttribute();
			personAttributes = new AttributeNamedPersonImpl(usernameAttribute,
					transformedAttributes);
		}

		return Collections.singleton(personAttributes);
	}

	public Set<String> getPossibleUserAttributeNames() {
		return Collections.singleton(getNameOut());
	}

}
