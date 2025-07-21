package com.prm.groupproject_flowershop;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm.groupproject_flowershop.adapters.OrderDetailAdapter;
import com.prm.groupproject_flowershop.apis.FlowerRepository;
import com.prm.groupproject_flowershop.apis.OderDetailRepository;
import com.prm.groupproject_flowershop.apis.OrderDetailService;
import com.prm.groupproject_flowershop.apis.OrderRepository;
import com.prm.groupproject_flowershop.apis.OrderService;
import com.prm.groupproject_flowershop.dao.OrderViewDao;
import com.prm.groupproject_flowershop.models.Customer;
import com.prm.groupproject_flowershop.models.Flower;
import com.prm.groupproject_flowershop.models.Order;
import com.prm.groupproject_flowershop.models.OrderDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderDetailAdapter adapter;
    private List<OrderDetail> filteredList = new ArrayList<>();
    private long orderId;
    private OrderDetailService orderDetailService;

    private TextView tvTotalAmount;
    private Button btnBack;
    private Map<Long, String> flowerNameMap = new HashMap<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        recyclerView = findViewById(R.id.recyclerOrderDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        orderId = getIntent().getLongExtra("ORDER_ID", -1);
        orderDetailService = OderDetailRepository.getOrderDetailService();

        fetchAllFlowers(); // fetch flowers first, for mapping names later
    }

//    private void fetchOrderDetails() {
//        Call<OrderDetail[]> orderCall = orderDetailService.getAllOrderDetails();
//        orderCall.enqueue(new Callback<OrderDetail[]>() {
//            @Override
//            public void onResponse(Call<OrderDetail[]> call, Response<OrderDetail[]> response) {
//                if (response.isSuccessful()) {
//                    double totalPrice = 0;
//
//                    for (OrderDetail detail : response.body()) {
//                        if (detail.getOrderId() == orderId) {
//                            filteredList.add(detail);
//                            totalPrice += detail.getUnitPrice() * detail.getQuantity();
//                        }
//                    }
//
//                    adapter = new OrderDetailAdapter(filteredList);
//                    recyclerView.setAdapter(adapter);
//
//                    tvTotalAmount.setText("Total: $" + String.format("%.2f", totalPrice));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<OrderDetail[]> call, Throwable t) {
//                Toast.makeText(OrderDetailsActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    // Fetch flowers first
    private void fetchAllFlowers() {
        Call<Flower[]> call = FlowerRepository.getFlowerService().getAllFlowers();
        call.enqueue(new Callback<Flower[]>() {
            @Override
            public void onResponse(Call<Flower[]> call, Response<Flower[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Flower flower : response.body()) {
                        flowerNameMap.put(flower.getId(), flower.getFlowerName());
                    }
                    fetchOrderDetails(); // Now fetch order details after flower data loaded
                }
            }

            @Override
            public void onFailure(Call<Flower[]> call, Throwable t) {
                Toast.makeText(OrderDetailsActivity.this, "Failed to load flowers.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Fetch order details after flowers are ready
    private void fetchOrderDetails() {
        Call<OrderDetail[]> orderCall = orderDetailService.getAllOrderDetails();
        orderCall.enqueue(new Callback<OrderDetail[]>() {
            @Override
            public void onResponse(Call<OrderDetail[]> call, Response<OrderDetail[]> response) {
                if (response.isSuccessful()) {
                    double totalPrice = 0;
                    for (OrderDetail detail : response.body()) {
                        if (detail.getOrderId() == orderId) {
                            filteredList.add(detail);
                            totalPrice += detail.getUnitPrice() * detail.getQuantity();
                        }
                    }

                    adapter = new OrderDetailAdapter(filteredList);
                    adapter.setFlowerNameMap(flowerNameMap);  // Flower map is ready here
                    recyclerView.setAdapter(adapter);

                    tvTotalAmount.setText("Total: $" + String.format("%.2f", totalPrice));
                }
            }

            @Override
            public void onFailure(Call<OrderDetail[]> call, Throwable t) {
                Toast.makeText(OrderDetailsActivity.this, "Failed to load order details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}