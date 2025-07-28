package vn.edu.fpt.shopapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.fpt.shopapp.adapter.OrderAdapter;
import vn.edu.fpt.shopapp.entity.Order;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView orderRecyclerView; // RecyclerView hiển thị danh sách đơn hàng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history); // Gán layout cho activity

        // Gán view từ layout
        orderRecyclerView = findViewById(R.id.orderHistoryRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Sắp xếp theo chiều dọc

        // Lấy thông tin người dùng hiện tại từ session
        User user = SessionManager.getInstance().getCurrentUser();

        // Khởi tạo API service
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Nếu là người dùng thường => chỉ lấy đơn hàng theo ID người dùng
        if (user.getRole().equalsIgnoreCase("user")) {
            Call<List<Order>> call = apiService.getOrdersByUserId(user.getId());

            call.enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> orders = response.body();

                        // Tạo adapter và gán danh sách đơn hàng
                        OrderAdapter adapter = new OrderAdapter(OrderHistoryActivity.this, orders);
                        orderRecyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(OrderHistoryActivity.this,
                                "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {
                    Toast.makeText(OrderHistoryActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("API", "lỗi này: " + t.getMessage());
                }
            });

        } else {
            // Nếu là admin => hiển thị tất cả đơn hàng
            Call<List<Order>> call = apiService.getAllOrders();

            call.enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> orders = response.body();

                        // Tạo adapter và gán danh sách đơn hàng
                        OrderAdapter adapter = new OrderAdapter(OrderHistoryActivity.this, orders);
                        orderRecyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(OrderHistoryActivity.this,
                                "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {
                    Toast.makeText(OrderHistoryActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("API", "lỗi này: " + t.getMessage());
                }
            });
        }
    }
}
