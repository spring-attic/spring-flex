/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.samples.contact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
public class ContactDAO implements IContactDAO {

    private final SimpleJdbcTemplate template;

    private final SimpleJdbcInsert insertContact;

    private final ParameterizedRowMapper<Contact> rowMapper = new ParameterizedRowMapper<Contact>() {

        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();
            contact.setId(rs.getInt("id"));
            contact.setFirstName(rs.getString("first_name"));
            contact.setLastName(rs.getString("last_name"));
            contact.setAddress(rs.getString("address"));
            contact.setCity(rs.getString("city"));
            contact.setState(rs.getString("state"));
            contact.setZip(rs.getString("zip"));
            contact.setPhone(rs.getString("phone"));
            contact.setEmail(rs.getString("email"));
            return contact;
        }
    };

    public ContactDAO(DataSource dataSource) {
        this.template = new SimpleJdbcTemplate(dataSource);
        this.insertContact = new SimpleJdbcInsert(dataSource).withTableName("CONTACT").usingGeneratedKeyColumns("ID");
    }

    public List<Contact> findAll() {
        return this.template.query("SELECT * FROM contact ORDER BY first_name, last_name", this.rowMapper);
    }

    public List<Contact> findByName(String name) {
        return this.template.query("SELECT * FROM contact WHERE UPPER(CONCAT(first_name, ' ', last_name)) LIKE ? ORDER BY first_name, last_name",
            this.rowMapper, "%" + name.toUpperCase() + "%");
    }

    public Contact findById(int id) {
        return this.template.queryForObject("SELECT * FROM contact WHERE id=?", this.rowMapper, id);
    }

    public Contact create(Contact contact) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("first_name", contact.getFirstName());
        parameters.put("last_name", contact.getLastName());
        parameters.put("address", contact.getAddress());
        parameters.put("city", contact.getCity());
        parameters.put("state", contact.getState());
        parameters.put("zip", contact.getZip());
        parameters.put("phone", contact.getPhone());
        parameters.put("email", contact.getEmail());
        Number id = this.insertContact.executeAndReturnKey(parameters);
        contact.setId(id.intValue());
        return contact;
    }

    public boolean update(Contact contact) {
        int count = this.template.update(
            "UPDATE contact SET first_name=?, last_name=?, address=?, city=?, state=?, zip=?, phone=?, email=? WHERE id=?", contact.getFirstName(),
            contact.getLastName(), contact.getAddress(), contact.getCity(), contact.getState(), contact.getZip(), contact.getPhone(),
            contact.getEmail(), contact.getId());
        return count == 1;
    }

    public boolean remove(Contact contact) {
        int count = this.template.update("DELETE FROM contact WHERE id=?", contact.getId());
        return count == 1;
    }

}
