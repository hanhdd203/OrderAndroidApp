package vn.edu.fpt.shopapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.entity.OrderDetail;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    // Danh sách các món trong đơn hàng
    private List<OrderDetail> orderItems;
    // Context để sử dụng trong adapter (ví dụ: để show Toast hoặc start Activity)
    private Context context;

    public OrderItemAdapter(Context context, List<OrderDetail> orderItems) {
        this.orderItems = orderItems;
        this.context = context;
    }

    //  tạo ViewHolder cho từng item (chạy 1 lần cho mỗi item cần tạo)
    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view từ file layout item_order_food_history.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_food_history, parent, false);
        return new OrderItemViewHolder(view);
    }

    //  gán dữ liệu cho từng item (chạy mỗi lần scroll đến item)
    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        // Lấy món tại vị trí hiện tại
        OrderDetail item = orderItems.get(position);

        // Hiển thị tên món ăn
        holder.foodNameTextView.setText(item.getFood().getName());
        // Hiển thị số lượng, ví dụ "x2"
        holder.quantityTextView.setText("x" + item.getNumber());
        // Hiển thị tổng tiền (giá * số lượng) và đơn vị VNĐ
        holder.priceTextView.setText(item.getFood().getPrice() * item.getNumber() + " VNĐ");
        // Hiển thị trạng thái món ăn
        holder.statusTextview.setText(item.getStatus());

        // Nếu người dùng là admin và món đang ở trạng thái "Đang chế biến"
        if(SessionManager.getInstance().getCurrentUser().getRole().equalsIgnoreCase("admin")
                && item.getStatus().equalsIgnoreCase("Đang chế biến")){

            // Hiển thị nút xác nhận
            holder.confirmTextView.setVisibility(View.VISIBLE);

            // Xử lý sự kiện khi nhấn nút xác nhận
            holder.confirmTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Tạo đối tượng ApiService để gọi API
                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    // Gọi API cập nhật trạng thái món ăn, truyền id món ăn
                    Call<Void> call = apiService.updateStatus(item.getOrderDetailId());

                    // Thực hiện gọi API bất đồng bộ
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            // Nếu server trả về thành công
                            if (response.isSuccessful()) {
                                // Hiển thị thông báo cập nhật thành công
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                // Ẩn nút xác nhận đi
                                holder.confirmTextView.setVisibility(View.GONE);
                                // Cập nhật trạng thái sang "Đã giao"
                                holder.statusTextview.setText("Đã giao");

                                // Reload lại Activity để cập nhật giao diện
                                Intent intent = ((Activity) context).getIntent(); // lấy intent hiện tại
                                ((Activity) context).finish();                    // đóng activity hiện tại
                                context.startActivity(intent);                    // khởi động lại activity

                            } else {
                                // Nếu lỗi server, show mã lỗi
                                Toast.makeText(context, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Nếu lỗi mạng hoặc gọi API thất bại
                            Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    // Trả về số lượng món trong danh sách
    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    // Lớp ViewHolder dùng để giữ tham chiếu các view của item, tránh gọi findViewById nhiều lần
    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView, quantityTextView, priceTextView, statusTextview, confirmTextView;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ view từ layout xml
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            statusTextview = itemView.findViewById(R.id.statusTextview);
            confirmTextView = itemView.findViewById(R.id.confirmTextView);
        }
    }
}
