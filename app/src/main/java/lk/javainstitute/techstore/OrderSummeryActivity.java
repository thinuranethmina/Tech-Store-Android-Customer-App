package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import lk.javainstitute.techstore.adapter.OrderSummeryAdapter;
import lk.javainstitute.techstore.model.CartProduct;
import lk.javainstitute.techstore.model.Order;
import lk.javainstitute.techstore.model.Product;
import lk.javainstitute.techstore.model.User;
import lk.javainstitute.techstore.utill.Format;

public class OrderSummeryActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private ArrayList<CartProduct> products;
    private String userId;
    private Integer total = 0;
    private TextView totalLabel;
    private OrderSummeryAdapter orderSummeryAdapter;
    private NotificationManager notificationManager;
    private String channelId="info";

    private int x = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summery);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        totalLabel = findViewById(R.id.total);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "INFO", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setDescription("This is Information");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

            if (!getIntent().getExtras().getString("mobile").isEmpty() || !getIntent().getExtras().getString("address").isEmpty() || !getIntent().getExtras().getString("latitude ").isEmpty() || !getIntent().getExtras().getString("longitude ").isEmpty()) {


                String mobile = getIntent().getExtras().getString("mobile");
                String address = getIntent().getExtras().getString("address");

                TextView nameView = findViewById(R.id.name);
                TextView addressView = findViewById(R.id.address);
                TextView mobileView = findViewById(R.id.mobile);

                addressView.setText(address);
                mobileView.setText(mobile);


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        products = new ArrayList<>();

                        RecyclerView itemView = findViewById(R.id.productRecyclerView);

                        orderSummeryAdapter = new OrderSummeryAdapter(products, getApplicationContext());

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        itemView.setLayoutManager(linearLayoutManager);

                        itemView.setAdapter(orderSummeryAdapter);

                        firestore.collection("users")
                                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            userId = documentSnapshot.getId();

                                            nameView.setText(documentSnapshot.toObject(User.class).getName());


                                            firestore.collection("users/" + userId + "/cart")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                            if (task.isSuccessful() && task.getResult().size() > 0) {

                                                                CollectionReference collectionReference = firestore.collection("products");
                                                                HashMap<String, String> pInfo = new HashMap<>();
                                                                int numberOfIds = task.getResult().size();
                                                                int index = 0;

                                                                String[] idsArray = new String[numberOfIds];

                                                                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                    String id = snapshot.getData().get("id").toString();
                                                                    pInfo.put(id, snapshot.getData().get("qty").toString());
                                                                    idsArray[index++] = id;
                                                                }

                                                                List<String> idsToMatch = Arrays.asList(idsArray);

                                                                Query query = collectionReference.whereIn("id", idsToMatch);


                                                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        products.clear();

                                                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                            Product allItems = snapshot.toObject(Product.class);

                                                                            products.add(new CartProduct(allItems.getId(), allItems.getTitle(), allItems.getImage1(), pInfo.get(allItems.getId()), allItems.getPrice()));

                                                                            total = total + Integer.valueOf((Integer.valueOf(allItems.getPrice()) * Integer.valueOf(pInfo.get(allItems.getId()))));
                                                                        }
                                                                        totalLabel.setText("Rs." + total.toString() + "/=");
                                                                        orderSummeryAdapter.notifyDataSetChanged();
                                                                        UpdateTotal();
                                                                    }
                                                                });

                                                            } else {

                                                                TextView textView = new TextView(getApplicationContext());
                                                                textView.setTextSize(20);
                                                                textView.setTextColor(Color.GRAY);
                                                                textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                                                textView.setText("Cart Empty");
                                                                textView.setGravity(Gravity.CENTER);

                                                                FrameLayout frameLayout = findViewById(R.id.cartContainer);
                                                                frameLayout.addView(textView);

                                                            }

                                                        }
                                                    });


                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }).start();

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

                findViewById(R.id.confirmtBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String ref = String.valueOf(System.currentTimeMillis());

                        String mobile = getIntent().getExtras().getString("mobile");
                        String address = getIntent().getExtras().getString("address");
                        String latitude = getIntent().getExtras().getString("latitude");
                        String longitude = getIntent().getExtras().getString("longitude");

                        LocalDateTime currentDateTime = LocalDateTime.now();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = currentDateTime.format(formatter);

                        Order order = new Order(ref, firebaseAuth.getCurrentUser().getEmail(), total.toString(), mobile, address, latitude, longitude, formattedDateTime, 0);

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


                                                            for (CartProduct cartitem : products) {

                                                                firestore.collection("products").whereEqualTo("id", cartitem.getId()).get()
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

                                                                                                        Integer nowQty = Integer.parseInt(addproduct.getQty()) - Integer.parseInt(cartitem.getQty());

                                                                                                        HashMap<String, Object> data = new HashMap<>();
                                                                                                        data.put("qty", String.valueOf(nowQty));

                                                                                                        firestore.document("products/" + documentSnapshot.getId())
                                                                                                                .update(data)
                                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Void aVoid) {

                                                                                                                        addproduct.setQty(cartitem.getQty());

                                                                                                                        firestore.collection("orders/" + orderDocId + "/products")
                                                                                                                                .add(addproduct)
                                                                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                                                    @Override
                                                                                                                                    public void onSuccess(DocumentReference documentReference) {

                                                                                                                                        firestore.collection("users/" + userId + "/cart/")
                                                                                                                                                .get()
                                                                                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                                                                        if (task.isSuccessful()) {
                                                                                                                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                                                                                                firestore.collection("users/" + userId + "/cart/").document(document.getId()).delete();
                                                                                                                                                                int count = products.size();

                                                                                                                                                                if (count == x) {

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

                                                                                                                                                                    Toast.makeText(getApplicationContext(), "Confirmed Your Order.", Toast.LENGTH_LONG).show();
                                                                                                                                                                    startActivity(new Intent(OrderSummeryActivity.this,MainLayoutActivity.class));
                                                                                                                                                                    finish();
                                                                                                                                                                }
                                                                                                                                                                x++;
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                });

                                                                                                                                    }
                                                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                    @Override
                                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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


                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                                                        }
                                                    });


                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });


                    }
                });

            }

        } else {
            finish();
        }
    }

    public void UpdateTotal() {
        total = 0;
        for (CartProduct cartitem : products) {
            total = total + Integer.valueOf((Integer.valueOf(cartitem.getPrice()) * Integer.valueOf(cartitem.getQty())));
        }
        totalLabel = findViewById(R.id.total);
        totalLabel.setText("Rs." + new Format(String.valueOf(total)).formatPrice() + "/=");
    }

}