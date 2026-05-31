package com.group16b.ApplicationLayer.DTOs;

public class ProductionCompanyDTO {

    private int id;
    private String name;


    public ProductionCompanyDTO() {
    }

    public ProductionCompanyDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
