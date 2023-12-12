package lk.javainstitute.techstore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import lk.javainstitute.techstore.model.User;

public class CheckoutActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker_current;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private User user;
    private LatLng dLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser().isEmailVerified()) {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    firestore.collection("users").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                String userId = documentSnapshot.getId();
                                user = documentSnapshot.toObject(User.class);

                                EditText mobileView = findViewById(R.id.mobile);
                                EditText address1View = findViewById(R.id.address1);
                                EditText address2View = findViewById(R.id.address2);
                                EditText cityView = findViewById(R.id.city);
                                EditText postalcodeView = findViewById(R.id.postalcode);

                                if (user.getMobile() != null) {
                                    mobileView.setText(user.getMobile());
                                }
                                if (user.getAddress() != null) {
                                    String[] address = user.getAddress().split(",");

                                    if (address.length >= 1) {
                                        address1View.setText(address[0]);
                                    }
                                    if (address.length >= 2) {
                                        address2View.setText(address[1]);
                                    }
                                    if (address.length >= 3) {
                                        cityView.setText(address[2]);
                                    }
                                    if (address.length >= 4) {
                                        postalcodeView.setText(address[3]);
                                    }

                                }

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(400);
                                        } catch (InterruptedException e) {
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                findViewById(R.id.loader).setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }).start();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();


            findViewById(R.id.countinueButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText mobileView = findViewById(R.id.mobile);
                    EditText address1View = findViewById(R.id.address1);
                    EditText address2View = findViewById(R.id.address2);
                    EditText cityView = findViewById(R.id.city);
                    EditText postalcodeView = findViewById(R.id.postalcode);

                    String address1 = address1View.getText().toString().replace(',', ' ');
                    String address2 = address2View.getText().toString().replace(',', ' ');
                    String city = cityView.getText().toString().replace(',', ' ');
                    String postalcode = postalcodeView.getText().toString().replace(',', ' ');
                    String mobile = mobileView.getText().toString();

                    if (address1.isEmpty()) {
                        Toast.makeText(CheckoutActivity.this, "Please enter Address line1", Toast.LENGTH_SHORT).show();
                    } else if (address2.isEmpty()) {
                        Toast.makeText(CheckoutActivity.this, "Please enter Address line2", Toast.LENGTH_SHORT).show();
                    } else if (city.isEmpty()) {
                        Toast.makeText(CheckoutActivity.this, "Please enter City", Toast.LENGTH_SHORT).show();
                    } else if (postalcode.isEmpty()) {
                        Toast.makeText(CheckoutActivity.this, "Please enter Postal code", Toast.LENGTH_SHORT).show();
                    } else if (mobile.isEmpty()) {
                        Toast.makeText(CheckoutActivity.this, "Please enter Mobile", Toast.LENGTH_SHORT).show();
                    } else {


                        if (dLocation != null && marker_current != null) {
                            startActivity(new Intent(getApplicationContext(), OrderSummeryActivity.class)
                                    .putExtra("address", address1 + "," + address2 + "," + city + "," + postalcode)
                                    .putExtra("mobile", mobile)
                                    .putExtra("latitude", String.valueOf(dLocation.latitude))
                                    .putExtra("longitude", String.valueOf(dLocation.longitude))
                            );

                        } else {
                            Toast.makeText(CheckoutActivity.this, "Please select your deliver location", Toast.LENGTH_SHORT).show();

                        }

                    }


                }
            });
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if (latLng != null && marker_current != null) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    dLocation = latLng;
                    marker_current.setPosition(latLng);
                }
            }
        });

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if (checkPermissions()) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }

                return false;
            }
        });

        if (checkPermissions()) {
            map.setMyLocationEnabled(true);
            getLastLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        dLocation = latLng;
//                        map.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }
            });
            //********************* Current location live update  *****************************
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).setWaitForAccurateLocation(true).setMinUpdateIntervalMillis(500).setMaxUpdateDelayMillis(1000).build();


            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        }
    }

    LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            currentLocation = locationResult.getLastLocation();
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            dLocation = latLng;

            if (marker_current == null) {
                MarkerOptions options = new MarkerOptions().title("My Location").position(latLng);
                marker_current = map.addMarker(options);
            } else {
                marker_current.setPosition(latLng);
            }

//            moveCamera(latLng);

        }
    };


    public void moveCamera(LatLng latLng) {
        CameraPosition cameraPosition = CameraPosition.builder().target(latLng).zoom(10f).build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }

    private boolean checkPermissions() {
        boolean permission = false;

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permission = true;
        }

        return permission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Snackbar.make(findViewById(R.id.container), "Location permission denied", Snackbar.LENGTH_INDEFINITE).setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
            }
        }
    }
}