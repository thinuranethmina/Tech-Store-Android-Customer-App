package lk.javainstitute.techstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;

import lk.javainstitute.techstore.adapter.ProductAdapter;
import lk.javainstitute.techstore.listener.ProductSelectListener;
import lk.javainstitute.techstore.model.Product;

public class CategoryWiseProductActivity extends AppCompatActivity implements ProductSelectListener {
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private ArrayList<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_wise_products);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!getIntent().getExtras().getString("categoryName").isEmpty()) {

            String category = getIntent().getExtras().getString("categoryName");

            TextView categoryLabel = findViewById(R.id.category);
            categoryLabel.setText(category);

            products = new ArrayList<>();

            RecyclerView itemView = findViewById(R.id.productRecyclerView);

            ProductAdapter productAdapter = new ProductAdapter(products, CategoryWiseProductActivity.this,this);

            GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
            itemView.setLayoutManager(layoutManager);

            itemView.setAdapter(productAdapter);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    firestore.collection("products")
                            .whereEqualTo("status","true")
                            .whereEqualTo("category",category).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            for (DocumentChange change : value.getDocumentChanges()) {
                                Product product = change.getDocument().toObject(Product.class);
                                switch (change.getType()) {
                                    case ADDED:
                                        products.add(product);
                                    case MODIFIED:
                                        for (Product existingProduct : products) {
                                            if (existingProduct.getId().equals(product.getId())) {
                                                existingProduct.setTitle(product.getTitle());
                                                existingProduct.setPrice(product.getPrice());
                                                existingProduct.setImage1(product.getImage1());
                                                break;
                                            }
                                        }
                                        break;
                                    case REMOVED:
                                        Iterator<Product> iterator = products.iterator();
                                        while (iterator.hasNext()) {
                                            Product existingProduct = iterator.next();
                                            if (existingProduct.getId().equals(product.getId())) {
                                                iterator.remove(); // Remove the specific object reference
                                                break;
                                            }
                                        }
                                }
                            }

                            productAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }).start();




        }else{
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    public void viewProduct(Product product) {
        startActivity(new Intent(getApplicationContext(), ViewProductActivity.class).putExtra("productID",product.getId().toString()));
    }
}