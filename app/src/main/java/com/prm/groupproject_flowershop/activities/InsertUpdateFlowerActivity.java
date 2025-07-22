package com.prm.groupproject_flowershop.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.prm.groupproject_flowershop.R;
import com.prm.groupproject_flowershop.SignInActivity;
import com.prm.groupproject_flowershop.apis.FlowerRepository;
import com.prm.groupproject_flowershop.apis.FlowerService;
import com.prm.groupproject_flowershop.constants.AppConstants;
import com.prm.groupproject_flowershop.models.Flower;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsertUpdateFlowerActivity extends AppCompatActivity {
    private static final int IMAGE_PICKER_REQUEST = 1001;
    private Uri selectedImageUri;
    private ImageView imagePreview;
    private Button btnUploadImage;
    boolean isImageUploading = false;//flag for logic

    EditText etName, etPrice, etUnitInStock, etDescription, etImageUrl;
    Button btnSave, btnCancel;
    Flower updatedFlower;
    FlowerService flowerService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_update_flower);

        etName = findViewById(R.id.etFlowerName);
        etPrice = findViewById(R.id.etPrice);
        etUnitInStock = findViewById(R.id.etUnitInStock);
        etDescription = findViewById(R.id.etDescription);
        etImageUrl = findViewById(R.id.etImageUrlFlower);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        flowerService = FlowerRepository.getFlowerService();

        imagePreview = findViewById(R.id.imagePreview);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        btnUploadImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(IMAGE_PICKER_REQUEST);
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(AppConstants.UPDATED_FLOWER)) {
            updatedFlower = (Flower) intent.getSerializableExtra(AppConstants.UPDATED_FLOWER);
            generateUI(updatedFlower);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isImageUploading) {
                        Toast.makeText(InsertUpdateFlowerActivity.this, "Please wait for image upload to complete.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Flower flower = getFlowerObj();
                    flower.setId(updatedFlower.getId());
                    // update flower
                    updateFlower(flower);
                }
            });
        } else {
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isImageUploading) {
                        Toast.makeText(InsertUpdateFlowerActivity.this, "Please wait for image upload to complete.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // add flower
                    Flower flower = getFlowerObj();
                    addFlower(flower);
                }
            });
        }
    }

    private void addFlower(Flower flower) {
        Call<Flower> call = flowerService.createFlower(flower);
        call.enqueue(new Callback<Flower>() {
            @Override
            public void onResponse(Call<Flower> call, Response<Flower> response) {
                if (response.body() == null) {
                    return;
                }
                Toast.makeText(InsertUpdateFlowerActivity.this, "Insert flower successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(InsertUpdateFlowerActivity.this, AdminFlowerMngActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<Flower> call, Throwable t) {
                Toast.makeText(InsertUpdateFlowerActivity.this, "Insert flower failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFlower(Flower flower) {
        Call<Flower> call = flowerService.updateFlower(updatedFlower.getId(), flower);
        call.enqueue(new Callback<Flower>() {
            @Override
            public void onResponse(Call<Flower> call, Response<Flower> response) {
                if (response.body() == null) {
                    return;
                }
                Toast.makeText(InsertUpdateFlowerActivity.this, "Update flower successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(InsertUpdateFlowerActivity.this, AdminFlowerMngActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<Flower> call, Throwable t) {
                Toast.makeText(InsertUpdateFlowerActivity.this, "Update flower failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateUI(Flower flower) {
        etName.setText(flower.getFlowerName());
        etPrice.setText(flower.getUnitPrice()+"");
        etUnitInStock.setText(flower.getUnitInStock()+"");
        etDescription.setText(flower.getDescription());
        etImageUrl.setText(flower.getImageUrl());
    }
    private Flower getFlowerObj() {
        Flower flower = new Flower();
        flower.setFlowerName(etName.getText().toString());
        flower.setUnitPrice(Double.parseDouble(etPrice.getText().toString()));
        flower.setUnitInStock(Integer.parseInt(etUnitInStock.getText().toString()));
        flower.setDescription(etDescription.getText().toString());
        flower.setImageUrl(etImageUrl.getText().toString());
        return flower;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_home_admin) {
            // profile setting processor
        }
        else if (item.getItemId() == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(InsertUpdateFlowerActivity.this, SignInActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER_REQUEST) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);

            // Upload to Cloudinary
            uploadImageToCloudinary(selectedImageUri);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        isImageUploading = true;
        String cloudName = "dsvllb1am";
        String uploadPreset = "android_preset";

        File file = new File(imageUri.getPath());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/*")))
                .addFormDataPart("upload_preset", uploadPreset)
                .build();

        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                isImageUploading = false;
                runOnUiThread(() -> Toast.makeText(InsertUpdateFlowerActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String imageUrl = json.getString("secure_url");

                        runOnUiThread(() -> {
                            etImageUrl.setText(imageUrl);
                            Toast.makeText(InsertUpdateFlowerActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                        });
                        isImageUploading = false;
                    } catch (JSONException e) {
                        isImageUploading = false;
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}