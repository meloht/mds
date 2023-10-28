package com.htc.mds.model;

import com.htc.mds.util.ConstValue;

public enum MessageType {
    Teams(1, ConstValue.Teams),
    Mail(2, ConstValue.Mail);

    private int value;
    private String name;

    private MessageType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static MessageType intToEnum(int value) {
        switch (value) {
            case 1:
                return Teams;
            case 2:
                return Mail;

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
