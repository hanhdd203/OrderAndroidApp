package vn.edu.fpt.shopapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.fpt.shopapp.adapter.OrderAdapter;
import vn.edu.fpt.shopapp.entity.Order;
import vn.edu.fpt.shopapp.entity.OrderDetail;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;

public class AdminOrderActivity extends AppCompatActivity {

    private TextView totalRevenueTextView, orderCountTextView;
    private RecyclerView ordersRecyclerView;
    private OrderAdapter adapter;
    private RadioGroup radioGroupFilter;
    private RadioButton dayRadio, monthRadio;
    private Button pickDateButton;

    private ApiService apiService;
    private List<Order> allOrders = new ArrayList<>();
    private LocalDate selectedDate;

    // Sử dụng formatter tiêu chuẩn ISO để không lỗi microseconds
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);

        // Bind view
        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        orderCountTextView = findViewById(R.id.orderCountTextView);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        radioGroupFilter = findViewById(R.id.radioGroupFilter);
        dayRadio = findViewById(R.id.dayRadio);
        monthRadio = findViewById(R.id.monthRadio);
        pickDateButton = findViewById(R.id.pickDateButton);

        selectedDate = LocalDate.now();

        adapter = new OrderAdapter(this, new ArrayList<>());
        ordersRecyclerView.setAdapter(adapter);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Load initial data
        fetchOrdersFromApi();

        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            pickDateButton.setText(checkedId == R.id.dayRadio ? "Chọn ngày" : "Chọn tháng");
            selectedDate = LocalDate.now();
            updateUI();
        });

        pickDateButton.setOnClickListener(v -> showDatePickerDialog());
    }

    private void fetchOrdersFromApi() {
        apiService.getAllOrders().enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear();
                    allOrders.addAll(response.body());
                    updateUI();
                } else {
                    Toast.makeText(AdminOrderActivity.this, "Lấy dữ liệu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AdminOrderActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        List<Order> filteredOrders;

        if (dayRadio.isChecked()) {
            filteredOrders = filterOrdersByDay(allOrders, selectedDate);
            totalRevenueTextView.setText("Doanh thu ngày " + selectedDate + ": " + formatCurrency(calculateTotalRevenue(filteredOrders)));
        } else {
            filteredOrders = filterOrdersByMonth(allOrders, selectedDate.getYear(), selectedDate.getMonthValue());
            totalRevenueTextView.setText("Doanh thu tháng " + selectedDate.getMonthValue() + "/" + selectedDate.getYear() + ": " + formatCurrency(calculateTotalRevenue(filteredOrders)));
        }

        adapter.updateData(filteredOrders);
        orderCountTextView.setText("Có " + filteredOrders.size() + " đơn hàng");

        if (filteredOrders.isEmpty()) {
            Toast.makeText(this, "Không có đơn hàng nào trong thời gian này", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        int year = selectedDate.getYear();
        int month = selectedDate.getMonthValue() - 1;
        int day = selectedDate.getDayOfMonth();

        if (dayRadio.isChecked()) {
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = LocalDate.of(y, m + 1, d);
                updateUI();
            }, year, month, day).show();
        } else {
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = LocalDate.of(y, m + 1, 1);
                updateUI();
            }, year, month, 1).show();
        }
    }

    private List<Order> filterOrdersByDay(List<Order> orders, LocalDate day) {
        return orders.stream().filter(order -> {
            try {
                LocalDateTime orderDateTime = LocalDateTime.parse(order.getTime(), dateTimeFormatter);
                return orderDateTime.toLocalDate().equals(day);
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private List<Order> filterOrdersByMonth(List<Order> orders, int year, int month) {
        return orders.stream().filter(order -> {
            try {
                LocalDateTime orderDateTime = LocalDateTime.parse(order.getTime(), dateTimeFormatter);
                return orderDateTime.getYear() == year && orderDateTime.getMonthValue() == month;
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private double calculateTotalRevenue(List<Order> orders) {
        double total = 0;
        for (Order order : orders) {
            for (OrderDetail detail : order.getListFood()) {
                total += detail.getFood().getPrice() * detail.getNumber();
            }
        }
        return total;
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
