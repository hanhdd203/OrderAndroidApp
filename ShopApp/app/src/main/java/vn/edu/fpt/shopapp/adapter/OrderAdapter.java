package vn.edu.fpt.shopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.entity.Order;
import vn.edu.fpt.shopapp.entity.OrderDetail;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;  // Danh sách các đơn hàng cần hiển thị
    private Context context;

    public OrderAdapter(Context context, List<Order> orders) {
        this.orders = orders;
        this.context = context;
    }
    // Hàm tạo ViewHolder, inflate layout item_order_history.xml
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }
    // Hàm gán dữ liệu vào từng ViewHolder
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);// Lấy đơn hàng tại vị trí position

        // Chuyển String thời gian thành đối tượng LocalDateTime để định dạng lại hiển thị đẹp hơn
        LocalDateTime dateTime = LocalDateTime.parse(order.getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        holder.orderDateTextView.setText("Ngày đặt: " + dateTime.format(formatter));
        // Hiển thị tên bàn đặt
        holder.tableTextView.setText("Bàn: " + order.getTableOrder().getNameTable());
        holder.statusTextView.setText("Trạng thái: " + order.getStatus());

        double total = totalOrder(order);
        holder.totalOrderTextView.setText("Tổng: " + total + " VNĐ");

        // Khởi tạo adapter con cho danh sách món ăn trong đơn hàng
        OrderItemAdapter itemAdapter = new OrderItemAdapter(context, order.getListFood());
        holder.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.orderItemsRecyclerView.setAdapter(itemAdapter);
        // Nếu có ghi chú thì hiển thị, ngược lại ẩn TextView ghi chú
        String note = order.getNote();
        if (note != null && !note.isEmpty()) {
            holder.noteTextView.setText("Ghi chú: " + note);
            holder.noteTextView.setVisibility(View.VISIBLE);
        } else {
            holder.noteTextView.setVisibility(View.GONE);
        }



    }
    // Trả về số lượng đơn hàng hiện tại
    @Override
    public int getItemCount() {
        return orders.size();
    }

    // Hàm tính tổng tiền đơn hàng, bằng cách cộng tiền từng món x số lượng
    private double totalOrder(Order order) {
        double total = 0;
        for (OrderDetail orderDetail : order.getListFood()) {
            total += orderDetail.getFood().getPrice() * orderDetail.getNumber();
        }
        return total;
    }


    // ViewHolder dùng để giữ reference các view trong item layout để tái sử dụng
    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDateTextView, tableTextView, statusTextView, totalOrderTextView,noteTextView;
        RecyclerView orderItemsRecyclerView;// RecyclerView con để hiển thị chi tiết món ăn trong đơn
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ view từ layout item_order_history.xml
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            tableTextView = itemView.findViewById(R.id.tableTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            orderItemsRecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView);
            totalOrderTextView = itemView.findViewById(R.id.totalOrderTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);

        }
    }

    public void updateData(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }
}
