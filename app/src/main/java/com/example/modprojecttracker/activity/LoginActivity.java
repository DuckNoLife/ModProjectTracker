package com.example.modprojecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.User;
import com.example.modprojecttracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isAdminLoggedIn()) {
            openDashboard();
            return;
        }

        TextInputEditText usernameInput = findViewById(R.id.input_username);
        TextInputEditText passwordInput = findViewById(R.id.input_password);
        MaterialButton loginButton = findViewById(R.id.button_login);
        MaterialButton registerButton = findViewById(R.id.button_go_register);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
            String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = databaseHelper.authenticateAdmin(username, password);
            if (user == null) {
                Toast.makeText(this, "Invalid credentials or not an admin account", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.saveLogin(user);
            openDashboard();
        });

        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void openDashboard() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}


