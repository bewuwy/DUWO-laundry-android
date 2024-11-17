package dev.bewu.duwolaundry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button loginButton = findViewById(R.id.login);
        EditText email = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        EditText mutlipossURL = findViewById(R.id.multipossURL);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        loginButton.setOnClickListener(v -> {
            String e = email.getText().toString();
            String p = password.getText().toString();
            String m = mutlipossURL.getText().toString();

            if (m.isEmpty()) {
                m = getString(R.string.multipossurl_default);
            }

            // update preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("multipossURL", m);
            editor.putString("userMail", e);
            editor.putString("userPwd", p);
            editor.apply();

            // update scraper in app
            MultiPossScraper scraper = new MultiPossScraper(e, p, m);
            LaundryApplication application = (LaundryApplication) getApplication();
            application.setMultiPossScraper(scraper);

            // switch back to main activity
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        });
    }
}