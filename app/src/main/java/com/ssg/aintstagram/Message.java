package com.ssg.aintstagram;


public class Message {
    private String name;
    private String text;
    private Boolean mine;

    public Message(String name, String text, Boolean mine) {
        this.name = name;
        this.text = text;
        this.mine = mine;
    }

    public String getText() {
        return this.text;
    }

    public java.lang.Boolean getMine() {
        return this.mine;
    }
}
