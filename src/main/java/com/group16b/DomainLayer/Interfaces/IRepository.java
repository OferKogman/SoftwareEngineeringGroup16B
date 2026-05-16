package com.group16b.DomainLayer.Interfaces;

import java.util.List;

public interface IRepository<T> {
    T findByID(String ID);
    List<T> getAll();
    void delete(String ID);
    void save(T Obj);
}
