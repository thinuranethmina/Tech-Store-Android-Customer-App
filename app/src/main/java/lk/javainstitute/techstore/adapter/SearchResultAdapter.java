package lk.javainstitute.techstore.adapter;

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
import lk.javainstitute.techstore.listener.SearchResultSelectListener;
import lk.javainstitute.techstore.model.Category;
import lk.javainstitute.techstore.model.Product;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private ArrayList<Product> products;
    private Context context;
    private FirebaseStorage storage;
    private SearchResultSelectListener selectListener;

    public SearchResultAdapter(ArrayList<Product> products, Context context, SearchResultSelectListener selectListener) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_result_row, parent, false);
        return new SearchResultAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText(product.getPrice());

        holder.productCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewProduct(product);
            }
        });

        storage.getReference("product_img/"+product.getImage1())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get()
                                .load(uri)
                                .centerCrop()
                                .fit()
                                .into(holder.productIcon);
                    }
                });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle, productPrice;
        ImageView productIcon;
        View productCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            productIcon = itemView.findViewById(R.id.productIcon);
            productCard = itemView.findViewById(R.id.productCard);
        }
    }
}
