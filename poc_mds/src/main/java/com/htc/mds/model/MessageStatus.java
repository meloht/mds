package com.htc.mds.model;

import com.htc.mds.util.ConstValue;

public enum MessageStatus {
    Waiting(0, ConstValue.Waiting),
    Success(1, ConstValue.Success),
    PartialSuccess(2, ConstValue.PartialSuccess),
    Failed(3, ConstValue.Failed);

    private int value;
    private String name;

    private MessageStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
