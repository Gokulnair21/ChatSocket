package com.example.chatsocket.ui.client;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatsocket.R;
import com.example.chatsocket.data.model.Message;
import com.example.chatsocket.data.model.MessengerType;
import com.example.chatsocket.databinding.FragmentClientPageBinding;
import com.example.chatsocket.ui.chat.MessageAdapter;
import com.example.chatsocket.ui.server.ServerPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ClientPage extends Fragment {


    private FragmentClientPageBinding binding;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private Socket client;
    private final MessageAdapter messageAdapter = new MessageAdapter();
    String host;
    private StartClient runningThread;
    boolean showChatUI = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClientPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.connect.setOnClickListener(v -> {
            host = binding.IPAddressEditText.getEditText().getEditableText().toString().trim();
            if (host.isEmpty() || host.equals("null")) {
                Toast.makeText(getContext(), "Please enter the servers IP address", Toast.LENGTH_SHORT).show();

            } else {
                Thread thread = new Thread(new InitializeClient());
                thread.start();
            }
        });
        binding.stopClient.setOnClickListener(v -> {
            new Thread(new StopServer()).start();
        });
        binding.chatUI.messageRecyclerView.setAdapter(messageAdapter);
        binding.chatUI.sendMessage.setOnClickListener(v -> {
            try {
                String message = binding.chatUI.messageTextInputLayout.getEditText().getEditableText().toString();
                if (message.isEmpty() || message.equals("null")) {
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                } else {
                    Thread thread = new Thread(new SendMessage(message));
                    thread.start();
                    binding.chatUI.messageTextInputLayout.getEditText().getEditableText().clear();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }

        });
        binding.showChatPage.setOnClickListener(v -> {
            showChatUI();
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (showChatUI) {
                    hideChatUI();
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void showChatUI() {
        binding.connectionUI.setVisibility(View.GONE);
        binding.chatUI.getRoot().setVisibility(View.VISIBLE);
        showChatUI = true;
    }


    private void hideChatUI() {
        binding.connectionUI.setVisibility(View.VISIBLE);
        binding.chatUI.getRoot().setVisibility(View.GONE);
        showChatUI = false;
    }

    private void showConnectButton() {
        binding.stopClient.setVisibility(View.GONE);
        binding.connect.setVisibility(View.VISIBLE);
        binding.IPAddressEditText.setVisibility(View.VISIBLE);
        binding.showChatPage.setVisibility(View.GONE);
    }

    private void showStopButton() {
        binding.stopClient.setVisibility(View.VISIBLE);
        binding.connect.setVisibility(View.GONE);
    }


    private void updateRecyclerView(Message message) {
        messageAdapter.changeMessageList(message);
        messageAdapter.notifyDataSetChanged();
        binding.chatUI.messageRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    public class InitializeClient implements Runnable {

        @Override
        public void run() {
            try {
                new Handler(Looper.getMainLooper()).post(() -> {
                    showStopButton();
                });
                client = new Socket(host, 4444);
                inputStreamReader = new InputStreamReader(client.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                printWriter = new PrintWriter(client.getOutputStream(), true);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        binding.IPAddressEditText.setVisibility(View.GONE);
                        binding.showChatPage.setVisibility(View.VISIBLE);
                        showChatUI();
                    }
                });
                runningThread = new StartClient();
                runningThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class StartClient extends Thread {

        boolean running = true;

        @Override
        public void start() {

            try {
                while (running) {
                    String message = bufferedReader.readLine();
                    if (message != null) {
                        Message messageObject = new Message(message, 0, MessengerType.Receiver);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                updateRecyclerView(messageObject);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void cancelRunning() {
            running = false;
        }
    }

    class SendMessage implements Runnable {
        final String message;

        public SendMessage(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                Message messageObject = new Message(message, 0, MessengerType.Sender);
                printWriter.write(message + "\n");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateRecyclerView(messageObject);

                    }
                });
                printWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class StopServer implements Runnable {

        @Override
        public void run() {
            try {
                if (runningThread != null && runningThread.isAlive()) {
                    runningThread.cancelRunning();
                }
                if (client != null) {
                    client.close();
                }

                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                new Handler(Looper.getMainLooper()).post(() -> {
                    showConnectButton();
                });
            }
        }
    }


}

