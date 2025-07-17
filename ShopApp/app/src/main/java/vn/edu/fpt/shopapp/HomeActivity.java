package vn.edu.fpt.shopapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.adapter.FoodAdapter;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class HomeActivity extends AppCompatActivity implements FoodAdapter.OnImageSelectedListener {

    RecyclerView recyclerView;
    FoodAdapter foodAdapter;
    List<Food> foodList;

    EditText searchEditText;
    Button searchButton;
    private static final int REQUEST_CODE_PICK_IMAGE = FoodAdapter.REQUEST_CODE_PICK_IMAGE;
    private int editFoodPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        recyclerView = findViewById(R.id.listRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);



        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Food>> call;

        User user = SessionManager.getInstance().getCurrentUser();
        if(user.getRole().equalsIgnoreCase("admin")){
            call = apiService.getFoodAll();
        }else{
            call = apiService.getFoods();
        }

        call.enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodList = response.body();
                    foodAdapter = new FoodAdapter(HomeActivity.this, foodList);

                    recyclerView.setAdapter(foodAdapter);

                    foodAdapter.setOnImageSelectedListener(position -> {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                    });
                } else {
                    Log.e("API", "Lỗi khi lấy food: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e("API", "Lỗi kết nối: " + t.getMessage());
            }
        });

        searchButton.setOnClickListener(v -> {
            String keyword = searchEditText.getText().toString().trim().toLowerCase();

            if (!keyword.isEmpty()) {
                List<Food> filteredList = new ArrayList<>();
                for (Food food : foodList) {
                    if (food.getName().toLowerCase().contains(keyword)) {
                        filteredList.add(food);
                    }
                }

                // cập nhật adapter với danh sách đã lọc
                foodAdapter = new FoodAdapter(HomeActivity.this, filteredList);
                recyclerView.setAdapter(foodAdapter);
                foodAdapter.notifyDataSetChanged();
            } else {
                // Nếu từ khóa rỗng thì hiển thị lại toàn bộ danh sách
                foodAdapter = new FoodAdapter(HomeActivity.this, foodList);
                recyclerView.setAdapter(foodAdapter);
                foodAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onRequestImagePick(int position) {
        editFoodPosition = position;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            Food editingFood = SessionManager.getInstance().getCurrentEditingFood();
            ImageView previewImage = SessionManager.getInstance().getCurrentImageView();

            if (editingFood != null && selectedImageUri != null) {
                editingFood.setImageUrl(selectedImageUri.toString());

                if (previewImage != null) {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.bun)
                            .into(previewImage); // cập nhật ảnh ngay
                }
            }
        }
    }
}