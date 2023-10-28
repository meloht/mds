package com.htc.mds.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TemplateQuery {

    @NotBlank(message = "name cannot be empty")
    @Size(max = 60,message = "max size 60")
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
