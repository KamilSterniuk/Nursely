package com.example.nursely;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class MainActivity extends AppCompatActivity {

    EditText loginEditText, passwordEditText;
    Button loginButton;
    final String FILE_NAME = "users.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Skopiuj plik users.json z assets do pamięci wewnętrznej jeśli go tam nie ma
        copyJsonIfNotExists();

        // Inicjalizacja pól
        loginEditText = findViewById(R.id.loginEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Obsługa przycisku logowania
        loginButton.setOnClickListener(v -> {
            String loginInput = loginEditText.getText().toString().trim();
            String passwordInput = passwordEditText.getText().toString();

            try {
                String jsonStr = readJsonFromInternal();
                JSONArray users = new JSONArray(jsonStr);
                boolean found = false;

                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String login = user.getString("login");
                    String password = user.getString("password");
                    String userType = user.getString("userType");

                    if (loginInput.equals(login) && passwordInput.equals(password)) {
                        found = true;
                        if (userType.equals("admin")) {
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, NurseActivity.class));
                        }
                        finish(); // zakończ bieżące activity
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Nieprawidłowy login lub hasło", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Błąd odczytu użytkowników", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kopiuje plik users.json z assets do pamięci wewnętrznej, jeśli jeszcze go tam nie ma
    private void copyJsonIfNotExists() {
        File file = new File(getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            try (InputStream is = getAssets().open(FILE_NAME);
                 OutputStream os = openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Czyta zawartość users.json z pamięci wewnętrznej
    private String readJsonFromInternal() throws IOException {
        File file = new File(getFilesDir(), FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        reader.close();
        fis.close();
        return sb.toString();
    }
}
