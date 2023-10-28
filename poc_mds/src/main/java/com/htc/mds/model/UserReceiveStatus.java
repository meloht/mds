package com.htc.mds.model;

import com.htc.mds.util.ConstValue;

public enum UserReceiveStatus {
    ReceiveSuccess(1, ConstValue.ReceiveSuccess),
    ReceiveFailed(0, ConstValue.ReceiveFailed);

    private int value;
    private String name;

    private UserReceiveStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static UserReceiveStatus intToEnum(int value) {
        switch (value) {
            case 0:
                return ReceiveFailed;
            case 1:
                return ReceiveSuccess;

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
