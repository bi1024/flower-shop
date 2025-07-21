package com.prm.groupproject_flowershop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.prm.groupproject_flowershop.apis.FlowerRepository;
import com.prm.groupproject_flowershop.apis.FlowerService;
import com.prm.groupproject_flowershop.apis.OderDetailRepository;
import com.prm.groupproject_flowershop.apis.OrderDetailService;
import com.prm.groupproject_flowershop.apis.OrderRepository;
import com.prm.groupproject_flowershop.apis.OrderService;
import com.prm.groupproject_flowershop.app_services.CredentialService;
import com.prm.groupproject_flowershop.db.AppDatabase;
import com.prm.groupproject_flowershop.db.AppExecutors;
import com.prm.groupproject_flowershop.models.Cart;
import com.prm.groupproject_flowershop.models.Flower;
import com.prm.groupproject_flowershop.models.Order;
import com.prm.groupproject_flowershop.models.OrderDetail;
import com.prm.groupproject_flowershop.models.OrderStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Button btnPayAfter;
    long userId;
    CredentialService credentialService;
    private AppDatabase database;
    private OrderService orderService;
    private OrderDetailService orderDetailService;
    Button btnPayNow;
    private FlowerService flowerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        btnPayAfter = findViewById(R.id.checkout1);
        credentialService = new CredentialService(this);
        userId = credentialService.getCurrentUserId();
        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").build();
        orderService = OrderRepository.getOrderService();
        orderDetailService = OderDetailRepository.getOrderDetailService();
        flowerService = FlowerRepository.getFlowerService();

        btnPayAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkoutAfter();
            }
        });

        btnPayNow = findViewById(R.id.checkout2);
        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payNow();
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.menu_home){
                startActivity(new Intent(CheckoutActivity.this, FlowersList.class));
            }
            if(item.getItemId()==R.id.menu_order){
                startActivity(new Intent(CheckoutActivity.this, IndividualOrderActivity.class));
            }
            if(item.getItemId()==R.id.menu_map){
                startActivity(new Intent(CheckoutActivity.this, ViewMapActivity.class));
            }
            return true;
        });
    }

    private void checkoutAfter(){
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Cart> myCart = database.cartDao().getAllFlowersByUserID(userId);
                List<Order> orderList = new ArrayList<>();
                Call<Order[]> call = orderService.getAllOrders();
                call.enqueue(new Callback<Order[]>() {
                    @Override
                    public void onResponse(Call<Order[]> call, Response<Order[]> response) {
                        if(response.body() == null){
                            int maxId = 0;
                            double total = 0;
                            for (Cart c:myCart) {
                                total += c.getUnitPrice() *  c.getQuantity();
                            }
                            Order order = new Order(maxId + 1, new Date().toString(),new Date().toString(),total, OrderStatus.UNPAID.toString());
                            Call<Order> orderCreateCall = orderService.createOrder(order);
                            orderCreateCall.enqueue(new Callback<Order>() {
                                @Override
                                public void onResponse(Call<Order> call, Response<Order> response) {
                                    if(response.body() != null){
                                        for (Cart cart:myCart) {
                                            OrderDetail orderDetail = new OrderDetail(1,cart.getIdFlower(),cart.getUnitPrice(),cart.getQuantity());
                                            Call<OrderDetail> orderDetailCall = orderDetailService.createOrderDetail(orderDetail);
                                            orderDetailCall.enqueue(new Callback<OrderDetail>() {
                                                @Override
                                                public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                                                    if(response.body() != null){
                                                        updateFlowerStock(cart);
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<OrderDetail> call, Throwable t) {

                                                }
                                            });
                                        }
                                        deleteCart();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                                                intent.putExtra("redirect_to", "orders");
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Call<Order> call, Throwable t) {

                                }
                            });
                        } else {
                            List<Order> orders = new ArrayList<>();
                            for (int i = 0; i < response.body().length; i++) {
                                orders.add(response.body()[i]);
                            }
                            long idMax = orders.stream().max(Comparator.comparingLong(o -> o.getId())).get().getId();
                            double total = 0;
                            for (Cart c:myCart) {
                                total += c.getUnitPrice() *  c.getQuantity();
                            }
                            Order order = new Order(idMax + 1,userId, new Date().toString(),new Date().toString(),total, OrderStatus.UNPAID.toString());
                            final long orderId = idMax + 1;
                            Call<Order> orderCreateCall = orderService.createOrder(order);
                            orderCreateCall.enqueue(new Callback<Order>() {
                                @Override
                                public void onResponse(Call<Order> call, Response<Order> response) {
                                    if(response.body() != null){
                                        for (Cart cart:myCart) {
                                            OrderDetail orderDetail = new OrderDetail(orderId,cart.getIdFlower(),cart.getUnitPrice(),cart.getQuantity());
                                            Call<OrderDetail> orderDetailCall = orderDetailService.createOrderDetail(orderDetail);
                                            orderDetailCall.enqueue(new Callback<OrderDetail>() {
                                                @Override
                                                public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                                                    if(response.body() != null){
                                                        updateFlowerStock(cart);
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<OrderDetail> call, Throwable t) {

                                                }
                                            });
                                        }
                                        deleteCart();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                                                intent.putExtra("redirect_to", "orders");
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Call<Order> call, Throwable t) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<Order[]> call, Throwable t) {

                    }
                });
            }
        });

    }

    private void deleteCart(){
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Cart> myCart = database.cartDao().getAllFlowersByUserID(userId);
                for (Cart mCart:myCart) {
                    try {
                        database.cartDao().delete(mCart);
                    } catch (Exception e){
                        Log.e("tag",e.getMessage());
                    }
                }
            }
        });
    }

    private void updateFlowerStock(Cart cart) {
        Call<Flower> flowerCall = flowerService.getFlower(cart.getIdFlower());
        flowerCall.enqueue(new Callback<Flower>() {
            @Override
            public void onResponse(Call<Flower> call, Response<Flower> response) {
                Flower flower = response.body();
                if (flower != null) {
                    flower.setUnitInStock(flower.getUnitInStock() - cart.getQuantity());
                    Call<Flower> flowerCall2 = flowerService.updateFlower(cart.getIdFlower(),flower);
                    flowerCall2.enqueue(new Callback<Flower>() {
                        @Override
                        public void onResponse(Call<Flower> call, Response<Flower> response) {
                            // Stock updated successfully
                        }

                        @Override
                        public void onFailure(Call<Flower> call, Throwable t) {
                            Log.e("Stock Update", "Failed to update flower stock");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Flower> call, Throwable t) {
                Log.e("Flower Fetch", "Failed to fetch flower details");
            }
        });
    }

    private void payNow(){
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Cart> myCart = database.cartDao().getAllFlowersByUserID(userId);
                processPayment(myCart);
            }
        });
    }

    private void processPayment(List<Cart> carts) {
        Call<Order[]> call = orderService.getAllOrders();
        call.enqueue(new Callback<Order[]>() {
            @Override
            public void onResponse(Call<Order[]> call, Response<Order[]> response) {
                if(response.body() == null){
                    int maxId = 0;
                    double total = 0;
                    for (Cart c:carts) {
                        total += c.getUnitPrice() *  c.getQuantity();
                    }
                    Order order = new Order(maxId + 1,userId, new Date().toString(),new Date().toString(),total, OrderStatus.PAID.toString());
                    Call<Order> orderCreateCall = orderService.createOrder(order);
                    orderCreateCall.enqueue(new Callback<Order>() {
                        @Override
                        public void onResponse(Call<Order> call, Response<Order> response) {
                            if(response.body() != null){
                                for (Cart cart:carts) {
                                    OrderDetail orderDetail = new OrderDetail(1,cart.getIdFlower(),cart.getUnitPrice(),cart.getQuantity());
                                    Call<OrderDetail> orderDetailCall = orderDetailService.createOrderDetail(orderDetail);
                                    orderDetailCall.enqueue(new Callback<OrderDetail>() {
                                        @Override
                                        public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                                            if(response.body() != null){
                                                updateFlowerStock(cart);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<OrderDetail> call, Throwable t) {

                                        }
                                    });
                                }
                                deleteCart();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                                        intent.putExtra("redirect_to", "orders");
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<Order> call, Throwable t) {

                        }
                    });
                } else {
                    List<Order> orders = new ArrayList<>();
                    for (int i = 0; i < response.body().length; i++) {
                        orders.add(response.body()[i]);
                    }
                    long idMax = orders.stream().max(Comparator.comparingLong(o -> o.getId())).get().getId();
                    double total = 0;
                    for (Cart c:carts) {
                        total += c.getUnitPrice() *  c.getQuantity();
                    }
                    Order order = new Order(idMax + 1,userId, new Date().toString(),new Date().toString(),total, OrderStatus.PAID.toString());
                    final long orderId = idMax + 1;
                    Call<Order> orderCreateCall = orderService.createOrder(order);
                    orderCreateCall.enqueue(new Callback<Order>() {
                        @Override
                        public void onResponse(Call<Order> call, Response<Order> response) {
                            if(response.body() != null){
                                for (Cart cart:carts) {
                                    OrderDetail orderDetail = new OrderDetail(orderId,cart.getIdFlower(),cart.getUnitPrice(),cart.getQuantity());
                                    Call<OrderDetail> orderDetailCall = orderDetailService.createOrderDetail(orderDetail);
                                    orderDetailCall.enqueue(new Callback<OrderDetail>() {
                                        @Override
                                        public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                                            if(response.body() != null){
                                                updateFlowerStock(cart);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<OrderDetail> call, Throwable t) {

                                        }
                                    });
                                }
                                deleteCart();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                                        intent.putExtra("redirect_to", "orders");
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<Order> call, Throwable t) {
                            Log.e("Order Creation", "Failed to create order: " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Order[]> call, Throwable t) {
                Log.e("Orders Fetch", "Failed to fetch orders: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cart) {
            startActivity(new Intent(CheckoutActivity.this, ViewCartActivity.class));
        }
        else if (item.getItemId() == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(CheckoutActivity.this, SignInActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}