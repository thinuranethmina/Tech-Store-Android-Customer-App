package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import lk.javainstitute.techstore.adapter.OrderItemAdapter;
import lk.javainstitute.techstore.adapter.OrderSummeryAdapter;
import lk.javainstitute.techstore.model.Order;
import lk.javainstitute.techstore.model.Product;

public class OrderItemsActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Product> items;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_items);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(!getIntent().getExtras().getString("orderID").isEmpty()) {

            String orderid = getIntent().getExtras().getString("orderID");



            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseAuth.getCurrentUser().isEmailVerified()) {


                    items = new ArrayList<>();
                    RecyclerView categoryView = findViewById(R.id.orderItemRecyclerView);
                    OrderItemAdapter ordersAdapter = new OrderItemAdapter(items, this);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                    categoryView.setLayoutManager(linearLayoutManager);
                    categoryView.setAdapter(ordersAdapter);


                    firestore.collection("orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot snapshot1 : task.getResult()) {
                                    Order order = snapshot1.toObject(Order.class);

                                    if (order.getId().equals(orderid)) {

                                        orderId = snapshot1.getId();

                                        firestore.collection("orders/" + orderId + "/products").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot snapshot2 : task.getResult()) {

                                                        Product item = snapshot2.toObject(Product.class);
                                                        items.add(item);

                                                    }
                                                    ordersAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                        break;
                                    }

                                }


                            } else {
                                Toast.makeText(getApplicationContext(), "Try again later.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                } else {
                    Toast.makeText(getApplicationContext(), "Please verify your email account. Check your email inbox.", Toast.LENGTH_LONG).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                }

            } else {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }

        }

    }
}