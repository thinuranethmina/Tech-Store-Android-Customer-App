package lk.javainstitute.techstore;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;


import java.util.ArrayList;
import java.util.Iterator;

import lk.javainstitute.techstore.adapter.HomeCategoryAdapter;
import lk.javainstitute.techstore.adapter.ProductAdapter;
import lk.javainstitute.techstore.listener.HomeCategorySelectListener;
import lk.javainstitute.techstore.listener.ProductSelectListener;
import lk.javainstitute.techstore.model.Category;
import lk.javainstitute.techstore.model.Product;


public class HomeFragment extends Fragment implements HomeCategorySelectListener, ProductSelectListener {


    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    View view;
    private ArrayList<Product> products;
    private ArrayList<Category> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        ImageSlider imageSlider = view.findViewById(R.id.image_slider);

        ArrayList<SlideModel> slideModels = new ArrayList<>();

        slideModels.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

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
                });
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

//                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL,false);
//                GridLayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);

                categories = new ArrayList<>();

                RecyclerView itemView = view.findViewById(R.id.categoryRecyclerView);

                HomeCategoryAdapter homeCategoryAdapter = new HomeCategoryAdapter(categories, getActivity(), HomeFragment.this);

                GridLayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2, GridLayoutManager.HORIZONTAL, false);

                itemView.setLayoutManager(layoutManager);

                itemView.setAdapter(homeCategoryAdapter);


                firestore.collection("categories").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        for (DocumentChange change : value.getDocumentChanges()) {
                            Category category = change.getDocument().toObject(Category.class);
                            switch (change.getType()) {
                                case ADDED:
                                    categories.add(category);
                                case MODIFIED:
                                    for (Category existingCategory : categories) {
                                        if (existingCategory.getId().equals(category.getId())) {
                                            existingCategory.setName(category.getName());
                                            existingCategory.setImageId(category.getImageId());
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    Iterator<Category> iterator = categories.iterator();
                                    while (iterator.hasNext()) {
                                        Category existingCategory = iterator.next();
                                        if (existingCategory.getId().equals(category.getId())) {
                                            iterator.remove(); // Remove the specific object reference
                                            break;
                                        }
                                    }
                            }
                        }

                        homeCategoryAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {

                products = new ArrayList<>();

                RecyclerView itemView = view.findViewById(R.id.productRecyclerView);

                ProductAdapter productAdapter = new ProductAdapter(products, getActivity(), HomeFragment.this);


                GridLayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
                itemView.setLayoutManager(layoutManager);

                itemView.setAdapter(productAdapter);


                firestore.collection("products").whereEqualTo("status","true").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                            existingProduct.setPrice("Rs." + product.getPrice());
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


        return view;
    }

    @Override
    public void viewCategory(Category category) {
        startActivity(new Intent(getActivity().getApplicationContext(), CategoryWiseProductActivity.class).putExtra("categoryName",category.getName().toString()));
    }

    @Override
    public void viewProduct(Product product) {
        startActivity(new Intent(getActivity().getApplicationContext(), ViewProductActivity.class).putExtra("productID",product.getId().toString()));
    }
}