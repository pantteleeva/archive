package com.example.my_archive;



import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Первый экран приложения: вход по логину и паролю. */
public class LoginActivity extends AppCompatActivity {

    private EditText etLogin;
    private EditText etPassword;
    private TextView tvError;
    private DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = DbHelper.get(this);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Кнопка «Готово» на клавиатуре тоже выполняет вход
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    private void attemptLogin() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Введите логин и пароль");
            return;
        }

        String fullName;
        try {
            fullName = db.authenticate(login, password);
        } catch (Exception e) {
            showError("Ошибка базы данных: " + e.getMessage());
            return;
        }

        if (fullName == null) {
            showError("Неверный логин или пароль");
            etPassword.setText("");
            return;
        }

        Session.loggedIn = true;
        Session.userName = fullName;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
