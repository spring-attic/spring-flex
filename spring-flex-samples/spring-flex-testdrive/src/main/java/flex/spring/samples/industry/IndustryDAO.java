package flex.spring.samples.industry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service("industryService")
@RemotingDestination(channels={"my-amf"})
public class IndustryDAO implements IIndustryDAO {
	
	private final SimpleJdbcTemplate template;
	private final SimpleJdbcInsert insertIndustry;
	
	private final ParameterizedRowMapper<Industry> rowMapper = new ParameterizedRowMapper<Industry>(){
		public Industry mapRow(ResultSet rs, int rowNum) throws SQLException {
			Industry industry = new Industry();
			industry.setId(rs.getInt("id"));
			industry.setName(rs.getString("name"));
			return industry;
		}
	};

	public List<Industry> findAll() {
		return template.query("SELECT * FROM industry ORDER BY name", rowMapper);
	}

	public List<Industry> findByName(String name) {
		return template.query("SELECT * FROM industry WHERE UPPER(name) LIKE ? ORDER BY name",
				rowMapper,
				"%" + name.toUpperCase() + "%");
	}

	@Autowired
	public IndustryDAO(DataSource dataSource) {
		template = new SimpleJdbcTemplate(dataSource);
		insertIndustry = new SimpleJdbcInsert(dataSource).withTableName("INDUSTRY").usingGeneratedKeyColumns("ID");
	}

	public Industry findById(int id) {
		return template.queryForObject("SELECT * FROM industry WHERE id=?", rowMapper, id);
	}

	
	public Industry create(Industry industry) {
		Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", industry.getName());
        Number id = insertIndustry.executeAndReturnKey(parameters);
		industry.setId(id.intValue());
		return industry;
	}
	
	public boolean update(Industry industry) {
        int count = template.update("UPDATE industry SET name=? WHERE id=?", 
				industry.getName());
		return (count == 1);
	}

	public boolean remove(Industry industry) {
		int count = template.update("DELETE FROM industry WHERE id=?", industry.getId());
		return (count == 1);
	}
	
}
