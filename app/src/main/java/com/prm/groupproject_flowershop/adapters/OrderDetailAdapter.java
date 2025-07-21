package com.prm.groupproject_flowershop.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.prm.groupproject_flowershop.R;
import com.prm.groupproject_flowershop.models.OrderDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private List<OrderDetail> orderDetails;
    private Map<Long, String> flowerNameMap = new HashMap<>();

    public void setFlowerNameMap(Map<Long, String> flowerNameMap) {
        this.flowerNameMap = flowerNameMap;
    }


    public OrderDetailAdapter(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OrderDetail detail = orderDetails.get(position);

        String flowerName = flowerNameMap.get(detail.getFlowerId());
        if (flowerName == null) {
            flowerName = "Flower ID: " + detail.getFlowerId();
        }
        holder.tvFlowerName.setText(flowerName);

//        holder.tvFlowerName.setText("Flower ID: " + detail.getFlowerId());
        holder.tvQuantity.setText("Quantity: " + detail.getQuantity());
        holder.tvUnitPrice.setText("Unit Price: $" + String.format("%.2f", detail.getUnitPrice()));

        double subtotal = detail.getQuantity() * detail.getUnitPrice();
        holder.tvSubtotal.setText("Subtotal: $" + String.format("%.2f", subtotal));
    }

    @Override
    public int getItemCount() {
        return orderDetails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlowerName, tvQuantity, tvUnitPrice, tvSubtotal;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFlowerName = itemView.findViewById(R.id.tvFlowerName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }
    }
}

