package lk.javainstitute.techstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_TechStore_FullScreen);
        setContentView(R.layout.activity_loading);


        ImageView imageView = findViewById(R.id.splashLogo);
        Picasso.get().load(R.drawable.tech_store).resize(300, 300).into(imageView);

        ProgressBar text = findViewById(R.id.splashProgressBar);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int status=0;  status<= 100; status++ ) {

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int finalStatus = status;

                    LoadingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setProgress(finalStatus);

                            if (finalStatus == 100 ) {
                                startActivity(new Intent(LoadingActivity.this, MainLayoutActivity.class));
                                finish();
                            }

                        }
                    });

                }

            }
        }).start();



    }
}