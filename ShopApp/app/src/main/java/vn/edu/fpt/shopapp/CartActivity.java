package vn.edu.fpt.shopapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.fpt.shopapp.adapter.FoodAdapter;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.FoodCart;
import vn.edu.fpt.shopapp.adapter.CartAdapter;
import vn.edu.fpt.shopapp.entity.Order;
import vn.edu.fpt.shopapp.entity.OrderDetail;
import vn.edu.fpt.shopapp.entity.TableOrder;
import vn.edu.fpt.shopapp.entity.dto.FoodOrderDetailDTO;
import vn.edu.fpt.shopapp.entity.dto.MappingDto;
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

        // Khởi tạo RecyclerView và layout theo chiều dọc
        recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lấy giỏ hàng hiện tại từ SessionManager
        FoodCart foodCart = SessionManager.getInstance().getFoodCart();
        HashMap<Food, Integer> cartMap = foodCart.getFoodList();

        // Khởi tạo adapter cho giỏ hàng và gán vào RecyclerView
        CartAdapter adapter = new CartAdapter(this, cartMap);
        recyclerView.setAdapter(adapter);

        // Gán các view button và text tổng tiền
        Button callOrderButton = findViewById(R.id.callOrderButton);
        TextView totalTextView = findViewById(R.id.totalTextView);

        // Ẩn hiện nút gọi món và tổng tiền tùy theo có món hay không
        if (!cartMap.isEmpty()) {
            callOrderButton.setVisibility(View.VISIBLE);
            totalTextView.setVisibility(View.VISIBLE);
        } else {
            callOrderButton.setVisibility(View.GONE);
            totalTextView.setVisibility(View.GONE);
        }

        // Xử lý khi nhấn nút "Gọi món"
        callOrderButton.setOnClickListener(v -> handleOrderButtonClick(adapter, cartMap, foodCart, totalTextView, callOrderButton));


        // Lắng nghe khi có thay đổi trong giỏ hàng để cập nhật tổng tiền
        adapter.setOnCartChangedListener(new CartAdapter.OnCartChangedListener() {
            @Override
            public void onCartChanged(HashMap<Food, Integer> newCartMap) {
                double totalSelectedPrice = adapter.getTotalSelectedPrice();
                totalTextView.setText("Tổng: " + totalSelectedPrice + " VNĐ");

                // Ẩn/hiện nút gọi món và tổng tiền theo tình trạng giỏ hàng
                callOrderButton.setVisibility(newCartMap.isEmpty() ? View.GONE : View.VISIBLE);
                totalTextView.setVisibility(totalSelectedPrice > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }


    private void handleOrderButtonClick(CartAdapter adapter, HashMap<Food, Integer> cartMap,
                                        FoodCart foodCart, TextView totalTextView, Button callOrderButton) {
        Set<Food> selected = adapter.getSelectedFoods();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Bạn chưa chọn món nào để gọi!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        int userId = SessionManager.getInstance().getCurrentUser().getId();

        // 1. Kiểm tra đơn hàng đang xử lý
        checkOrderByUserId(new FoodAdapter.OrderCallback() {
            @Override
            public void onOrderFound(Order existingOrder) {
                if (existingOrder != null) {
                    //  Có đơn đang xử lý -> cập nhật thêm món
                    updateExistingOrder(apiService, existingOrder, selected, cartMap, adapter, foodCart, totalTextView, callOrderButton);
                } else {
                    //  Không có đơn -> gọi món mới với dialog chọn bàn
                    showNewOrderDialog(apiService, selected, cartMap, adapter, foodCart, totalTextView, callOrderButton);
                }
            }
        });
    }

    public void checkOrderByUserId(FoodAdapter.OrderCallback callback) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Order>> call = apiService.getOrdersByUserId(SessionManager.getInstance().getCurrentUser().getId());

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Order o : response.body()) {
                        if (o.getStatus().equalsIgnoreCase("Đang xử lý")) {
                            callback.onOrderFound(o);
                            return;
                        }
                    }
                    callback.onOrderFound(null); // không có đơn hàng phù hợp
                } else {
                    callback.onOrderFound(null);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e("Error", "No Order");
                callback.onOrderFound(null);
            }
        });
    }
    private void updateExistingOrder(ApiService apiService, Order existingOrder,
                                     Set<Food> selected, HashMap<Food, Integer> cartMap,
                                     CartAdapter adapter, FoodCart foodCart,
                                     TextView totalTextView, Button callOrderButton) {

        // Tạo dialog xác nhận
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_order, null);
        TextView orderSummaryTextView = dialogView.findViewById(R.id.orderSummaryTextView);
        EditText noteEditText = dialogView.findViewById(R.id.noteEditText);
        // Ẩn spinnerTable vì không cần chọn bàn
        Spinner spinnerTable = dialogView.findViewById(R.id.spinnerTable);
        spinnerTable.setVisibility(View.GONE);

        // Tạo nội dung mô tả món gọi
        StringBuilder summary = new StringBuilder("Xác nhận thêm món:\n");
        for (Food food : selected) {
            int qty = cartMap.get(food);
            summary.append("- ").append(food.getName()).append(" x").append(qty).append("\n");
        }
        orderSummaryTextView.setText(summary.toString());

        // Mặc định lấy ghi chú từ đơn hiện tại, hiển thị trong noteEditText
        noteEditText.setText(existingOrder.getNote());

        new AlertDialog.Builder(CartActivity.this)
                .setTitle("Xác nhận cập nhật đơn")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Xử lý khi OK

                    Map<Integer, FoodOrderDetailDTO> foodMap = new HashMap<>();
                    for (OrderDetail detail : existingOrder.getListFood()) {
                        foodMap.put(detail.getFood().getId(), MappingDto.orderDetailToDto(detail));
                    }

                    for (Food food : selected) {
                        int qty = cartMap.get(food);
                        if (foodMap.containsKey(food.getId())) {
                            FoodOrderDetailDTO existing = foodMap.get(food.getId());
                            existing.setQuantity(existing.getQuantity() + qty);
                        } else {
                            foodMap.put(food.getId(), new FoodOrderDetailDTO(food.getId(), qty, "Đang xử lý"));
                        }
                    }

                    OrderRequestDTO updateRequest = new OrderRequestDTO();
                    updateRequest.setUserId(existingOrder.getUser().getId());
                    updateRequest.setTableOrder(existingOrder.getTableOrder());
                    updateRequest.setNote(noteEditText.getText().toString().trim());
                    updateRequest.setListFood(new ArrayList<>(foodMap.values()));

                    apiService.updateOrder(existingOrder.getOrderId(), updateRequest).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CartActivity.this, "Đã thêm món vào đơn đang xử lý", Toast.LENGTH_SHORT).show();
                                clearOrderedItems(selected, adapter, cartMap, foodCart, totalTextView, callOrderButton);
                            } else {
                                Toast.makeText(CartActivity.this, "Lỗi cập nhật đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CartActivity.this, "Lỗi mạng khi cập nhật đơn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showNewOrderDialog(ApiService apiService, Set<Food> selected,
                                    HashMap<Food, Integer> cartMap, CartAdapter adapter,
                                    FoodCart foodCart, TextView totalTextView, Button callOrderButton) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_order, null);
        TextView orderSummaryTextView = dialogView.findViewById(R.id.orderSummaryTextView);
        Spinner spinnerTable = dialogView.findViewById(R.id.spinnerTable);
        EditText noteEditText = dialogView.findViewById(R.id.noteEditText);

        StringBuilder summary = new StringBuilder("Gọi món:\n");
        for (Food food : selected) {
            int qty = cartMap.get(food);
            summary.append("- ").append(food.getName()).append(" x").append(qty).append("\n");
        }
        orderSummaryTextView.setText(summary.toString());

        apiService.getTableOrder().enqueue(new Callback<List<TableOrder>>() {
            @Override
            public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TableOrder> tables = response.body();
                    List<String> tableNames = new ArrayList<>();
                    for (TableOrder table : tables) tableNames.add(table.getNameTable());

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(CartActivity.this,
                            android.R.layout.simple_spinner_item, tableNames);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTable.setAdapter(spinnerAdapter);

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
                                selectedTable.setStatus(false);
                                String note = noteEditText.getText().toString().trim();

                                List<FoodOrderDetailDTO> listFood = new ArrayList<>();
                                final double[] total = {0};
                                for (Food food : selected) {
                                    int qty = cartMap.get(food);
                                    listFood.add(new FoodOrderDetailDTO(food.getId(), qty, "Đang chế biến"));
                                    total[0] += food.getPrice() * qty;
                                }

                                OrderRequestDTO request = new OrderRequestDTO();
                                request.setUserId(SessionManager.getInstance().getCurrentUser().getId());
                                request.setTableOrder(selectedTable);
                                request.setListFood(listFood);
                                request.setNote(note);

                                apiService.updateTableStatus(selectedTable).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {}
                                });

                                apiService.createOrder(request).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(CartActivity.this,
                                                    "Đã gọi món thành công tại " + selectedTable.getNameTable() +
                                                            "\nTổng tiền: " + total[0] + " VNĐ",
                                                    Toast.LENGTH_LONG).show();
                                            clearOrderedItems(selected, adapter, cartMap, foodCart, totalTextView, callOrderButton);
                                        } else {
                                            Toast.makeText(CartActivity.this, "Lỗi gửi đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Toast.makeText(CartActivity.this, "Lỗi kết nối gửi đơn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("Huỷ", null)
                            .show();
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi tải bàn: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TableOrder>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void clearOrderedItems(Set<Food> selected, CartAdapter adapter,
                                   HashMap<Food, Integer> cartMap, FoodCart foodCart,
                                   TextView totalTextView, Button callOrderButton) {
        // Tạo bản sao để tránh sửa đổi collection trong khi duyệt
        Set<Food> foodsToRemove = new HashSet<>(selected);

        for (Food food : foodsToRemove) {
            // Xóa món trong foodCart
            foodCart.removeFood(food);

            // Lấy vị trí món trong adapter để xóa item UI
            int pos = adapter.getPositionOfFood(food);
            if (pos != -1) {
                adapter.removeItemAt(pos);
            }
        }

        // Xóa danh sách món đã chọn trong adapter
        adapter.clearSelectedFoods();

        // Cập nhật tổng tiền mới
        double newTotal = adapter.getTotalSelectedPrice();
        totalTextView.setText("Tổng: " + newTotal + " VNĐ");

        // Ẩn/hiện TextView tổng tiền và nút gọi món tùy theo số món còn lại
        totalTextView.setVisibility(newTotal > 0 ? View.VISIBLE : View.GONE);
        callOrderButton.setVisibility(foodCart.getFoodList().isEmpty() ? View.GONE : View.VISIBLE);
    }



}
