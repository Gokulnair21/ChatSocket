package com.example.chatsocket.ui.client;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatsocket.data.model.Message;

import java.util.List;

public class ClientPageViewModel extends ViewModel {


    MutableLiveData<Message> data=new MutableLiveData<Message>();

    void update(Message message) {
        data.postValue(message);
    }

}
