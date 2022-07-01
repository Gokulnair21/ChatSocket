package com.example.chatsocket.ui.server;

import android.animation.Animator;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;

import com.example.chatsocket.R;
import com.example.chatsocket.data.model.Message;
import com.example.chatsocket.data.model.MessengerType;
import com.example.chatsocket.databinding.FragmentServerPageBinding;
import com.example.chatsocket.ui.chat.MessageAdapter;
import com.example.chatsocket.ui.client.ClientPage;

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
import java.util.Objects;

public class ServerPage extends Fragment {


    private FragmentServerPageBinding binding;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStreamReader inputStreamReader;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private final MessageAdapter messageAdapter = new MessageAdapter();
    private StartServer runningThread;
    boolean showChatUI = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentServerPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.startServer.setOnClickListener(v -> {
            try {
                String IPAddress = getIPAddress();
                Spanned result = Html.fromHtml("Please use this <b>" + IPAddress + "</b> IP Address for connection with client");
                binding.IPAddress.setText(result);
                Thread initializeServer = new Thread(new InitializeServer());
                initializeServer.start();
                binding.launchAnimation.playAnimation();
            } catch (UnknownHostException e) {
                Toast.makeText(getContext(), "Unable to obtain IP address", Toast.LENGTH_SHORT).show();
            }
        });
        binding.stopServer.setOnClickListener(v -> {
            new Thread(new StopServer()).start();
        });
        binding.showChatPage.setOnClickListener(v -> {
            showChatUI();
        });

        binding.chatUI.messageRecyclerView.setAdapter(messageAdapter);
        binding.chatUI.sendMessage.setOnClickListener(v -> {
            try {
                String message = binding.chatUI.messageTextInputLayout.getEditText().getEditableText().toString();
                if (message.isEmpty() || message.equals("null")) {
                    Toast.makeText(getContext(), "Please enter some message", Toast.LENGTH_SHORT).show();
                } else {
                    Thread thread = new Thread(new SendMessage(message));
                    thread.start();
                    binding.chatUI.messageTextInputLayout.getEditText().getEditableText().clear();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }

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

    private void showStopButton() {
        binding.stopServer.setVisibility(View.VISIBLE);
        binding.startServer.setVisibility(View.GONE);
    }

    private void showStartButton() {
        binding.showChatPage.setVisibility(View.GONE);
        binding.stopServer.setVisibility(View.GONE);
        binding.startServer.setVisibility(View.VISIBLE);
        binding.IPAddress.setText(getString(R.string.server_instruction));
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


    private void updateRecyclerView(Message message) {
        messageAdapter.changeMessageList(message);
        messageAdapter.notifyDataSetChanged();
        binding.chatUI.messageRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    public class InitializeServer implements Runnable {

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(4444);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        showStopButton();
                    }
                });
                clientSocket = serverSocket.accept();
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        binding.showChatPage.setVisibility(View.VISIBLE);
                        showChatUI();
                    }
                });
                runningThread = new StartServer();
                runningThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class StartServer extends Thread {

        boolean running = true;

        @Override
        public void start() {

            while (running) {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                printWriter.write(message + "\n");
                printWriter.flush();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateRecyclerView(new Message(message, 0, MessengerType.Sender));

                    }
                });

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
                if (clientSocket != null && clientSocket.isConnected()) {
                    clientSocket.close();
                }
                serverSocket.close();
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
                    showStartButton();
                });
            }
        }

    }

    private String getIPAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipAddress).array()).getHostAddress();
    }


}
