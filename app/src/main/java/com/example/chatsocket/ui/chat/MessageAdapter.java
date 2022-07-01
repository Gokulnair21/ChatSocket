package com.example.chatsocket.ui.chat;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatsocket.data.model.Message;
import com.example.chatsocket.data.model.MessengerType;
import com.example.chatsocket.databinding.ReceiverMessageCardBinding;
import com.example.chatsocket.databinding.SenderMessageCardBinding;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Message> messages = new ArrayList<>(0);

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MessengerType.Sender.ordinal()) {
            SenderMessageCardBinding binding = SenderMessageCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SenderViewHolder(binding);
        } else {
            ReceiverMessageCardBinding binding = ReceiverMessageCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceiverViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).bindData(messages.get(position));
        } else {
            ((ReceiverViewHolder) holder).bindData(messages.get(position));
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).messengerType.ordinal();
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private final ReceiverMessageCardBinding binding;

        public ReceiverViewHolder(@NonNull ReceiverMessageCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindData(Message message) {
            binding.message.setText(message.message);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        private final SenderMessageCardBinding binding;

        public SenderViewHolder(@NonNull SenderMessageCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindData(Message message) {
            binding.message.setText(message.message);
        }
    }

    public void changeMessageList(Message message) {
        messages.add(message);
        //notifyItemInserted(messages.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeEntireDataSet(List<Message> latestMessages) {
        messages.clear();
        messages.addAll(latestMessages);
        notifyDataSetChanged();
    }


}
