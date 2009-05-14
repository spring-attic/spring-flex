package flex.spring.samples.company;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
import flex.spring.samples.industry.IIndustryDAO;

@Service("companyService")
@RemotingDestination(channels={"my-amf"})
public class CompanyDAO implements ICompanyDAO {
	
	private final SimpleJdbcTemplate template;
	private final SimpleJdbcInsert insertCompany;

	private IIndustryDAO industryDAO;
	
	private final ParameterizedRowMapper<Company> rowMapper = new ParameterizedRowMapper<Company>(){
		public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
			Company company = new Company();
			company.setId(rs.getInt("id"));
			company.setName(rs.getString("name"));
			company.setAddress(rs.getString("address"));
			company.setCity(rs.getString("city"));
			company.setState(rs.getString("state"));
			company.setZip(rs.getString("zip"));
			company.setPhone(rs.getString("phone"));
			company.setIndustry(industryDAO.findById(rs.getInt("industry_id")));
			return company;
		}
	};
	
	@Autowired
	public CompanyDAO(DataSource dataSource, IIndustryDAO industryDAO) {
		template = new SimpleJdbcTemplate(dataSource);
		insertCompany = new SimpleJdbcInsert(dataSource).withTableName("COMPANY").usingGeneratedKeyColumns("ID");
		this.industryDAO = industryDAO;
	}

	@RemotingInclude
	public Company findById(int id) {
		return template.queryForObject("SELECT * FROM company WHERE id=?", rowMapper, id);
	}

	@RemotingInclude
	public List<Company> findAll() {
		return template.query("SELECT * FROM company ORDER BY name", rowMapper);
	}

	@RemotingInclude
	public List<Company> findByName(String name) {
		return template.query("SELECT * FROM company WHERE UPPER(name) LIKE ? ORDER BY name",
				rowMapper,
				"%" + name.toUpperCase() + "%");
	}
	
	@RemotingInclude
	public Company create(Company company) {
		Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", company.getName());
        parameters.put("address", company.getAddress());
        parameters.put("city", company.getCity());
        parameters.put("state", company.getState());
        parameters.put("zip", company.getZip());
        parameters.put("phone", company.getPhone());
        parameters.put("industry_id", company.getIndustry().getId());
        Number id = insertCompany.executeAndReturnKey(parameters);
		company.setId(id.intValue());
		return company;
	}
	
	@RemotingInclude
	public boolean update(Company company) {
        int count = template.update("UPDATE company SET name=?, address=?, city=?, state=?, zip=?, phone=?, industry_id=? WHERE id=?", 
				company.getName(),
				company.getAddress(),
				company.getCity(),
				company.getState(),
				company.getZip(),
				company.getPhone(),
				company.getIndustry().getId(),
				company.getId());
		return (count == 1);
	}

	@RemotingExclude
	public boolean remove(Company company) {
		int count = template.update("DELETE FROM company WHERE id=?", company.getId());
		return (count == 1);
	}
	
}
