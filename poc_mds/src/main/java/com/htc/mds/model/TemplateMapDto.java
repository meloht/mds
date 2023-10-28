package com.htc.mds.model;

import java.util.HashMap;
import java.util.Map;

public class TemplateMapDto {

    private Map<String, Object> bodyMap;
    private Map<String, Object> subjectMap;

    public TemplateMapDto() {
        bodyMap = new HashMap<>();
        subjectMap = new HashMap<>();
    }

    public Map<String, Object> getBodyMap() {
        return bodyMap;
    }

    public void setBodyMap(Map<String, Object> bodyMap) {
        this.bodyMap = bodyMap;
    }

    public Map<String, Object> getSubjectMap() {
        return subjectMap;
    }

    public void setSubjectMap(Map<String, Object> subjectMap) {
        this.subjectMap = subjectMap;
    }
}
