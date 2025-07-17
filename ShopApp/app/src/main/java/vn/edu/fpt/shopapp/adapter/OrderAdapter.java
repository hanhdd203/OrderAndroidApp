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

    private List<Order> orders;
    private Context context;

    public OrderAdapter(Context context, List<Order> orders) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);


        LocalDateTime dateTime = LocalDateTime.parse(order.getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        holder.orderDateTextView.setText("Ngày đặt: " + dateTime.format(formatter));

        holder.tableTextView.setText("Bàn: " + order.getTableOrder().getNameTable());
        holder.statusTextView.setText("Trạng thái: " + order.getStatus());

        double total = totalOrder(order);

        holder.totalOrderTextView.setText("Tổng: " + total + " VNĐ");
        OrderItemAdapter itemAdapter = new OrderItemAdapter(context, order.getListFood());
        holder.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.orderItemsRecyclerView.setAdapter(itemAdapter);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private double totalOrder(Order order) {
        double total = 0;
        for (OrderDetail orderDetail : order.getListFood()) {
            total += orderDetail.getFood().getPrice() * orderDetail.getNumber();
        }
        return total;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDateTextView, tableTextView, statusTextView, totalOrderTextView;
        RecyclerView orderItemsRecyclerView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            tableTextView = itemView.findViewById(R.id.tableTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            orderItemsRecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView);
            totalOrderTextView = itemView.findViewById(R.id.totalOrderTextView);
        }
    }

    public void updateData(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }
}
