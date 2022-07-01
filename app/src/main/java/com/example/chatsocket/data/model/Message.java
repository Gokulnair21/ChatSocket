package com.example.chatsocket.data.model;

public class Message {

    public final String message;
    public final int messengerID;
    public final MessengerType messengerType;


    public Message(String message, int messengerID,MessengerType messengerType) {
        this.message = message;
        this.messengerID = messengerID;
        this.messengerType=messengerType;
    }


}
