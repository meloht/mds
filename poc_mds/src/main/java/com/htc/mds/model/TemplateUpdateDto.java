package com.htc.mds.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TemplateUpdateDto {

    @NotNull(message = "id cannot be null")
    private long id;
    @NotBlank(message = "templateName cannot be empty")
    @Size(max = 60,message = "max size 60")
    private String templateName;
    @NotBlank(message = "templateContent cannot be empty")
    private String templateContent;

    private String subjectTemplate;

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }
}
