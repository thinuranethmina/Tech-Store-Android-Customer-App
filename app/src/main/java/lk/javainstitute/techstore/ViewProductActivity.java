package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firestore.v1.Value;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lk.javainstitute.techstore.adapter.ProductAdapter;
import lk.javainstitute.techstore.listener.ProductSelectListener;
import lk.javainstitute.techstore.model.Product;

public class ViewProductActivity extends AppCompatActivity implements ProductSelectListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private ArrayList<Product> arrayList;
    private ArrayList<Product> similerProducts;
    private Product item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (!getIntent().getExtras().getString("productID").isEmpty()) {

            String id = getIntent().getExtras().getString("productID");

            arrayList = new ArrayList<Product>();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    firestore.collection("products").whereEqualTo("id", id).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        item = snapshot.toObject(Product.class);

                                        if (item.getId().equals(id)) {

                                            TextView title = findViewById(R.id.title);
                                            TextView price = findViewById(R.id.price);
                                            TextView category = findViewById(R.id.category);
                                            TextView desc = findViewById(R.id.description);

                                            if (Integer.parseInt(item.getQty()) <= 0) {
                                                findViewById(R.id.buyNow).setVisibility(View.GONE);
                                                findViewById(R.id.addToCartBtn).setVisibility(View.GONE);
                                            }

                                            ImageSlider imageSlider = findViewById(R.id.image_slider);

                                            ArrayList<SlideModel> slideModels = new ArrayList<>();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/tech-store-31c56.appspot.com/o/product_img%2F" + item.getImage1() + "?alt=media", ScaleTypes.CENTER_INSIDE));
                                                    slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/tech-store-31c56.appspot.com/o/product_img%2F" + item.getImage2() + "?alt=media", ScaleTypes.CENTER_INSIDE));
                                                    slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/tech-store-31c56.appspot.com/o/product_img%2F" + item.getImage3() + "?alt=media", ScaleTypes.CENTER_INSIDE));

                                                    imageSlider.setImageList(slideModels, ScaleTypes.FIT);

                                                    title.setText(item.getTitle().toString());
                                                    price.setText("Rs." + item.getPrice().toString());
                                                    desc.setText(item.getDescription().toString());
                                                    category.setText(item.getCategory().toString());

                                                }
                                            });


                                            similerProducts = new ArrayList<>();

                                            RecyclerView itemView = findViewById(R.id.productRecyclerView);

                                            ProductAdapter productAdapter = new ProductAdapter(similerProducts, ViewProductActivity.this, ViewProductActivity.this);

                                            GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
                                            itemView.setLayoutManager(layoutManager);

                                            itemView.setAdapter(productAdapter);

                                            firestore.collection("products")
                                                    .whereNotEqualTo("id", item.getId().toString())
                                                    .whereEqualTo("category", item.getCategory().toString())
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                                            if (error != null) {
                                                                return;
                                                            }

                                                            if (value != null && !value.isEmpty()) {
                                                                for (DocumentChange change : value.getDocumentChanges()) {
                                                                    Product product = change.getDocument().toObject(Product.class);
                                                                    switch (change.getType()) {
                                                                        case ADDED:
                                                                            similerProducts.add(product);
                                                                        case MODIFIED:
                                                                            for (Product existingProduct : similerProducts) {
                                                                                if (existingProduct.getId().equals(product.getId())) {
                                                                                    existingProduct.setTitle(product.getTitle());
                                                                                    existingProduct.setPrice(product.getPrice());
                                                                                    existingProduct.setImage1(product.getImage1());
                                                                                    break;
                                                                                }
                                                                            }
                                                                            break;
                                                                        case REMOVED:
                                                                            Iterator<Product> iterator = similerProducts.iterator();
                                                                            while (iterator.hasNext()) {
                                                                                Product existingProduct = iterator.next();
                                                                                if (existingProduct.getId().equals(product.getId())) {
                                                                                    iterator.remove(); // Remove the specific object reference
                                                                                    break;
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                            }


                                                            productAdapter.notifyDataSetChanged();
                                                        }
                                                    });


                                            break;
                                        }

                                    }

                                }
                            });


                }
            }).start();


            findViewById(R.id.addToCartBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firebaseAuth.getCurrentUser() != null) {

                        if (firebaseAuth.getCurrentUser().isEmailVerified()) {


                            firestore.collection("users")
                                    .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                String userId = documentSnapshot.getId();

                                                firestore.collection("users/" + userId + "/cart")
                                                        .whereEqualTo("id", item.getId())
                                                        .get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                if (queryDocumentSnapshots.size() > 0) {
                                                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                        String productDoc = documentSnapshot.getId();

                                                                        firestore.collection("users/" + userId + "/cart")
                                                                                .whereEqualTo("id", item.getId())
                                                                                .get()
                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {

                                                                                            String qty = snapshot.getData().get("qty").toString();

                                                                                            if ((Integer.parseInt(qty) + 1) <= Integer.parseInt(item.getQty())) {

                                                                                                Map<String, Object> data = new HashMap<>();
                                                                                                data.put("id", item.getId());
                                                                                                data.put("qty", Integer.parseInt(qty) + 1);

                                                                                                firestore.document("users/" + userId + "/cart/" + productDoc)
                                                                                                        .update(data)
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                                // Update successful
                                                                                                                Toast.makeText(getApplicationContext(), "Already product have in your cart and increased qty.", Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                        })
                                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                                            @Override
                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                // Update failed
                                                                                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                        });

                                                                                            } else {
                                                                                                Toast.makeText(getApplicationContext(), "Not enought stock", Toast.LENGTH_LONG).show();
                                                                                            }

                                                                                        }
                                                                                    }
                                                                                });

                                                                    }
                                                                } else {

                                                                    Map<String, Object> data = new HashMap<>();
                                                                    data.put("id", item.getId());
                                                                    data.put("qty", 1);

                                                                    firestore.collection("users")
                                                                            .document(userId)
                                                                            .collection("cart")
                                                                            .add(data)
                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentReference documentReference) {
                                                                                    Toast.makeText(getApplicationContext(), "Added to Cart", Toast.LENGTH_LONG).show();
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

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            Toast.makeText(getApplicationContext(), "Check your email.", Toast.LENGTH_LONG).show();

                        }

                    } else {
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        finish();
                    }
                }
            });


            findViewById(R.id.buyNow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(firebaseAuth.getCurrentUser()!=null){
                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
                            startActivity(new Intent(ViewProductActivity.this, BuyNowActivity.class)
                                    .putExtra("productID", item.getId())
                            );
                        }else{
                            Toast.makeText(getApplicationContext(),"Please verify your email. Check your email inbox.", Toast.LENGTH_SHORT).show();
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Please Sign In", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    }


                }
            });


        } else {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            finish();
        }


    }

    @Override
    public void viewProduct(Product product) {
        startActivity(new Intent(getApplicationContext(), ViewProductActivity.class).putExtra("productID", product.getId().toString()));
    }
}