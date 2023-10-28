package com.htc.mds.model;

import com.htc.mds.util.ConstValue;

public enum MessageContentType {
    Text(0, ConstValue.Text),
    Html(1, ConstValue.Html),
    TeamsCard(2, ConstValue.TeamsCard),
    Template(3, ConstValue.Template),
    Markdown(4, ConstValue.Markdown);


    private int value;
    private String name;

    private MessageContentType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static MessageContentType intToEnum(int value) {
        switch (value) {
            case 0:
                return Text;
            case 1:
                return Html;
            case 2:
                return TeamsCard;
            case 3:
                return Template;
            case 4:
                return Markdown;
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
