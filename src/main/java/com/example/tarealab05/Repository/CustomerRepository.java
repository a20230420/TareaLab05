package com.example.tarealab05.Repository;

import com.example.tarealab05.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Validar documento único al crear
    boolean existsByDocument(String document);

    // Validar documento único al editar
    boolean existsByDocumentAndIdNot(String document, Integer id);
}
