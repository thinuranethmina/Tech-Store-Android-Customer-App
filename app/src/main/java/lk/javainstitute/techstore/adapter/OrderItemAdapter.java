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
import lk.javainstitute.techstore.model.CartProduct;
import lk.javainstitute.techstore.model.Product;
import lk.javainstitute.techstore.utill.Format;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private ArrayList<Product> products;
    private Context context;
    private FirebaseStorage storage;

    public OrderItemAdapter() {
    }

    public OrderItemAdapter(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();

    }


    @NonNull
    @Override
    public OrderItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_summery_row_layout, parent, false);
        return new OrderItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = products.get(position);
        holder.productTitleTextView.setText(product.getTitle());
        holder.qty.setText(product.getQty());
        holder.productPriceTextView.setText("Rs. "+new Format(product.getPrice()).formatPrice() +"/=");

        storage.getReference("product_img/" + product.getImage1())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .fit()
                                .centerCrop()
                                .into(holder.productIconImageView);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitleTextView = itemView.findViewById(R.id.productTitle);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            productIconImageView = itemView.findViewById(R.id.productIcon);
            qty = itemView.findViewById(R.id.qty);
        }
    }

}
