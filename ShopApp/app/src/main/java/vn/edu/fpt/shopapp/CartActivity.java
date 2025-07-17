package vn.edu.fpt.shopapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.FoodCart;
import vn.edu.fpt.shopapp.adapter.CartAdapter;
import vn.edu.fpt.shopapp.entity.TableOrder;
import vn.edu.fpt.shopapp.entity.dto.FoodOrderDetailDTO;
import vn.edu.fpt.shopapp.entity.dto.OrderRequestDTO;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FoodCart foodCart = SessionManager.getInstance().getFoodCart();
        HashMap<Food, Integer> cartMap = foodCart.getFoodList();

        CartAdapter adapter = new CartAdapter(this, cartMap);

        recyclerView.setAdapter(adapter);

        Button callOrderButton = findViewById(R.id.callOrderButton);
        TextView totalTextView = findViewById(R.id.totalTextView);

        if (!cartMap.isEmpty()) {
            callOrderButton.setVisibility(View.VISIBLE);
            totalTextView.setVisibility(View.VISIBLE);
        } else {
            callOrderButton.setVisibility(View.GONE);
            totalTextView.setVisibility(View.GONE);
        }

        callOrderButton.setOnClickListener(v -> {
            Set<Food> selected = adapter.getSelectedFoods();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Bạn chưa chọn món nào để gọi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tóm tắt đơn hàng
            StringBuilder summary = new StringBuilder();
            for (Food food : selected) {
                int qty = cartMap.get(food);
                summary.append("- ").append(food.getName()).append(" x").append(qty).append("\n");
            }

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_order, null);
            TextView orderSummaryTextView = dialogView.findViewById(R.id.orderSummaryTextView);
            Spinner spinnerTable = dialogView.findViewById(R.id.spinnerTable);
            orderSummaryTextView.setText("Gọi món:\n" + summary.toString());

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<List<TableOrder>> call = apiService.getTableOrder();

            call.enqueue(new Callback<List<TableOrder>>() {
                @Override
                public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<TableOrder> tables = response.body();

                        List<String> tableNames = new ArrayList<>();
                        for (TableOrder t : tables) {
                            tableNames.add(t.getNameTable());
                        }

                        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(CartActivity.this,
                                android.R.layout.simple_spinner_item, tableNames);
                        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerTable.setAdapter(adapterSpinner);

                        new AlertDialog.Builder(CartActivity.this)
                                .setTitle("Xác nhận đơn")
                                .setView(dialogView)
                                .setPositiveButton("OK", (dialog, which) -> {
                                    int selectedIndex = spinnerTable.getSelectedItemPosition();
                                    if (selectedIndex < 0) {
                                        Toast.makeText(CartActivity.this, "Vui lòng chọn bàn!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    TableOrder selectedTable = tables.get(selectedIndex);
                                    List<FoodOrderDetailDTO> listFood = new ArrayList<>();
                                    final double[] total = {0};

                                    for (Food food : selected) {
                                        int qty = cartMap.get(food);
                                        listFood.add(new FoodOrderDetailDTO(food.getId(), qty, "Đang chế biến"));
                                        total[0] += food.getPrice() * qty;
                                    }

                                    OrderRequestDTO orderRequest = new OrderRequestDTO();
                                    orderRequest.setUserId(SessionManager.getInstance().getCurrentUser().getId());
                                    orderRequest.setTableOrder(selectedTable);
                                    orderRequest.setListFood(listFood);

                                    selectedTable.setStatus(false);
                                    apiService.updateTableStatus(selectedTable).enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (!response.isSuccessful()) {
                                                Toast.makeText(CartActivity.this, "Không cập nhật được trạng thái bàn", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Toast.makeText(CartActivity.this, "Lỗi kết nối cập nhật bàn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    apiService.createOrder(orderRequest).enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.isSuccessful()) {
                                                Toast.makeText(CartActivity.this,
                                                        "Đã gọi món thành công tại " + selectedTable.getNameTable() +
                                                                "\nTổng tiền: " + total[0] + " VNĐ",
                                                        Toast.LENGTH_LONG).show();

                                                List<Food> foodsToRemove = new ArrayList<>(selected); // bản sao an toàn
                                                for (Food food : foodsToRemove) {
                                                    foodCart.removeFood(food);

                                                    int position = adapter.getPositionOfFood(food);
                                                    if (position != -1) {
                                                        adapter.removeItemAt(position);
                                                    }
                                                }


                                                adapter.clearSelectedFoods();
                                                // Cập nhật lại tổng tiền
                                                double newTotal = adapter.getTotalSelectedPrice();
                                                totalTextView.setText("Tổng: " + newTotal + " VNĐ");

                                                callOrderButton.setVisibility(foodCart.getFoodList().isEmpty() ? View.GONE : View.VISIBLE);
                                                totalTextView.setVisibility(newTotal > 0 ? View.VISIBLE : View.GONE);
                                            } else {
                                                Toast.makeText(CartActivity.this, "Lỗi gửi đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Toast.makeText(CartActivity.this, "Lỗi kết nối gửi đơn hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Huỷ", null)
                                .show();
                    } else {
                        Toast.makeText(CartActivity.this, "Lỗi tải danh sách bàn: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<TableOrder>> call, Throwable t) {
                    Toast.makeText(CartActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        adapter.setOnCartChangedListener(new CartAdapter.OnCartChangedListener() {
            @Override
            public void onCartChanged(HashMap<Food, Integer> newCartMap) {
                double totalSelectedPrice = adapter.getTotalSelectedPrice();
                totalTextView.setText("Tổng: " + totalSelectedPrice + " VNĐ");
                callOrderButton.setVisibility(newCartMap.isEmpty() ? View.GONE : View.VISIBLE);
                totalTextView.setVisibility(totalSelectedPrice > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }




}