package lk.javainstitute.techstore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.protobuf.Value;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lk.javainstitute.techstore.adapter.CartProductAdapter;
import lk.javainstitute.techstore.adapter.ProductAdapter;
import lk.javainstitute.techstore.listener.CartProductSelectListener;
import lk.javainstitute.techstore.model.CartProduct;
import lk.javainstitute.techstore.model.Product;
import lk.javainstitute.techstore.utill.Format;

public class CartFragment extends Fragment implements CartProductSelectListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private ArrayList<CartProduct> products;
    private View view;
    private String userId;
    private Integer total = 0;
    private TextView totalLabel;

    private CartProductAdapter cartProductAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cart, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        totalLabel = view.findViewById(R.id.total);

        if (firebaseAuth.getCurrentUser() != null) {

            if (firebaseAuth.getCurrentUser().isEmailVerified()) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        products = new ArrayList<>();

                        RecyclerView itemView = view.findViewById(R.id.productRecyclerView);

                        cartProductAdapter = new CartProductAdapter(products, getActivity().getApplicationContext(), CartFragment.this);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                        itemView.setLayoutManager(linearLayoutManager);

                        itemView.setAdapter(cartProductAdapter);

                        firestore.collection("users")
                                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            userId = documentSnapshot.getId();

                                            firestore.collection("users/" + userId + "/cart")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                            if (task.isSuccessful() && task.getResult().size() > 0) {


                                                                view.findViewById(R.id.checkoutBtn).setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        startActivity(new Intent(getActivity().getApplicationContext(), CheckoutActivity.class));
                                                                    }
                                                                });

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


                                                                query.whereEqualTo("status","true").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        products.clear();

                                                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                            Product item = snapshot.toObject(Product.class);

                                                                            products.add(new CartProduct(item.getId(), item.getTitle(), item.getImage1(), pInfo.get(item.getId()), item.getPrice()));

                                                                            total = total + Integer.valueOf((Integer.valueOf(item.getPrice()) * Integer.valueOf(pInfo.get(item.getId()))));
                                                                        }
                                                                        totalLabel.setText("Rs." + total.toString() + "/=");
                                                                        cartProductAdapter.notifyDataSetChanged();
                                                                        UpdateTotal();
                                                                    }
                                                                });

                                                            } else {

                                                                TextView textView = new TextView(getActivity().getApplicationContext());
                                                                textView.setTextSize(20);
                                                                textView.setTextColor(Color.GRAY);
                                                                textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                                                textView.setText("Cart Empty");
                                                                textView.setGravity(Gravity.CENTER);

                                                                FrameLayout frameLayout = view.findViewById(R.id.cartContainer);
                                                                frameLayout.addView(textView);

                                                                view.findViewById(R.id.checkoutBtn).setEnabled(false);

                                                            }
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        Thread.sleep(400);
                                                                    } catch (
                                                                            InterruptedException e) {
                                                                    }
                                                                    getActivity().runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            getActivity().findViewById(R.id.loader).setVisibility(View.GONE);
                                                                        }
                                                                    });                                                                }
                                                            }).start();

                                                        }
                                                    });


                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }).start();


            } else {
                firebaseAuth.getCurrentUser().sendEmailVerification();
                Toast.makeText(getActivity().getApplicationContext(), "Check your email verify your account.", Toast.LENGTH_LONG).show();

            }
        } else {
            startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
            Toast.makeText(getActivity().getApplicationContext(), "Please signin first.", Toast.LENGTH_LONG).show();

        }

        return view;
    }


    @Override
    public void viewProduct(CartProduct product) {
        startActivity(new Intent(getActivity().getApplicationContext(), ViewProductActivity.class).putExtra("productID", product.getId().toString()));
    }

    @Override
    public void productAddQty(CartProduct product) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String oldQtyS = product.getQty().toString();
                Integer oldQtyI = Integer.parseInt(oldQtyS);
                Integer newQtyI = oldQtyI + 1;

                firestore.collection("products").whereEqualTo("id", product.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            Product item = snapshot.toObject(Product.class);

                            if (item.getId().equals(product.getId())) {

                                if (newQtyI > Integer.parseInt(item.getQty())) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Not enough stock", Toast.LENGTH_SHORT).show();
                                } else {
                                    String newQtyS = String.valueOf(newQtyI);
                                    for (CartProduct cartitem : products) {
                                        if (cartitem.getId().equals(product.getId())) {
                                            cartitem.setQty(newQtyS.toString());
                                            break;
                                        }
                                    }


                                    Map<String, Object> data = new HashMap<>();
                                    data.put("id", product.getId());
                                    data.put("qty", newQtyI);

                                    firestore.collection("users/" + userId + "/cart").whereEqualTo("id", product.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                firestore.document("users/" + userId + "/cart/" + documentSnapshot.getId()).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Update successful
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Update failed
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    });

                                    cartProductAdapter.notifyDataSetChanged();
                                    UpdateTotal();

                                }

                                break;
                            }

                        }
                    }
                });


            }
        }).start();
    }

    @Override
    public void productRemoveQty(CartProduct product) {
        String oldQtyS = product.getQty().toString();
        Integer oldQtyI = Integer.parseInt(oldQtyS);
        Integer newQtyI = oldQtyI - 1;

        if (newQtyI >= 1) {
            String newQtyS = String.valueOf(newQtyI);
            for (CartProduct cartitem : products) {
                if (cartitem.getId().equals(product.getId())) {
                    cartitem.setQty(newQtyS.toString());
                    break;
                }
            }


            Map<String, Object> data = new HashMap<>();
            data.put("id", product.getId());
            data.put("qty", newQtyI);

            firestore.collection("users/" + userId + "/cart").whereEqualTo("id", product.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        firestore.document("users/" + userId + "/cart/" + documentSnapshot.getId()).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Update successful
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Update failed
                            }
                        });
                        break;
                    }
                }
            });


            cartProductAdapter.notifyDataSetChanged();
            UpdateTotal();

        }

    }

    @Override
    public void removeProduct(CartProduct product) {

        firestore.collection("users/" + userId + "/cart").whereEqualTo("id", product.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Delete the document
                                firestore.collection("users/" + userId + "/cart").document(document.getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                for (CartProduct cartitem : products) {
                                                    if (cartitem.getId().equals(product.getId())) {
                                                        products.remove(cartitem);
                                                        cartProductAdapter.notifyDataSetChanged();
                                                        UpdateTotal();
                                                        break;
                                                    }
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });
                            }
                        } else {
                        }
                    }
                });
    }

    public void UpdateTotal() {
        total = 0;
        for (CartProduct cartitem : products) {
            total = total + Integer.valueOf((Integer.valueOf(cartitem.getPrice()) * Integer.valueOf(cartitem.getQty())));
        }
        totalLabel = view.findViewById(R.id.total);
//        totalLabel.setText("Rs."+new PriceFormat(total.toString())+"/=");
        totalLabel.setText("Rs." + new Format(String.valueOf(total)).formatPrice() + "/=");
    }
}