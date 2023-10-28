package com.htc.mds.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TemplateDto {

    @NotBlank(message = "templateName cannot be empty")
    @Size(max = 60,message = "max size 60")
    private String templateName;
    @NotBlank(message = "templateContent cannot be empty")
    private String templateContent;

    private String subjectTemplate;


    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }
}
