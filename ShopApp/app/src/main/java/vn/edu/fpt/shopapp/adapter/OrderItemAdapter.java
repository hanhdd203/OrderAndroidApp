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

    private List<OrderDetail> orderItems;
    private Context context;

    public OrderItemAdapter(Context context, List<OrderDetail> orderItems) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_food_history, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderDetail item = orderItems.get(position);
        holder.foodNameTextView.setText(item.getFood().getName());
        holder.quantityTextView.setText("x" + item.getNumber());
        holder.priceTextView.setText(item.getFood().getPrice() * item.getNumber() + " VNĐ");
        holder.statusTextview.setText(item.getStatus());

        if(SessionManager.getInstance().getCurrentUser().getRole().equalsIgnoreCase("admin")
            && item.getStatus().equalsIgnoreCase("Đang chế biến")){
            holder.confirmTextView.setVisibility(View.VISIBLE);

            holder.confirmTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(context,"Hẹ hẹ hẹ",Toast.LENGTH_SHORT).show();

                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    Call<Void> call = apiService.updateStatus(item.getOrderDetailId()); // itemId là ID món ăn

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                holder.confirmTextView.setVisibility(View.GONE);
                                holder.statusTextview.setText("Đã giao");


                                Intent intent = ((Activity) context).getIntent(); // lấy intent hiện tại
                                ((Activity) context).finish();                    // đóng activity
                                context.startActivity(intent);

                            } else {
                                Toast.makeText(context, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView, quantityTextView, priceTextView, statusTextview,confirmTextView;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            statusTextview = itemView.findViewById(R.id.statusTextview);
            confirmTextView = itemView.findViewById(R.id.confirmTextView);
        }
    }
}
