package com.example.tarealab05.Repository;

import com.example.tarealab05.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Integer id);
}
