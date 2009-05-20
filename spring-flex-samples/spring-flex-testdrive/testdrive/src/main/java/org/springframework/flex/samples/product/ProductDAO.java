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

package org.springframework.flex.samples.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
public class ProductDAO implements IProductDAO {

    private final DataSource dataSource;

    public ProductDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Product> findAll() {
        List<Product> list = new ArrayList<Product>();
        Connection c = null;

        try {
            c = this.dataSource.getConnection();
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM product ORDER BY name");
            while (rs.next()) {
                list.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("image"),
                    rs.getString("category"), rs.getDouble("price"), rs.getInt("qty")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Product> findByName(String name) {
        List<Product> list = new ArrayList<Product>();
        Connection c = null;
        try {
            c = this.dataSource.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM product WHERE UPPER(name) LIKE ? ORDER BY name");
            ps.setString(1, "%" + name.toUpperCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("image"),
                    rs.getString("category"), rs.getDouble("price"), rs.getInt("qty")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return list;
    }

    public Product findById(int id) {
        Product product = new Product();
        Connection c = null;
        try {
            c = this.dataSource.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM product WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                product = new Product();
                product.setProductId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setImage(rs.getString("image"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getDouble("price"));
                product.setQty(rs.getInt("qty"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return product;
    }

    public Product create(Product product) {
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = this.dataSource.getConnection();
            ps = c.prepareStatement("INSERT INTO product (name, description, image, category, price, qty) VALUES (?, ?, ?, ?, ?, ?)",
                new String[] { "ID" });
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getImage());
            ps.setString(4, product.getCategory());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getQty());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            // Update the id in the returned object. This is important as this value must be returned to the client.
            int id = rs.getInt(1);
            product.setProductId(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return product;
    }

    public boolean update(Product product) {
        Connection c = null;
        try {
            c = this.dataSource.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE product SET name=?, description=?, image=?, category=?, price=?, qty=? WHERE id=?");
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getImage());
            ps.setString(4, product.getCategory());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getQty());
            ps.setInt(7, product.getProductId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public boolean remove(Product product) {
        Connection c = null;
        try {
            c = this.dataSource.getConnection();
            PreparedStatement ps = c.prepareStatement("DELETE FROM product WHERE id=?");
            ps.setInt(1, product.getProductId());
            int count = ps.executeUpdate();
            return count == 1;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
