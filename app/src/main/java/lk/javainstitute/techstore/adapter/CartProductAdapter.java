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
import lk.javainstitute.techstore.listener.CartProductSelectListener;
import lk.javainstitute.techstore.listener.ProductSelectListener;
import lk.javainstitute.techstore.model.CartProduct;
import lk.javainstitute.techstore.model.Product;


public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.ViewHolder> {
    private ArrayList<CartProduct> products;
    private Context context;
    private FirebaseStorage storage;
    private CartProductSelectListener selectListener;

    public CartProductAdapter() {
    }

    public CartProductAdapter(ArrayList<CartProduct> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    public CartProductAdapter(ArrayList<CartProduct> products, Context context, CartProductSelectListener selectListener) {
        this.products = products;
        this.context = context;
        this.selectListener = selectListener;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cart_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CartProduct product = products.get(position);
        holder.productTitleTextView.setText(product.getTitle());
        holder.qty.setText(product.getQty());
        holder.productPriceTextView.setText("Rs." + product.getPrice());

        storage.getReference("product_img/" + product.getImage1())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(140, 140)
                                .centerCrop()
                                .into(holder.productIconImageView);
                    }
                });

        holder.productIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewProduct(products.get(position));
            }
        });
        holder.productPriceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewProduct(products.get(position));
            }
        });
        holder.productTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewProduct(products.get(position));
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.removeProduct(products.get(position));
            }
        });

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.productRemoveQty(products.get(position));
            }
        });

        holder.pluse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.productAddQty(products.get(position));
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
        TextView qty;
        ImageView productIconImageView;
        ImageView pluse;
        ImageView minus;
        ImageView delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitleTextView = itemView.findViewById(R.id.productTitle);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            productIconImageView = itemView.findViewById(R.id.productIcon);
            pluse = itemView.findViewById(R.id.pluse);
            minus = itemView.findViewById(R.id.minus);
            delete = itemView.findViewById(R.id.delete);
            qty = itemView.findViewById(R.id.qty);
        }
    }
}

