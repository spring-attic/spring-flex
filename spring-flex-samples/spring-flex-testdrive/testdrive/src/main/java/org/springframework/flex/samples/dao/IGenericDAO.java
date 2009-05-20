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

package org.springframework.flex.samples.dao;

import java.util.List;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
public interface IGenericDAO<T> {

    public List<T> findAll();

    public List<T> findByName(String name);

    public T findById(int id);

    public T create(T item);

    public boolean update(T item);

    public boolean remove(T item);

}
