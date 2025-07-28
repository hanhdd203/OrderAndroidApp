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

    // Các view dùng để hiển thị doanh thu, số lượng đơn, danh sách đơn
    private TextView totalRevenueTextView, orderCountTextView;
    private RecyclerView ordersRecyclerView;
    private OrderAdapter adapter;

    // RadioGroup để chọn lọc theo ngày hoặc theo tháng
    private RadioGroup radioGroupFilter;
    private RadioButton dayRadio, monthRadio;
    private Button pickDateButton;

    // API service để gọi đến server
    private ApiService apiService;

    // Danh sách tất cả các đơn hàng lấy từ API
    private List<Order> allOrders = new ArrayList<>();

    // Ngày được chọn để lọc đơn hàng
    private LocalDate selectedDate;

    // Định dạng ngày giờ chuẩn ISO để tránh lỗi parse khi có microseconds
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);

        // Ánh xạ view từ layout
        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        orderCountTextView = findViewById(R.id.orderCountTextView);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        radioGroupFilter = findViewById(R.id.radioGroupFilter);
        dayRadio = findViewById(R.id.dayRadio);
        monthRadio = findViewById(R.id.monthRadio);
        pickDateButton = findViewById(R.id.pickDateButton);

        selectedDate = LocalDate.now(); // Mặc định là ngày hiện tại

        // Khởi tạo adapter cho RecyclerView
        adapter = new OrderAdapter(this, new ArrayList<>());
        ordersRecyclerView.setAdapter(adapter);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo đối tượng gọi API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Gọi API lấy danh sách đơn hàng ban đầu
        fetchOrdersFromApi();

        // Bắt sự kiện chọn lọc theo ngày hoặc tháng
        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            // Cập nhật lại text nút chọn ngày/tháng tương ứng
            pickDateButton.setText(checkedId == R.id.dayRadio ? "Chọn ngày" : "Chọn tháng");
            selectedDate = LocalDate.now(); // Reset lại ngày
            updateUI(); // Cập nhật UI sau khi chọn filter
        });

        // Bắt sự kiện click nút chọn ngày/tháng
        pickDateButton.setOnClickListener(v -> showDatePickerDialog());
    }

    // Gọi API lấy toàn bộ đơn hàng từ server
    private void fetchOrdersFromApi() {
        apiService.getAllOrders().enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear(); // Xóa danh sách cũ
                    allOrders.addAll(response.body()); // Thêm dữ liệu mới
                    updateUI(); // Cập nhật giao diện
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

    // Cập nhật danh sách đơn hàng và doanh thu trên giao diện
    private void updateUI() {
        List<Order> filteredOrders;

        // Lọc theo ngày
        if (dayRadio.isChecked()) {
            filteredOrders = filterOrdersByDay(allOrders, selectedDate);
            totalRevenueTextView.setText("Doanh thu ngày " + selectedDate + ": " + formatCurrency(calculateTotalRevenue(filteredOrders)));
        }
        // Lọc theo tháng
        else {
            filteredOrders = filterOrdersByMonth(allOrders, selectedDate.getYear(), selectedDate.getMonthValue());
            totalRevenueTextView.setText("Doanh thu tháng " + selectedDate.getMonthValue() + "/" + selectedDate.getYear() + ": " + formatCurrency(calculateTotalRevenue(filteredOrders)));
        }

        // Cập nhật danh sách đơn hàng lên RecyclerView
        adapter.updateData(filteredOrders);
        orderCountTextView.setText("Có " + filteredOrders.size() + " đơn hàng");

        // Hiển thị thông báo nếu không có đơn hàng
        if (filteredOrders.isEmpty()) {
            Toast.makeText(this, "Không có đơn hàng nào trong thời gian này", Toast.LENGTH_SHORT).show();
        }
    }

    // Hiển thị DatePicker để chọn ngày hoặc tháng
    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        int year = selectedDate.getYear();
        int month = selectedDate.getMonthValue() - 1; // Tháng bắt đầu từ 0
        int day = selectedDate.getDayOfMonth();

        // Nếu lọc theo ngày => chọn ngày
        if (dayRadio.isChecked()) {
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = LocalDate.of(y, m + 1, d); // Lưu ngày đã chọn
                updateUI(); // Cập nhật lại UI
            }, year, month, day).show();
        }
        // Nếu lọc theo tháng => chọn tháng, nhưng ngày là 1
        else {
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = LocalDate.of(y, m + 1, 1);
                updateUI();
            }, year, month, 1).show();
        }
    }

    // Lọc danh sách đơn theo ngày
    private List<Order> filterOrdersByDay(List<Order> orders, LocalDate day) {
        return orders.stream().filter(order -> {
            try {
                LocalDateTime orderDateTime = LocalDateTime.parse(order.getTime(), dateTimeFormatter);
                return orderDateTime.toLocalDate().equals(day); // So sánh ngày
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    // Lọc danh sách đơn theo tháng
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

    // Tính tổng doanh thu từ danh sách đơn hàng
    private double calculateTotalRevenue(List<Order> orders) {
        double total = 0;
        for (Order order : orders) {
            for (OrderDetail detail : order.getListFood()) {
                total += detail.getFood().getPrice() * detail.getNumber(); // Tổng tiền = giá * số lượng
            }
        }
        return total;
    }

    // Định dạng số tiền thành tiền Việt
    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
