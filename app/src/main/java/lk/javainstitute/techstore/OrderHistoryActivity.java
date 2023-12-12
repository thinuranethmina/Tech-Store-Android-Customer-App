package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import lk.javainstitute.techstore.adapter.OrderAdapter;
import lk.javainstitute.techstore.listener.OrderSelectListner;
import lk.javainstitute.techstore.model.Order;

public class OrderHistoryActivity extends AppCompatActivity implements OrderSelectListner {
    private ArrayList<Order> orders;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser() != null ) {
            if(firebaseAuth.getCurrentUser().isEmailVerified()){

            orders = new ArrayList<>();
            RecyclerView categoryView = findViewById(R.id.orderRecyclerView);
            OrderAdapter ordersAdapter = new OrderAdapter(orders, getApplicationContext(), this);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            categoryView.setLayoutManager(linearLayoutManager);
            categoryView.setAdapter(ordersAdapter);


            firestore.collection("orders")
                    .whereEqualTo("email",firebaseAuth.getCurrentUser().getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot snapshot1 : task.getResult()) {
                                    Order order = snapshot1.toObject(Order.class);
                                        orders.add(order);
                                }

                                ordersAdapter.notifyDataSetChanged();


                            } else {

                                TextView textView = new TextView(getApplicationContext());
                                textView.setTextSize(20);
                                textView.setTextColor(Color.GRAY);
                                textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                textView.setText("Cart Empty");
                                textView.setGravity(Gravity.CENTER);

                                FrameLayout frameLayout = findViewById(R.id.container);
                                frameLayout.addView(textView);
                            }
                        }
                    });

            }else{
                Toast.makeText(getApplicationContext(),"Please verify your email account. Check your email inbox.",Toast.LENGTH_LONG).show();
                firebaseAuth.getCurrentUser().sendEmailVerification();
            }

        } else {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }


    }

    @Override
    public void selectOrder(Order order) {
        startActivity(new Intent(getApplicationContext(), OrderItemsActivity.class)
                .putExtra("orderID",order.getId())
        );
    }

}