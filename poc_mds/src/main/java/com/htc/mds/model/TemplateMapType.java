package com.htc.mds.model;

import com.htc.mds.util.ConstValue;

public enum TemplateMapType {
    Body(0, ConstValue.Body),
    Subject(1, ConstValue.Subject);

    private int value;
    private String name;

    private TemplateMapType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static TemplateMapType intToEnum(int value) {
        switch (value) {
            case 0:
                return Body;
            case 1:
                return Subject;

            default:
                return null;
        }
    }


    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
