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

package org.springframework.flex.samples.company;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.flex.samples.industry.IIndustryDAO;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
@Service("companyService")
@RemotingDestination(channels = { "my-amf" })
public class CompanyDAO implements ICompanyDAO {

    private final SimpleJdbcTemplate template;

    private final SimpleJdbcInsert insertCompany;

    private final IIndustryDAO industryDAO;

    private final ParameterizedRowMapper<Company> rowMapper = new ParameterizedRowMapper<Company>() {

        public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
            Company company = new Company();
            company.setId(rs.getInt("id"));
            company.setName(rs.getString("name"));
            company.setAddress(rs.getString("address"));
            company.setCity(rs.getString("city"));
            company.setState(rs.getString("state"));
            company.setZip(rs.getString("zip"));
            company.setPhone(rs.getString("phone"));
            company.setIndustry(CompanyDAO.this.industryDAO.findById(rs.getInt("industry_id")));
            return company;
        }
    };

    @Autowired
    public CompanyDAO(DataSource dataSource, IIndustryDAO industryDAO) {
        this.template = new SimpleJdbcTemplate(dataSource);
        this.insertCompany = new SimpleJdbcInsert(dataSource).withTableName("COMPANY").usingGeneratedKeyColumns("ID");
        this.industryDAO = industryDAO;
    }

    @RemotingInclude
    public Company findById(int id) {
        return this.template.queryForObject("SELECT * FROM company WHERE id=?", this.rowMapper, id);
    }

    @RemotingInclude
    public List<Company> findAll() {
        return this.template.query("SELECT * FROM company ORDER BY name", this.rowMapper);
    }

    @RemotingInclude
    public List<Company> findByName(String name) {
        return this.template.query("SELECT * FROM company WHERE UPPER(name) LIKE ? ORDER BY name", this.rowMapper, "%" + name.toUpperCase() + "%");
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
        Number id = this.insertCompany.executeAndReturnKey(parameters);
        company.setId(id.intValue());
        return company;
    }

    @RemotingInclude
    public boolean update(Company company) {
        int count = this.template.update("UPDATE company SET name=?, address=?, city=?, state=?, zip=?, phone=?, industry_id=? WHERE id=?",
            company.getName(), company.getAddress(), company.getCity(), company.getState(), company.getZip(), company.getPhone(),
            company.getIndustry().getId(), company.getId());
        return count == 1;
    }

    @RemotingExclude
    public boolean remove(Company company) {
        int count = this.template.update("DELETE FROM company WHERE id=?", company.getId());
        return count == 1;
    }

}
