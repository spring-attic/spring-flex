package org.springframework.flex.integration.service;

import org.springframework.flex.integration.domain.Person;

public interface PersonService {
	
	Person loadPerson(Integer id);

	
	Person loadPersonAndChildren(Integer id);
}
