package lk.javainstitute.techstore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import lk.javainstitute.techstore.R;
import lk.javainstitute.techstore.listener.HomeCategorySelectListener;
import lk.javainstitute.techstore.model.Category;

public class CategoryAdaptor extends RecyclerView.Adapter<CategoryAdaptor.ViewHolder> {

    private ArrayList<Category> categories;
    private Context context;
    private FirebaseStorage storage;
    private HomeCategorySelectListener selectListener;

    public CategoryAdaptor(ArrayList<Category> categories, Context context, HomeCategorySelectListener selectListener) {
        this.categories = categories;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public CategoryAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.category_row_layout, parent, false);
        return new CategoryAdaptor.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdaptor.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categories.get(position);
        holder.categoryNameTextView.setText(category.getName());

        holder.categoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewCategory(categories.get(position));
            }
        });

        storage.getReference("category-images/"+category.getImageId())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get()
                                .load(uri)
                                .centerCrop()
                                .fit()
                                .into(holder.categoryIconImageView);
                    }
                });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        ImageView categoryIconImageView;
        View categoryCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryName);
            categoryIconImageView = itemView.findViewById(R.id.categoryIcon);
            categoryCard = itemView.findViewById(R.id.categoryCard);
        }
    }

}
