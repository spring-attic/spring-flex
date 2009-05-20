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

package org.springframework.flex.samples.industry;

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

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
@Service("industryService")
@RemotingDestination(channels = { "my-amf" })
public class IndustryDAO implements IIndustryDAO {

    private final SimpleJdbcTemplate template;

    private final SimpleJdbcInsert insertIndustry;

    private final ParameterizedRowMapper<Industry> rowMapper = new ParameterizedRowMapper<Industry>() {

        public Industry mapRow(ResultSet rs, int rowNum) throws SQLException {
            Industry industry = new Industry();
            industry.setId(rs.getInt("id"));
            industry.setName(rs.getString("name"));
            return industry;
        }
    };

    public List<Industry> findAll() {
        return this.template.query("SELECT * FROM industry ORDER BY name", this.rowMapper);
    }

    public List<Industry> findByName(String name) {
        return this.template.query("SELECT * FROM industry WHERE UPPER(name) LIKE ? ORDER BY name", this.rowMapper, "%" + name.toUpperCase() + "%");
    }

    @Autowired
    public IndustryDAO(DataSource dataSource) {
        this.template = new SimpleJdbcTemplate(dataSource);
        this.insertIndustry = new SimpleJdbcInsert(dataSource).withTableName("INDUSTRY").usingGeneratedKeyColumns("ID");
    }

    public Industry findById(int id) {
        return this.template.queryForObject("SELECT * FROM industry WHERE id=?", this.rowMapper, id);
    }

    public Industry create(Industry industry) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", industry.getName());
        Number id = this.insertIndustry.executeAndReturnKey(parameters);
        industry.setId(id.intValue());
        return industry;
    }

    public boolean update(Industry industry) {
        int count = this.template.update("UPDATE industry SET name=? WHERE id=?", industry.getName());
        return count == 1;
    }

    public boolean remove(Industry industry) {
        int count = this.template.update("DELETE FROM industry WHERE id=?", industry.getId());
        return count == 1;
    }

}
