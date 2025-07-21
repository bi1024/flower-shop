package com.prm.groupproject_flowershop.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm.groupproject_flowershop.OrderDetailsActivity;
import com.prm.groupproject_flowershop.R;
import com.prm.groupproject_flowershop.dao.OrderViewDao;

import java.util.List;

public class CustomerOrderAdapter extends  RecyclerView.Adapter<CustomerOrderAdapter.MyViewHolder> {
    private Context context;
    private List<OrderViewDao> orderList;

    public CustomerOrderAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_layout,
                viewGroup, false);
        return new MyViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//
//        String total = String.valueOf(orderList.get(position).getTotal());
//        holder.total.setText(total);
//        holder.orderStatus.setText(orderList.get(position).getOrderStatus());
//        holder.customerName.setText(orderList.get(position).getCustomerName());
//    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrderViewDao order = orderList.get(position);
        Log.d("order",orderList.get(position).toString());

        holder.total.setText(String.valueOf(order.getTotal()));
        holder.orderStatus.setText(order.getOrderStatus());
        holder.customerName.setText(order.getCustomerName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailsActivity.class);
           intent.putExtra("ORDER_ID", order.getOrderId());
            context.startActivity(intent);
        });
    }


    public void setOrderList(List<OrderViewDao> orderList){
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (orderList == null) {
            return 0;
        }
        return orderList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView customerName,total,orderStatus;
        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.tvCustomerName);
            orderStatus = itemView.findViewById(R.id.tvOrderStatus);
            total = itemView.findViewById(R.id.tvTotal);
        }
    }
}
