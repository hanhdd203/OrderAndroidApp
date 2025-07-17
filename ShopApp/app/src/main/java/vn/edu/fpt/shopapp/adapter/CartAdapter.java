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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Map.Entry<Food, Integer>> cartItems;
    private HashMap<Food, Integer> cartMap;
    private Set<Food> selectedFoods = new HashSet<>();

    public interface OnCartChangedListener {
        void onCartChanged(HashMap<Food, Integer> newCartMap);
    }

    private OnCartChangedListener cartChangedListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartChangedListener = listener;
    }

    public CartAdapter(Context context, HashMap<Food, Integer> cartMap) {
        this.context = context;
        this.cartMap = cartMap;
        this.cartItems = new ArrayList<>(cartMap.entrySet());
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView cartFoodNameTextView, cartTotalPriceTextView, quantityTextView;
        ImageView cartImageImageView, removeButton;
        CheckBox selectedCheckBox;
        EditText quantityEditText;
        Button increaseButton, decreaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Map.Entry<Food, Integer> entry = cartItems.get(position);
        Food food = entry.getKey();
        int quantity = entry.getValue();

        holder.cartFoodNameTextView.setText(food.getName());
        holder.quantityEditText.setText(String.valueOf(quantity));
        holder.cartTotalPriceTextView.setText("Tổng: " + (food.getPrice() * quantity) + " VNĐ");

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bun)
                .into(holder.cartImageImageView);

        holder.selectedCheckBox.setChecked(selectedFoods.contains(food));

        // Listener checkbox chọn món
        holder.selectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFoods.add(food);
            } else {
                selectedFoods.remove(food);
            }
            if (cartChangedListener != null) {
                cartChangedListener.onCartChanged(cartMap);
            }
        });

        // Xoá món khỏi giỏ hàng
        holder.removeButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            selectedFoods.remove(food);
            cartMap.remove(food);
            cartItems.remove(pos);
            notifyItemRemoved(pos);

            if (cartChangedListener != null) {
                cartChangedListener.onCartChanged(cartMap);
            }
        });


        // Nút giảm số lượng
        holder.decreaseButton.setOnClickListener(v -> {
            int currentQty = getQuantitySafe(holder);
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                updateQuantity(holder, food, newQty);
            }
        });

        // Nút tăng số lượng
        holder.increaseButton.setOnClickListener(v -> {
            int currentQty = getQuantitySafe(holder);
            int newQty = currentQty + 1;
            updateQuantity(holder, food, newQty);
        });

    }

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
    private int getQuantitySafe(CartViewHolder holder) {
        try {
            return Integer.parseInt(holder.quantityEditText.getText().toString());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void updateQuantity(CartViewHolder holder, Food food, int qty) {
        int position = holder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        holder.quantityEditText.setText(String.valueOf(qty));
        cartMap.put(food, qty);
        cartItems.set(position, new AbstractMap.SimpleEntry<>(food, qty));
        holder.cartTotalPriceTextView.setText("Tổng: " + (food.getPrice() * qty) + " VNĐ");

        if (cartChangedListener != null) {
            cartChangedListener.onCartChanged(cartMap);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public Set<Food> getSelectedFoods() {
        return selectedFoods;
    }

    // Tìm vị trí món ăn trong danh sách hiển thị
    public int getPositionOfFood(Food food) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getKey().equals(food)) {
                return i;
            }
        }
        return -1;
    }

    // Xóa item tại vị trí
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

    public void clearSelectedFoods() {
        selectedFoods.clear();
        notifyDataSetChanged();
    }

}
