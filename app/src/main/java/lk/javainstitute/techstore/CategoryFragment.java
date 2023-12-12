package lk.javainstitute.techstore;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;

import lk.javainstitute.techstore.adapter.CategoryAdaptor;
import lk.javainstitute.techstore.adapter.HomeCategoryAdapter;
import lk.javainstitute.techstore.listener.HomeCategorySelectListener;
import lk.javainstitute.techstore.model.Category;
import lk.javainstitute.techstore.model.Product;

public class CategoryFragment extends Fragment implements HomeCategorySelectListener {

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private ArrayList<Category> categories;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

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

                categories = new ArrayList<>();

                RecyclerView itemView = view.findViewById(R.id.categoryRecyclerView);

                CategoryAdaptor categoryAdaptor = new CategoryAdaptor(categories, getActivity(), CategoryFragment.this);

                GridLayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2, GridLayoutManager.VERTICAL, false);

                itemView.setLayoutManager(layoutManager);

                itemView.setAdapter(categoryAdaptor);


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

                        categoryAdaptor.notifyDataSetChanged();
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
}