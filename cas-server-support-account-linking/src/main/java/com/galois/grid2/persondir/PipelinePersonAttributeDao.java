package com.galois.grid2.persondir;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;

public class PipelinePersonAttributeDao extends
		AbstractDefaultAttributePersonAttributeDao {

	private final List<IPersonAttributeDao> daos;

	public PipelinePersonAttributeDao(List<IPersonAttributeDao> daos) {
		super();
		if (daos.isEmpty()) {
			throw new RuntimeException(getClass().getName()
					+ " requires a non-empty list of IPersonAttributeDaos");
		}
		this.daos = daos;
	}

	public List<IPersonAttributeDao> getDaos() {
		return Collections.unmodifiableList(this.daos);
	}
	
	public Set<String> getAvailableQueryAttributes() {
		return daos.get(0).getAvailableQueryAttributes();
	}

	public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
			Map<String, List<Object>> initialAttributes) {
		Set<IPersonAttributes> people = null;
		Set<Map<String, List<Object>>> attributesToProcess = Collections
				.singleton(initialAttributes);

		for (IPersonAttributeDao dao : daos) {
			people = new HashSet<IPersonAttributes>();
			for (Map<String, List<Object>> attributes : attributesToProcess) {
				logger.debug("Processing attributes with dao: dao=" + dao
						+ ", attributes=" + attributes);
				Set<IPersonAttributes> toAdd = dao
						.getPeopleWithMultivaluedAttributes(attributes);
				if (toAdd == null) {
					logger.debug("DAO returned null");
				} else {
					logger.debug("Adding all: " + toAdd);
					people.addAll(toAdd);
				}
			}

			attributesToProcess = new HashSet<Map<String, List<Object>>>();
			for (IPersonAttributes person : people) {
				logger.debug("Getting attributes for person: "
						+ person.getName());
				attributesToProcess.add(person.getAttributes());
			}
		}
		return people;
	}

	public Set<String> getPossibleUserAttributeNames() {
		return daos.get(daos.size() - 1).getPossibleUserAttributeNames();
	}

}
