package vn.edu.fpt.shopapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.TableOrder;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.entity.dto.FoodOrderDetailDTO;
import vn.edu.fpt.shopapp.entity.dto.OrderRequestDTO;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.FileUtils;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private Context context;
    private List<Food> foodList;

    // URI ảnh được chọn trong dialog edit
    private Uri editDialogImageUri = null;

    // Interface để truyền sự kiện chọn ảnh lên Activity
    public interface OnImageSelectedListener {
        void onRequestImagePick(int position);
    }

    private OnImageSelectedListener imageSelectedListener;

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.imageSelectedListener = listener;
    }


    public FoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvFoodPrice, tvFoodStatus;
        Button addToCartButton, orderButton, editButton;

        public FoodViewHolder(View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.foodImageView);
            tvFoodName = itemView.findViewById(R.id.foodNameTextView);
            tvFoodPrice = itemView.findViewById(R.id.foodPriceTextView);
            tvFoodStatus = itemView.findViewById(R.id.foodStatusTextView);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            orderButton = itemView.findViewById(R.id.orderButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        final int pos = position;
        Food food = foodList.get(pos);
        holder.tvFoodName.setText(food.getName());
        holder.tvFoodPrice.setText("Giá: " + food.getPrice() + " VNĐ");
        holder.tvFoodStatus.setText(food.isStatus() ? "Còn bán" : "Ngừng bán");

        User user = SessionManager.getInstance().getCurrentUser();
        if (user.getRole().equalsIgnoreCase("user")) {
            holder.addToCartButton.setVisibility(food.isStatus() ? View.VISIBLE : View.GONE);
            holder.orderButton.setVisibility(food.isStatus() ? View.VISIBLE : View.GONE);
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
//            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        // Load ảnh từ URL bằng Glide
        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bun)
                .into(holder.ivFoodImage);

        // Xử lý nút Đặt món
        holder.orderButton.setOnClickListener(v -> showOrderDialog(food));

        // Nút Thêm vào giỏ
        holder.addToCartButton.setOnClickListener(v -> {
            HashMap<Food, Integer> mapFood = SessionManager.getInstance().getFoodCart().getFoodList();
            mapFood.put(food, mapFood.getOrDefault(food, 0) + 1);
            Toast.makeText(context, "Đã thêm " + food.getName() + " vào yêu thích", Toast.LENGTH_SHORT).show();
        });


        // Sửa món
        holder.editButton.setOnClickListener(v -> {
            editDialogImageUri = null; // reset ảnh chọn mới mỗi lần mở dialog
            showEditFoodDialog(context, position, food, updatedFood -> {
                notifyItemChanged(position);
                // Gọi API update database nếu cần
            });
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    private void showEditFoodDialog(Context context, int position, Food food, Consumer<Food> onFoodUpdated) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_food, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);
        Button selectImageButton = dialogView.findViewById(R.id.selectImageButton);
        CheckBox chkStatus = dialogView.findViewById(R.id.availableCheckBox);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // Set dữ liệu cũ
        nameEditText.setText(food.getName());
        priceEditText.setText(String.valueOf(food.getPrice()));
        chkStatus.setChecked(food.isStatus());

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bun)
                .into(previewImage);

        AlertDialog dialog = builder.create();

        //  Lưu food đang được chỉnh sửa
        SessionManager.getInstance().setCurrentEditingFood(food);
        SessionManager.getInstance().setCurrentImageView(previewImage);

        selectImageButton.setOnClickListener(v -> {
            if (imageSelectedListener != null) {
                imageSelectedListener.onRequestImagePick(position);
            } else {
                Toast.makeText(context, "Không thể chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();
            boolean status = chkStatus.isChecked();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            food.setName(name);
            food.setPrice(price);
            food.setStatus(status);

            if (editDialogImageUri != null) {
                food.setImageUrl(editDialogImageUri.toString());
            }

            // Gọi API cập nhật
            updateFoodApi(food, success -> {
                if (success) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    onFoodUpdated.accept(food);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            });

        });

        dialog.show();
    }

    private void updateFoodApi(Food food, Consumer<Boolean> callback) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.updateFood(food);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                callback.accept(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.accept(false);
            }
        });
    }

    private void showOrderDialog(Food food) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        Spinner spinnerTable = dialogView.findViewById(R.id.spinnerTable);
        EditText edtSoLuong = dialogView.findViewById(R.id.numberEditText);
        TextView tvTongTien = dialogView.findViewById(R.id.moneySumTextView);
        Button btnXacNhan = dialogView.findViewById(R.id.confirmButton);
        AlertDialog dialog = builder.create();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Gọi API lấy danh sách bàn
        apiService.getTableOrder().enqueue(new Callback<List<TableOrder>>() {
            @Override
            public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TableOrder> tables = response.body();

                    // Hiển thị danh sách bàn lên Spinner
                    List<String> tableNames = new ArrayList<>();
                    for (TableOrder table : tables) {
                        tableNames.add(table.getNameTable());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, tableNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTable.setAdapter(adapter);

                    // Xử lý khi nhấn xác nhận đặt món
                    btnXacNhan.setOnClickListener(v -> {
                        String soLuongStr = edtSoLuong.getText().toString().trim();
                        int selectedTableIndex = spinnerTable.getSelectedItemPosition();

                        if (soLuongStr.isEmpty() || selectedTableIndex < 0) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int soLuong = Integer.parseInt(soLuongStr);
                        double tongTien = soLuong * food.getPrice();
                        TableOrder selectedTable = tables.get(selectedTableIndex);

                        // Tạo OrderRequestDTO
                        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
                        orderRequestDTO.setUserId(SessionManager.getInstance().getCurrentUser().getId());
                        orderRequestDTO.setTableOrder(selectedTable);

                        List<FoodOrderDetailDTO> listFood = new ArrayList<>();
                        listFood.add(new FoodOrderDetailDTO(food.getId(), soLuong, "Đang xử lý"));
                        orderRequestDTO.setListFood(listFood);

                        // Cập nhật trạng thái bàn và gửi order
                        handleTableAndOrder(apiService, selectedTable, orderRequestDTO, tongTien, food, dialog);
                    });

                } else {
                    Toast.makeText(context, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TableOrder>> call, Throwable t) {
                Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Tự động tính tổng tiền khi nhập số lượng
        edtSoLuong.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int soLuong = Integer.parseInt(s.toString());
                    int tongTien = (int) (soLuong * food.getPrice());
                    tvTongTien.setText("Tổng tiền: " + tongTien + " VNĐ");
                } catch (NumberFormatException e) {
                    tvTongTien.setText("Tổng tiền: 0 VNĐ");
                }
            }
        });

        dialog.show();
    }

    private void handleTableAndOrder(ApiService apiService, TableOrder selectedTable, OrderRequestDTO orderRequestDTO,
                                     double tongTien, Food food, AlertDialog dialog) {

        selectedTable.setStatus(false); // cập nhật trạng thái

        // Gửi yêu cầu cập nhật trạng thái bàn
        apiService.updateTableStatus(selectedTable).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, "Lỗi cập nhật trạng thái bàn: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối cập nhật bàn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Gửi yêu cầu tạo order
        apiService.createOrder(orderRequestDTO).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context,
                            "Đã đặt " + orderRequestDTO.getListFood().get(0).getQuantity() + " x " + food.getName() +
                                    " tại " + selectedTable.getNameTable() + ". Tổng: " + tongTien + " VNĐ",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Lỗi khi gửi order: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối gửi order: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
