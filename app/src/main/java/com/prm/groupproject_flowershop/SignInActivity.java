package com.prm.groupproject_flowershop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm.groupproject_flowershop.activities.AdminActivity;
import com.prm.groupproject_flowershop.apis.CustomerRepository;
import com.prm.groupproject_flowershop.apis.CustomerService;
import com.prm.groupproject_flowershop.app_services.CredentialService;
import com.prm.groupproject_flowershop.constants.AppConstants;
import com.prm.groupproject_flowershop.models.Customer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    FirebaseUser firebaseUser;
    private CustomerService customerService;
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView txtRegister;
    CredentialService credentialService;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            if (email.equals(AppConstants.ADMIN_ACCOUNT)) {
                startActivity(new Intent(SignInActivity.this, AdminActivity.class));
                finish();
            } else {
                startActivity(new Intent(SignInActivity.this, FlowersList.class));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnSignin);
        txtRegister = findViewById(R.id.textViewRegister);
        auth = FirebaseAuth.getInstance();
        customerService = CustomerRepository.getCustomerService();
        credentialService = new CredentialService(SignInActivity.this);

        txtRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == txtRegister.getId()) {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        }
        else if (v.getId() == btnLogin.getId()) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            if (!ValidateData(email, password)) {
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // Login successfully, get accountId & redirect to customer page
                            loadAccount(email);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignInActivity.this, "Login Failed! " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private boolean ValidateData(String email, String password) {
        boolean flag = true;

        if (email.isEmpty()) {
            etEmail.setError("Email can not be empty!");
            flag = false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password can not be empty!");
            flag = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email wrong format!");
            flag = false;
        }
        return flag;
    }
    private void loadAccount(String email) {
        try {
            Call<Customer[]> call = customerService.getCustomerByEmail(email);
            call.enqueue(new Callback<Customer[]>() {
                @Override
                public void onResponse(Call<Customer[]> call, Response<Customer[]> response) {
                    Customer[] customers = response.body();
                    if (customers == null || customers.length == 0) {
                        Toast.makeText(SignInActivity.this, "Account does not exist!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Customer customer = customers[0];
                    credentialService.setCurrentUserId(customer.getId());

                    Toast.makeText(SignInActivity.this, "Login sucessfully!", Toast.LENGTH_SHORT).show();

                    if (customer.getEmail().equals(AppConstants.ADMIN_ACCOUNT)) {
                        startActivity(new Intent(SignInActivity.this, AdminActivity.class));
                        finish();
                    } else {
                        Intent intent = new Intent(SignInActivity.this,FlowersList.class);
                        intent.putExtra("idCustomer", customer.getId());
                        startActivity(intent);
                        finish();
                    }
                }
                @Override
                public void onFailure(Call<Customer[]> call, Throwable t) {
                    Toast.makeText(SignInActivity.this, "Get customer failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            Log.d("Get Customer By Email failed", ex.getMessage());
        }
    }
}