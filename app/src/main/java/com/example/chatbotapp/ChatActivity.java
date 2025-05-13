package com.example.chatbotapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String username;
    private LinearLayout chatContainer;
    private EditText chatInputBox;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");


        chatContainer = findViewById(R.id.chatContainer);
        chatInputBox = findViewById(R.id.chatInputBox);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());

        // Add welcome message from the bot
        addBotMessage("Hello " + username + "! How can I assist you today?");
    }

    private void sendMessage() {
        String userMessage = chatInputBox.getText().toString().trim();
        if (userMessage.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserMessage(userMessage);
        chatInputBox.setText("");

        progressBar.setVisibility(View.VISIBLE);

        // Send request to server
        sendRequestToServer(userMessage);
    }

    private void sendRequestToServer(String userMessage) {
        String url = "http://10.0.2.2:5000/chat";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    addBotMessage(response.trim());
                },
                error -> {
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(this, "Error connecting to server", Toast.LENGTH_LONG).show();
                    addBotMessage("Sorry, I couldn't process your request. Please try again.");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userMessage", userMessage);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        Volley.newRequestQueue(this).add(request);
    }

    private void addUserMessage(String message) {
        View messageView = LayoutInflater.from(this).inflate(R.layout.user_message_item, chatContainer, false);

        // Find and set avatar initial
        TextView avatarInitial = messageView.findViewById(R.id.avatarInitial);
        String initial = username != null && username.length() > 0
                ? String.valueOf(username.charAt(0)).toUpperCase()
                : "?";
        avatarInitial.setText(initial);

        TextView contentView = messageView.findViewById(R.id.messageContent);
        contentView.setText(message);

        chatContainer.addView(messageView);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        View messageView = LayoutInflater.from(this).inflate(R.layout.bot_message_item, chatContainer, false);
        TextView contentView = messageView.findViewById(R.id.messageContent);

        contentView.setText(message);

        chatContainer.addView(messageView);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}