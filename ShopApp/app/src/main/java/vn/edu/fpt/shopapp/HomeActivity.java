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
import android.widget.TextView;

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

    TextView foodTextView, drinkTextView;
    EditText searchEditText;
    Button searchButton;

    // Mã request khi chọn ảnh từ thư viện
    private static final int REQUEST_CODE_PICK_IMAGE = FoodAdapter.REQUEST_CODE_PICK_IMAGE;
    private int editFoodPosition = -1; // vị trí món ăn đang chỉnh sửa ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Gán các view từ layout
        recyclerView = findViewById(R.id.listRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        foodTextView = findViewById(R.id.foodTextView);
        drinkTextView = findViewById(R.id.drinkTextView);

        // Khởi tạo Retrofit để gọi API
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Food>> call;

        // Xác định người dùng hiện tại là admin hay nhân viên để lấy danh sách món phù hợp
        User user = SessionManager.getInstance().getCurrentUser();
        if (user.getRole().equalsIgnoreCase("admin")) {
            call = apiService.getFoodAll(); // admin được lấy tất cả món ăn
        } else {
            call = apiService.getFoods(); // nhân viên chỉ lấy món đang bán
        }

        // Gọi API lấy danh sách món ăn
        call.enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodList = response.body();

                    // Gán adapter với danh sách món ăn
                    foodAdapter = new FoodAdapter(HomeActivity.this, foodList);
                    recyclerView.setAdapter(foodAdapter);

                    // Gán sự kiện chọn ảnh cho adapter (chức năng chỉnh sửa ảnh món)
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

        // Bắt sự kiện tìm kiếm món ăn theo tên
        searchButton.setOnClickListener(v -> {
            String keyword = searchEditText.getText().toString().trim().toLowerCase();

            if (!keyword.isEmpty()) {
                // Lọc món theo từ khoá nhập vào
                List<Food> filteredList = new ArrayList<>();
                for (Food food : foodList) {
                    if (food.getName().toLowerCase().contains(keyword)) {
                        filteredList.add(food);
                    }
                }

                // Cập nhật adapter với danh sách đã lọc
                foodAdapter = new FoodAdapter(HomeActivity.this, filteredList);
                recyclerView.setAdapter(foodAdapter);
                foodAdapter.notifyDataSetChanged();
            } else {
                // Nếu không có từ khoá thì hiển thị toàn bộ danh sách gốc
                foodAdapter = new FoodAdapter(HomeActivity.this, foodList);
                recyclerView.setAdapter(foodAdapter);
                foodAdapter.notifyDataSetChanged();
            }
        });

        // Bắt sự kiện lọc món ăn theo loại "food"
        foodTextView.setOnClickListener(v -> {
            List<Food> filteredList = new ArrayList<>();
            for (Food food : foodList) {
                if (food.getCategory().equalsIgnoreCase("food")) {
                    filteredList.add(food);
                }
            }

            foodAdapter = new FoodAdapter(HomeActivity.this, filteredList);
            recyclerView.setAdapter(foodAdapter);
            foodAdapter.notifyDataSetChanged();
        });

        // Bắt sự kiện lọc món ăn theo loại "drink"
        drinkTextView.setOnClickListener(v -> {
            List<Food> filteredList = new ArrayList<>();
            for (Food food : foodList) {
                if (food.getCategory().equalsIgnoreCase("drink")) {
                    filteredList.add(food);
                }
            }

            foodAdapter = new FoodAdapter(HomeActivity.this, filteredList);
            recyclerView.setAdapter(foodAdapter);
            foodAdapter.notifyDataSetChanged();
        });
    }

    // Callback khi người dùng chọn ảnh mới cho món ăn (trong adapter)
    @Override
    public void onRequestImagePick(int position) {
        editFoodPosition = position;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // Nhận kết quả ảnh sau khi người dùng chọn từ thư viện
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            // Lấy món ăn đang chỉnh sửa và imageView từ session
            Food editingFood = SessionManager.getInstance().getCurrentEditingFood();
            ImageView previewImage = SessionManager.getInstance().getCurrentImageView();

            if (editingFood != null && selectedImageUri != null) {
                // Cập nhật đường dẫn ảnh trong đối tượng món ăn
                editingFood.setImageUrl(selectedImageUri.toString());

                // Hiển thị ảnh mới ngay bằng Glide
                if (previewImage != null) {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.bun) // ảnh mặc định nếu chưa có
                            .into(previewImage);
                }
            }
        }
    }
}
