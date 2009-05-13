package flex.spring.samples.dao;

import java.util.List;

public interface IGenericDAO<T> {

	public List<T> findAll();

	public List<T> findByName(String name);

	public T findById(int id);

	public T create(T item);

	public boolean update(T item);

	public boolean remove(T item);

}