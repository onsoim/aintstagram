package com.ssg.aintstagram;


public class Message {
    private int messageID;
    private String text;
    private Boolean mine;

    public Message(int messageID, String text, Boolean mine) {
        this.messageID = messageID;
        this.text = text;
        this.mine = mine;
    }

    public int getMessageID() { return this.messageID; }

    public String getText() {
        return this.text;
    }

    public java.lang.Boolean getMine() {
        return this.mine;
    }
}
