package com.example.tarealab05.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "name")
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Column(name = "document_type")
    private String documentType;
    public String getDocumentType() {
        return documentType;
    }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @NotBlank(message = "El número de documento es obligatorio")
    @Column(name = "document", unique = true)
    private String document;
    public String getDocument() {
        return document;
    }
    public void setDocument(String document) {
        this.document = document;
    }

    public Customer(){}
}
