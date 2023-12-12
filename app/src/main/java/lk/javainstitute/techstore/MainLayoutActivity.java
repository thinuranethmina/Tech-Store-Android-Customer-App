package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lk.javainstitute.techstore.broadcast.PowerConnected;

public class MainLayoutActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, SensorEventListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;
    private SensorManager sensorManager;
    private Sensor accelometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.topToolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        requestPermissions(new String[]{
                Manifest.permission.ACTIVITY_RECOGNITION
        }, 100);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelometer != null) {
            sensorManager.registerListener(MainLayoutActivity.this, accelometer, SensorManager.SENSOR_DELAY_UI);
        }

        Settings.System.canWrite(this);

        IntentFilter intentFilter = new IntentFilter("android.intent.action.ACTION_POWER_CONNECTED");
        PowerConnected mbr = new PowerConnected();
        registerReceiver(mbr, intentFilter);

        EditText search = findViewById(R.id.search);

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

                    // Handle search action
                    showSearchResult(search.getText().toString());
                    return true;
                }
                return false;
            }
        });

        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2; // Index for the drawableEnd
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Hide the keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                        showSearchResult(search.getText().toString());

                        return true;
                    }
                }
                return false;
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                FragmentManager supportFragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, new HomeFragment());
                fragmentTransaction.commit();
            }
        }).start();



    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        if (item.getItemId() == R.id.bottemNavHome) {
            loadFragment(new HomeFragment());
            return true;
        } else if (item.getItemId() == R.id.bottemNavCategory) {
            loadFragment(new CategoryFragment());
            return true;
        } else if (item.getItemId() == R.id.bottemNavcart) {
            loadFragment(new CartFragment());
            return true;
        } else if (item.getItemId() == R.id.bottemNavProfile) {
            loadFragment(new ProfileFragment());
            return true;
        }

        return false;

    }

    public void loadFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    public void showSearchResult(String text) {
        startActivity(new Intent(MainLayoutActivity.this, SearchResultActivity.class)
                .putExtra("text", text)
        );
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double value = Math.floor(x * x + y * y + z * z);

            if (value > 340) {

                final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shutter_sound);
                try {
                    // Create a bitmap from the root view
                    View rootView = getWindow().getDecorView().getRootView();
                    Bitmap screenshotBitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(screenshotBitmap);
                    rootView.draw(canvas);

                    // Save the bitmap to a file
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String fileName = "screenshot_" + timeStamp + ".png";

                    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    File screenshotFile = new File(directory,  "Screenshots/"+fileName);

                    FileOutputStream outputStream = new FileOutputStream(screenshotFile);
                    screenshotBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    // Notify the user
                    Toast.makeText(MainLayoutActivity.this, "Screenshot saved to " + screenshotFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                mediaPlayer.start();


            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}