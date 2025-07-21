package com.prm.groupproject_flowershop;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Auto redirect after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String redirectTo = getIntent().getStringExtra("redirect_to");
                if ("orders".equals(redirectTo)) {
                    startActivity(new Intent(PaymentSuccessActivity.this, IndividualOrderActivity.class));
                } else {
                    startActivity(new Intent(PaymentSuccessActivity.this, FlowersList.class));
                }
                finish();
            }
        }, 3000); // 3 seconds delay
    }
}