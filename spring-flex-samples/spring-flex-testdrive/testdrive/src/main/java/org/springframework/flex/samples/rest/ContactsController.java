package org.springframework.flex.samples.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.samples.contact.Contact;
import org.springframework.flex.samples.contact.IContactDAO;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

	@Autowired
	private IContactDAO contactDAO;

	@RequestMapping(method=RequestMethod.GET)
	public List<Contact> find(@RequestParam(required=false) String searchStr) {
		if (StringUtils.hasText(searchStr)) {
			return contactDAO.findByName(searchStr);
		} else {
			return contactDAO.findAll();
		}
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public Contact create(@RequestBody Contact contact) {
		return contactDAO.create(contact);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT)
	public void update(@PathVariable int id, @RequestBody Contact contact) {
		if (id != contact.getId()) {
			//Should actually set an appropriate status code here
			return;
		}
		contactDAO.update(contact);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public void delete(@PathVariable int id) {
		contactDAO.removeById(id);
	}
	
	public void setContactDAO(IContactDAO contactDAO) {
		this.contactDAO = contactDAO;
	}
}
