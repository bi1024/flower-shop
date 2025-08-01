package com.prm.groupproject_flowershop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.prm.groupproject_flowershop.apis.FlowerRepository;
import com.prm.groupproject_flowershop.apis.FlowerService;
import com.prm.groupproject_flowershop.app_services.CredentialService;
import com.prm.groupproject_flowershop.db.AppDatabase;
import com.prm.groupproject_flowershop.db.AppExecutors;
import com.prm.groupproject_flowershop.models.Cart;
import com.prm.groupproject_flowershop.models.Flower;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class FlowerDetailActivity extends AppCompatActivity {
    FlowerService flowerService;
    ImageView ivFlower;
    TextView tvFlowerName, tvFlowerDescription, tvFlowerPrice;
    EditText etQuantity;
    ImageView ivAdd, ivPlus, ivMinus;
    CredentialService credentialService;
    BottomNavigationView bottomNavigationView;
    long userId;
    private static final String CHANNEL_ID = "notification_channel";
    private static final CharSequence CHANNEL_NAME = "Notification Channel";
    private static final String CHANNEL_DESCRIPTION = "Channel for notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_detail);
        ivFlower = (ImageView) findViewById(R.id.ivFlower);
        tvFlowerName = (TextView) findViewById(R.id.tvFlowerName);
        tvFlowerDescription = (TextView) findViewById(R.id.tvFlowerDescription);
        tvFlowerPrice = (TextView) findViewById(R.id.tvFlowerPrice);
        etQuantity = (EditText) findViewById(R.id.etQuantity);
        ivAdd = (ImageView) findViewById(R.id.ivAdd);
        ivPlus = (ImageView) findViewById(R.id.ivPlus);
        ivMinus = (ImageView) findViewById(R.id.ivMinus);
        flowerService = FlowerRepository.getFlowerService();
        Intent intent = getIntent();
        long id = intent.getLongExtra("id", -1);
        if (id != -1) {
            viewFlower(id);
        }
        credentialService = new CredentialService(FlowerDetailActivity.this);
        long userId = credentialService.getCurrentUserId();
        ivPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(etQuantity.getText().toString());
                quantity++;
                etQuantity.setText(quantity + "");
            }
        });
        ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(etQuantity.getText().toString());
                quantity--;
                etQuantity.setText(quantity + "");
            }
        });
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                animateProductToCart(id, userId);
                insertCart(id, userId);
            }
        });
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                startActivity(new Intent(FlowerDetailActivity.this, FlowersList.class));
            }
            if (item.getItemId() == R.id.menu_order) {
                startActivity(new Intent(FlowerDetailActivity.this, FlowersList.class));
            }
            if (item.getItemId() == R.id.menu_map) {
                startActivity(new Intent(FlowerDetailActivity.this, ViewMapActivity.class));
            }
            return true;
        });
    }

    public void viewFlower(long id) {
        try {
            Call<Flower> call = flowerService.getFlower(id);
            call.enqueue(new Callback<Flower>() {
                @Override
                public void onResponse(Call<Flower> call, Response<Flower> response) {
                    if (response.body() != null) {
                        Flower flower = response.body();
                        Picasso.get().load(flower.getImageUrl()).into(ivFlower);
                        tvFlowerName.setText(flower.getFlowerName());
                        tvFlowerDescription.setText(flower.getDescription());
                        tvFlowerPrice.setText("$ " + flower.getUnitPrice());
                    }
                }

                @Override
                public void onFailure(Call<Flower> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            Log.d("Loi", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cart) {
            // start view cat activity
            startActivity(new Intent(FlowerDetailActivity.this, ViewCartActivity.class));
        } else if (item.getItemId() == R.id.menu_logout) {
            // process for logout feature
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(FlowerDetailActivity.this, SignInActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertCart(long idFlower, long idCustomer) {
        try {
            Call<Flower> call = flowerService.getFlower(idFlower);
            call.enqueue(new Callback<Flower>() {
                @Override
                public void onResponse(Call<Flower> call, Response<Flower> response) {
                    if (response.body() != null) {
                        AppDatabase database = Room.databaseBuilder(getApplicationContext(),
                                AppDatabase.class, "app-database").build();
                        Flower flower = response.body();
                        int quantity = 0;
                        if (etQuantity.getText().toString().trim().equals("")) {
                            quantity = 0;
                        } else {
                            quantity = Integer.parseInt(etQuantity.getText().toString());
                        }
                        if (quantity > flower.getUnitInStock()) {
                            Toast.makeText(FlowerDetailActivity.this, "You can buy max " + flower.getUnitInStock(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (quantity <= 0) {
                            etQuantity.setError("Quantity must be larger than 0");
                            return;
                        }
                        Cart cart = new Cart(1, flower.getFlowerName(), flower.getDescription(), flower.getImageUrl(), flower.getUnitPrice(), quantity, idFlower, idCustomer);
                        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    long maxId = database.cartDao().maxId();
                                    cart.setId(maxId + 1);
                                    int quantity = Integer.parseInt(etQuantity.getText().toString());
                                    sendAddToCartNotification(flower.getFlowerName(), quantity);
                                    finish();
                                    List<Cart> cartList = database.cartDao().getAllFlower();
                                    long cartId = cart.getIdFlower();
                                    List<Cart> cartx = cartList.stream().filter(c -> c.getIdFlower() == cartId).collect(Collectors.toList());
                                    if (cartx.isEmpty()) {
                                        cart.setId(maxId + 1);
                                        database.cartDao().insert(cart);
                                        finish();
                                    } else {
                                        for (Cart cart : cartx) {
                                            cart.setQuantity(cart.getQuantity() + quantity);
                                            AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    database.cartDao().update(cart);
                                                    finish();
                                                }
                                            });
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(FlowerDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<Flower> call, Throwable t) {
                    Log.e("error", t.toString());
                }
            });
        } catch (Exception e) {
            Log.d("Loi", e.getMessage());
        }
    }

    private void sendAddToCartNotification(String flowerName, int quantity) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            notificationBuilder = new Notification.Builder(this);
        }

        Notification notification = notificationBuilder
                .setContentTitle("Added to Cart!")
                .setContentText("Added " + quantity + " " + flowerName + " to cart")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(getResources().getColor(R.color.white))
                .build();

        NotificationManager notificationManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager.class);
        }
        if (notificationManager != null) {
            int notificationId = getNotificationId();
            notificationManager.notify(notificationId, notification);
        }
    }

    private int getNotificationId() {
        return (int) new Date().getTime();
    }

//    private void animateProductToCart(long idFlower, long idCustomer) {
//        // Get the coordinates of the product image view
//        int[] productCoordinates = new int[2];
//        ivFlower.getLocationOnScreen(productCoordinates);
//        int startX = productCoordinates[0];
//        int startY = productCoordinates[1];
//
//        int[] cartCoordinates = new int[2];
//        findViewById(R.id.menu_cart).getLocationOnScreen(cartCoordinates);
//        int endX = cartCoordinates[0];
//        int endY = cartCoordinates[1];
//
//        Path path = new Path();
//        path.moveTo(startX, startY);
//        path.quadTo(startX, endY, endX, endY);
//        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(ivFlower,
//                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.2f),
//                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.2f)
//        );
//        scaleAnimator.setDuration(1000);
//
//        ObjectAnimator animator = ObjectAnimator.ofFloat(ivFlower, View.X, View.Y, path);
//        animator.setDuration(1000);
//        animator.setInterpolator(new PathInterpolator(0.4f, 0f, 0.2f, 1f));
//
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animator, scaleAnimator);
//        animatorSet.start();
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                insertCart(idFlower, idCustomer);
//            }
//        });
//    }
}