package flex.spring.samples.contact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class ContactDAO implements IContactDAO {
	
	private final SimpleJdbcTemplate template;
	private final SimpleJdbcInsert insertContact;
	
	private final ParameterizedRowMapper<Contact> rowMapper = new ParameterizedRowMapper<Contact>(){
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
		template = new SimpleJdbcTemplate(dataSource);
		insertContact = new SimpleJdbcInsert(dataSource).withTableName("CONTACT").usingGeneratedKeyColumns("ID");
	}

	public List<Contact> findAll() {
		return template.query("SELECT * FROM contact ORDER BY first_name, last_name", rowMapper);
	}

	public List<Contact> findByName(String name) {
		return template.query("SELECT * FROM contact WHERE UPPER(CONCAT(first_name, ' ', last_name)) LIKE ? ORDER BY first_name, last_name",
				rowMapper,
				"%" + name.toUpperCase() + "%");
	}

	public Contact findById(int id) {
		return template.queryForObject("SELECT * FROM contact WHERE id=?", rowMapper, id);
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
        Number id = insertContact.executeAndReturnKey(parameters);
		contact.setId(id.intValue());
		return contact;
	}
	
	public boolean update(Contact contact) {
        int count = template.update("UPDATE contact SET first_name=?, last_name=?, address=?, city=?, state=?, zip=?, phone=?, email=? WHERE id=?", 
				contact.getFirstName(),
				contact.getLastName(),
				contact.getAddress(),
				contact.getCity(),
				contact.getState(),
				contact.getZip(),
				contact.getPhone(),
				contact.getEmail(),
				contact.getId());
		return (count == 1);
	}

	public boolean remove(Contact contact) {
		int count = template.update("DELETE FROM contact WHERE id=?", contact.getId());
		return (count == 1);
	}
	
}