package com.example.modprojecttracker.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.User;
import com.example.modprojecttracker.util.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = DatabaseHelper.getInstance(this);

        TextInputEditText fullNameInput = findViewById(R.id.input_full_name);
        TextInputEditText usernameInput = findViewById(R.id.input_username_register);
        TextInputEditText passwordInput = findViewById(R.id.input_password_register);
        TextInputEditText confirmPasswordInput = findViewById(R.id.input_password_confirm);
        MaterialButton registerButton = findViewById(R.id.button_register);
        MaterialButton loginButton = findViewById(R.id.button_go_login);

        registerButton.setOnClickListener(v -> {
            String fullName = fullNameInput.getText() != null ? fullNameInput.getText().toString().trim() : "";
            String username = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
            String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
            String confirm = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString() : "";

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must have at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Password confirmation does not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (databaseHelper.usernameExists(username)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            User admin = new User();
            admin.setUid(UUID.randomUUID().toString());
            admin.setName(fullName);
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setRole(Constants.ROLE_ADMIN);
            admin.setCreatedAt(System.currentTimeMillis());

            long rowId = databaseHelper.insertUser(admin);
            if (rowId == -1) {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Admin account created. Please login.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        loginButton.setOnClickListener(v -> finish());
    }
}



