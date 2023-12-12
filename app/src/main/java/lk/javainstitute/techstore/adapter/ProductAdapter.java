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
import lk.javainstitute.techstore.listener.ProductSelectListener;
import lk.javainstitute.techstore.model.Product;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private ArrayList<Product> products;
    private Context context;
    private FirebaseStorage storage;
    private ProductSelectListener selectListener;

    public ProductAdapter(ArrayList<Product> products, Context context,ProductSelectListener selectListener) {
        this.products = products;
        this.context = context;
        this.selectListener = selectListener;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.product_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = products.get(position);
        holder.productTitleTextView.setText(product.getTitle());
        holder.productPriceTextView.setText(product.getPrice());

        storage.getReference("product_img/"+product.getImage1())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(150, 150)
                                .centerCrop()
                                .into(holder.productIconImageView);
                    }
                });

        holder.viewProductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewProduct(products.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitleTextView;
        TextView productPriceTextView;
        ImageView productIconImageView;
        View viewProductbtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitleTextView = itemView.findViewById(R.id.productTitle);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            productIconImageView = itemView.findViewById(R.id.productIcon);
            viewProductbtn = itemView.findViewById(R.id.itemCard);
        }
    }
}

