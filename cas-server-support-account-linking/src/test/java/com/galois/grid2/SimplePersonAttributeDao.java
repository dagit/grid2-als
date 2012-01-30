package com.galois.grid2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

public class SimplePersonAttributeDao implements IPersonAttributeDao {

    private final Set<IPersonAttributes> people;

    public SimplePersonAttributeDao(Set<IPersonAttributes> people) {
        this.people = people;
    }

    public Set<String> getAvailableQueryAttributes() {
        throw new RuntimeException("Not implemented");
    }

    public Map<String, List<Object>> getMultivaluedUserAttributes(
            Map<String, List<Object>> arg0) {
        throw new RuntimeException("Not implemented");
    }

    public Map<String, List<Object>> getMultivaluedUserAttributes(String arg0) { // $codepro.audit.disable overloadedMethods
        throw new RuntimeException("Not implemented");
    }

    public Set<IPersonAttributes> getPeople(Map<String, Object> arg0) {
        return people;
    }

    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
            Map<String, List<Object>> arg0) {
        throw new RuntimeException("Not implemented");
    }

    public IPersonAttributes getPerson(String arg0) {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> getPossibleUserAttributeNames() {
        Set<String> result = new HashSet<String>();
        
        for (IPersonAttributes p : people) {
        	result.addAll(p.getAttributes().keySet());
        }
        
		return result;
    }

    public Map<String, Object> getUserAttributes(Map<String, Object> arg0) {
        throw new RuntimeException("Not implemented");
    }

    public Map<String, Object> getUserAttributes(String arg0) { // $codepro.audit.disable overloadedMethods
        throw new RuntimeException("Not implemented");
    }
}