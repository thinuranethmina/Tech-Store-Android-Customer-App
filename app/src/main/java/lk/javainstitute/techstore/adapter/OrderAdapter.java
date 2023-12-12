package lk.javainstitute.techstore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import lk.javainstitute.techstore.R;
import lk.javainstitute.techstore.listener.OrderSelectListner;
import lk.javainstitute.techstore.model.Order;
import lk.javainstitute.techstore.utill.Format;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private ArrayList<Order> orders;
    private Context context;
    private OrderSelectListner orderSelectListner;

    public OrderAdapter(ArrayList<Order> orders, Context context, OrderSelectListner orderSelectListner) {
        this.orders = orders;
        this.context = context;
        this.orderSelectListner = orderSelectListner;
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_row_layout, parent, false);
        return new OrderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Order order = orders.get(position);

        holder.orderIdTxt.setText(order.getId());
        holder.orderDateTxt.setText(order.getDate_time());
        holder.orderPriceTxt.setText("Rs. "+new Format(order.getTotal()).formatPrice() +"/=");
        holder.orderStatusTxt.setText(String.valueOf((order.getDeliver_status())).equals("1") ? "Delivered":"Pending" );

        holder.orderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderSelectListner.selectOrder(orders.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTxt, orderDateTxt, orderPriceTxt, orderStatusTxt;
        CardView orderCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTxt = itemView.findViewById(R.id.orderId);
            orderDateTxt = itemView.findViewById(R.id.datetime);
            orderPriceTxt = itemView.findViewById(R.id.price);
            orderStatusTxt = itemView.findViewById(R.id.status);
            orderCard = itemView.findViewById(R.id.orderCard);
        }
    }
}
