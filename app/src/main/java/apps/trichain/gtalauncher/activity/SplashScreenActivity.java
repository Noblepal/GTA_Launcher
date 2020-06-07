package apps.trichain.gtalauncher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import apps.trichain.gtalauncher.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3000);

    }
}
