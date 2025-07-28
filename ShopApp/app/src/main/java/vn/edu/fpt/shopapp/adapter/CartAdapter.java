package vn.edu.fpt.shopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.entity.Food;

// Adapter dùng cho RecyclerView để hiển thị giỏ hàng
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Map.Entry<Food, Integer>> cartItems; // Danh sách các món ăn trong giỏ (kèm số lượng)
    private HashMap<Food, Integer> cartMap; // Map để lưu món ăn và số lượng tương ứng
    private Set<Food> selectedFoods = new HashSet<>(); // Danh sách các món được chọn (checkbox)

    // Interface để lắng nghe sự thay đổi trong giỏ hàng
    public interface OnCartChangedListener {
        void onCartChanged(HashMap<Food, Integer> newCartMap);
    }

    private OnCartChangedListener cartChangedListener;

    // Cho phép thiết lập listener từ bên ngoài
    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartChangedListener = listener;
    }

    // Constructor: nhận vào context và map giỏ hàng ban đầu
    public CartAdapter(Context context, HashMap<Food, Integer> cartMap) {
        this.context = context;
        this.cartMap = cartMap;
        this.cartItems = new ArrayList<>(cartMap.entrySet()); // chuyển sang danh sách để dễ xử lý với RecyclerView
    }

    // ViewHolder giúp giữ các view trong mỗi item
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView cartFoodNameTextView, cartTotalPriceTextView, quantityTextView;
        ImageView cartImageImageView, removeButton;
        CheckBox selectedCheckBox;
        EditText quantityEditText;
        Button increaseButton, decreaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view trong layout item_cart.xml
            cartFoodNameTextView = itemView.findViewById(R.id.cartFoodNameTextView);
            cartTotalPriceTextView = itemView.findViewById(R.id.cartTotalPriceTextView);
            cartImageImageView = itemView.findViewById(R.id.cartImageImageView);
            selectedCheckBox = itemView.findViewById(R.id.selectedCheckBox);
            removeButton = itemView.findViewById(R.id.removeButton);
            quantityEditText = itemView.findViewById(R.id.quantityEditText);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_cart.xml để tạo view cho mỗi item
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // Lấy dữ liệu món ăn ở vị trí hiện tại
        Map.Entry<Food, Integer> entry = cartItems.get(position);
        Food food = entry.getKey();
        int quantity = entry.getValue();

        // Gán dữ liệu cho các view
        holder.cartFoodNameTextView.setText(food.getName());
        holder.quantityEditText.setText(String.valueOf(quantity));
        holder.cartTotalPriceTextView.setText("Tổng: " + (food.getPrice() * quantity) + " VNĐ");

        // Load ảnh bằng thư viện Glide
        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bun)
                .into(holder.cartImageImageView);

        // Set trạng thái của checkbox
        holder.selectedCheckBox.setChecked(selectedFoods.contains(food));

        // Khi tick vào checkbox
        holder.selectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFoods.add(food); // thêm món vào danh sách đã chọn
            } else {
                selectedFoods.remove(food); // bỏ chọn món
            }
            if (cartChangedListener != null) {
                cartChangedListener.onCartChanged(cartMap); // Gọi callback khi có thay đổi
            }
        });

        // Xử lý nút xóa món ăn
        holder.removeButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            selectedFoods.remove(food); // nếu có chọn thì bỏ chọn luôn
            cartMap.remove(food); // xóa khỏi map
            cartItems.remove(pos); // xóa khỏi danh sách hiển thị
            notifyItemRemoved(pos); // thông báo cập nhật giao diện

            if (cartChangedListener != null) {
                cartChangedListener.onCartChanged(cartMap);
            }
        });

        // Xử lý nút giảm số lượng
        holder.decreaseButton.setOnClickListener(v -> {
            int currentQty = getQuantitySafe(holder);
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                updateQuantity(holder, food, newQty); // cập nhật số lượng
            }
        });

        // Xử lý nút tăng số lượng
        holder.increaseButton.setOnClickListener(v -> {
            int currentQty = getQuantitySafe(holder);
            int newQty = currentQty + 1;
            updateQuantity(holder, food, newQty); // cập nhật số lượng
        });
    }

    // Tính tổng giá các món đã chọn
    public double getTotalSelectedPrice() {
        int total = 0;
        for (Food food : selectedFoods) {
            Integer qty = cartMap.get(food);
            if (qty != null) {
                total += food.getPrice() * qty;
            }
        }
        return total;
    }

    // Lấy số lượng an toàn (tránh lỗi khi người dùng nhập linh tinh)
    private int getQuantitySafe(CartViewHolder holder) {
        try {
            int qty = Integer.parseInt(holder.quantityEditText.getText().toString());
            return Math.min(qty, 100); // không cho lớn hơn 100
        } catch (NumberFormatException e) {
            return 1; // nếu nhập sai thì mặc định là 1
        }
    }

    // Hàm cập nhật lại số lượng món ăn trong giỏ
    private void updateQuantity(CartViewHolder holder, Food food, int qty) {
        qty = Math.min(qty, 100); // giới hạn lại nếu lỡ vượt 100
        int position = holder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        holder.quantityEditText.setText(String.valueOf(qty)); // hiển thị lại số mới
        cartMap.put(food, qty); // cập nhật trong map
        cartItems.set(position, new AbstractMap.SimpleEntry<>(food, qty)); // cập nhật trong danh sách
        holder.cartTotalPriceTextView.setText("Tổng: " + (food.getPrice() * qty) + " VNĐ");

        if (cartChangedListener != null) {
            cartChangedListener.onCartChanged(cartMap);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size(); // số lượng item trong giỏ
    }

    // Lấy danh sách món đã chọn
    public Set<Food> getSelectedFoods() {
        return selectedFoods;
    }

    // Tìm vị trí của món ăn trong danh sách
    public int getPositionOfFood(Food food) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getKey().equals(food)) {
                return i;
            }
        }
        return -1;
    }

    // Xóa một item theo vị trí
    public void removeItemAt(int position) {
        if (position >= 0 && position < cartItems.size()) {
            Food foodToRemove = cartItems.get(position).getKey();
            selectedFoods.remove(foodToRemove);
            cartMap.remove(foodToRemove);
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());

            if (cartChangedListener != null) {
                cartChangedListener.onCartChanged(cartMap);
            }
        }
    }

    // Bỏ chọn tất cả các món đã chọn
    public void clearSelectedFoods() {
        selectedFoods.clear();
        notifyDataSetChanged(); // cập nhật lại giao diện
    }

}
