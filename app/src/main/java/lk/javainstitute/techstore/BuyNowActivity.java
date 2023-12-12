package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lk.javainstitute.techstore.adapter.ProductAdapter;
import lk.javainstitute.techstore.model.CartProduct;
import lk.javainstitute.techstore.model.Order;
import lk.javainstitute.techstore.model.Product;
import lk.javainstitute.techstore.model.User;
import lk.javainstitute.techstore.utill.Format;

public class BuyNowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker_current;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private NotificationManager notificationManager;
    private String channelId="info";
    private User user;
    private LatLng dLocation;
    private String ProductId;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_now);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (!getIntent().getExtras().getString("productID").isEmpty()) {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseAuth.getCurrentUser().isEmailVerified()) {

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);

                    ProductId = getIntent().getExtras().getString("productID");

                    TextView titleView = findViewById(R.id.product);
                    TextView priceView = findViewById(R.id.price);
                    TextView categoryView = findViewById(R.id.category);
                    TextView qtyView = findViewById(R.id.qty);
                    TextView totalView = findViewById(R.id.total);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            firestore.collection("products").whereEqualTo("id", ProductId).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                product = snapshot.toObject(Product.class);

                                                if (product.getId().equals(ProductId)) {

                                                    if (Integer.parseInt(product.getQty()) <= 0) {
                                                        finish();
                                                    }

                                                    titleView.setText(product.getTitle());
                                                    priceView.setText("Rs. " + new Format(String.valueOf(product.getPrice())).formatPrice());
                                                    totalView.setText("Rs. " + new Format(String.valueOf(product.getPrice())).formatPrice() + "/=");
                                                    categoryView.setText(product.getCategory());
                                                    qtyView.setText("1");

                                                    break;
                                                }

                                            }

                                        }
                                    });


                        }
                    }).start();


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            firestore.collection("users").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                String userDocId = documentSnapshot.getId();
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


                    findViewById(R.id.pluse).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String oldQtyS = qtyView.getText().toString();
                            Integer oldQtyI = Integer.parseInt(oldQtyS);
                            Integer newQtyI = oldQtyI + 1;

                            if (newQtyI > Integer.parseInt(product.getQty())) {
                                Toast.makeText(getApplicationContext(), "Not enough stock", Toast.LENGTH_SHORT).show();
                            } else {
                                Integer total = newQtyI * Integer.parseInt(product.getPrice());
                                qtyView.setText(newQtyI.toString());
                                totalView.setText("Rs. "+new Format(total.toString()).formatPrice()+"/=");
                            }


                        }
                    });

                    findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String oldQtyS = qtyView.getText().toString();
                            Integer oldQtyI = Integer.parseInt(oldQtyS);
                            Integer newQtyI = oldQtyI - 1;

                            if (newQtyI <= 0) {
                                qtyView.setText("1");
                                totalView.setText("Rs. "+new Format(product.getPrice()).formatPrice()+"/=");
                            } else {
                                Integer total = newQtyI * Integer.parseInt(product.getPrice());
                                qtyView.setText(newQtyI.toString());
                                totalView.setText("Rs. "+new Format(total.toString()).formatPrice()+"/=");
                            }

                        }
                    });


                    findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
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
                                Toast.makeText(BuyNowActivity.this, "Please enter Address line1", Toast.LENGTH_SHORT).show();
                            } else if (address2.isEmpty()) {
                                Toast.makeText(BuyNowActivity.this, "Please enter Address line2", Toast.LENGTH_SHORT).show();
                            } else if (city.isEmpty()) {
                                Toast.makeText(BuyNowActivity.this, "Please enter City", Toast.LENGTH_SHORT).show();
                            } else if (postalcode.isEmpty()) {
                                Toast.makeText(BuyNowActivity.this, "Please enter Postal code", Toast.LENGTH_SHORT).show();
                            } else if (mobile.isEmpty()) {
                                Toast.makeText(BuyNowActivity.this, "Please enter Mobile", Toast.LENGTH_SHORT).show();
                            } else {

                                if (dLocation != null && marker_current != null) {
//

                                    String ref = String.valueOf(System.currentTimeMillis());

                                    LocalDateTime currentDateTime = LocalDateTime.now();

                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    String formattedDateTime = currentDateTime.format(formatter);

                                    String address = address1 + ", " + address2 + ", " + city + ", " + postalcode;

                                    Integer total = (Integer.parseInt(qtyView.getText().toString()) * Integer.parseInt(product.getPrice().toString()));

                                    Order order = new Order(ref, firebaseAuth.getCurrentUser().getEmail(), total.toString(), mobile, address, String.valueOf(dLocation.latitude), String.valueOf(dLocation.longitude), formattedDateTime, 0);

                                    firestore.collection("users")
                                            .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                        String userId = documentSnapshot.getId();

                                                        firestore.collection("orders")
                                                                .add(order)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {

                                                                        String orderDocId = documentReference.getId();


                                                                        firestore.collection("products").whereEqualTo("id", product.getId()).get()
                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                                            Product addproduct = snapshot.toObject(Product.class);

                                                                                            firestore.collection("products/")
                                                                                                    .whereEqualTo("id", addproduct.getId())
                                                                                                    .get()
                                                                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                                                                                                Integer nowQty = Integer.parseInt(addproduct.getQty()) - Integer.parseInt(qtyView.getText().toString());

                                                                                                                HashMap<String, Object> data = new HashMap<>();
                                                                                                                data.put("qty", String.valueOf(nowQty));

                                                                                                                firestore.document("products/" + documentSnapshot.getId())
                                                                                                                        .update(data)
                                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onSuccess(Void aVoid) {

                                                                                                                                addproduct.setQty(qtyView.getText().toString());

                                                                                                                                firestore.collection("orders/" + orderDocId + "/products")
                                                                                                                                        .add(addproduct)
                                                                                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                                                            @Override
                                                                                                                                            public void onSuccess(DocumentReference documentReference) {

                                                                                                                                                Intent intent = new Intent(getApplicationContext(),MainLayoutActivity.class);
                                                                                                                                                intent.putExtra("name","TEXT");

                                                                                                                                                PendingIntent pendingIntent = PendingIntent
                                                                                                                                                        .getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);

                                                                                                                                                Notification notification = new NotificationCompat.Builder(getApplicationContext(),channelId)
                                                                                                                                                        .setSmallIcon(R.drawable.notification_icon)
                                                                                                                                                        .setContentTitle("Order Info")
                                                                                                                                                        .setContentText("Your order has been confirmed")
                                                                                                                                                        .setColor(Color.RED)
                                                                                                                                                        .setContentIntent(pendingIntent)
                                                                                                                                                        .build();

                                                                                                                                                notificationManager.notify(1,notification);

                                                                                                                                                Toast.makeText(getApplicationContext(), "Confirmed Your Order.", Toast.LENGTH_SHORT).show();
                                                                                                                                                startActivity(new Intent(BuyNowActivity.this, MainLayoutActivity.class));
                                                                                                                                                finish();

                                                                                                                                            }
                                                                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                            @Override
                                                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        });
                                                                                                                            }
                                                                                                                        })
                                                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                                                            @Override
                                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                            }
                                                                                                                        });

                                                                                                            }
                                                                                                        }
                                                                                                    });


                                                                                        }
                                                                                    }
                                                                                });


                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                    }
                                                                });


                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                } else {
                                    Toast.makeText(BuyNowActivity.this, "Please select your deliver location", Toast.LENGTH_SHORT).show();

                                }

                            }


                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Please verify your email. Check your email inbox.", Toast.LENGTH_SHORT).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Please Sign In", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }

        } else {
            finish();
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
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
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