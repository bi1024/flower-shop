package com.prm.groupproject_flowershop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prm.groupproject_flowershop.app_services.CredentialService;

public class MainActivity extends AppCompatActivity {
    private CredentialService credentialService;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.menu_home){
                startActivity(new Intent(MainActivity.this, FlowersList.class));
            }
            if(item.getItemId()==R.id.menu_order){
                startActivity(new Intent(MainActivity.this, IndividualOrderActivity.class));
            }
            if(item.getItemId()==R.id.menu_map){
                startActivity(new Intent(MainActivity.this, ViewMapActivity.class));
            }
            return true;
        });

        //Test get current logged in UserID
//        credentialService = new CredentialService(MainActivity.this);
//        long currentId = credentialService.getCurrentUserId();
//        TextView txt = findViewById(R.id.txtWelcome);
//        txt.setText("Welcome, userId: "+currentId);

    }
}
