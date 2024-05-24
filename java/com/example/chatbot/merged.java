package com.example.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username_edit);
        loginButton = findViewById(R.id.buttonView);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            if (!username.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.chatbot;

        import android.os.Bundle;
        import android.view.Gravity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.LinearLayout;
        import android.widget.ScrollView;
        import android.widget.TextView;

        import androidx.appcompat.app.AppCompatActivity;

        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;

public class ChatActivity extends AppCompatActivity {
    private EditText messageEditText;
    private Button sendButton;
    private LinearLayout messagesLayout;
    private ScrollView scrollView;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = getIntent().getStringExtra("username");

        messagesLayout = findViewById(R.id.messages_layout);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        scrollView = findViewById(R.id.scrollView);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            addMessageToLayout(message, true); // User message
            sendMessageToBot(message);
            messageEditText.setText("");
            scrollToBottom();

        });
    }

    private void addMessageToLayout(String message, boolean isUser) {
        View messageView = LayoutInflater.from(this).inflate(R.layout.message_item, messagesLayout, false);
        TextView messageText = messageView.findViewById(R.id.message_text);
        messageText.setText(message);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageView.getLayoutParams();
        if (isUser) {
            params.gravity = Gravity.END;
            messageText.setBackgroundResource(R.drawable.message_background);
        } else {
            params.gravity = Gravity.START;
            messageText.setBackgroundResource(R.drawable.bot_chat);
        }

        messageView.setLayoutParams(params);
        messagesLayout.addView(messageView);
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void sendMessageToBot(String message) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:5000/chat");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userMessage", message);
                jsonParam.put("chatHistory", new JSONArray());

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    JSONObject responseJson = new JSONObject(response.toString());
                    String botMessage = responseJson.getString("message");

                    runOnUiThread(() -> addMessageToLayout(botMessage, false)); // Bot message
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
