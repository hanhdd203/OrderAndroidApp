package vn.edu.fpt.shopapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import vn.edu.fpt.shopapp.OrderHistoryActivity;
import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.Order;
import vn.edu.fpt.shopapp.entity.OrderDetail;
import vn.edu.fpt.shopapp.entity.TableOrder;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.entity.dto.FoodOrderDetailDTO;
import vn.edu.fpt.shopapp.entity.dto.MappingDto;
import vn.edu.fpt.shopapp.entity.dto.OrderRequestDTO;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.FileUtils;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private Context context;
    private List<Food> foodList; // Danh sách các món ăn

    private Uri editDialogImageUri = null;// URI ảnh được chọn khi sửa món

    // Interface dùng để truyền sự kiện từ Adapter lên Activity (chọn ảnh)
    public interface OnImageSelectedListener {
        void onRequestImagePick(int position);// Khi người dùng chọn ảnh
    }

    private OnImageSelectedListener imageSelectedListener;

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.imageSelectedListener = listener;
    }

    public FoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }
    // ViewHolder đại diện cho 1 item món ăn trong RecyclerView
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvFoodPrice, tvFoodStatus;
        Button addToCartButton, orderButton, editButton;

        public FoodViewHolder(View itemView) {
            super(itemView);
            // Gán các View từ XML
            ivFoodImage = itemView.findViewById(R.id.foodImageView);
            tvFoodName = itemView.findViewById(R.id.foodNameTextView);
            tvFoodPrice = itemView.findViewById(R.id.foodPriceTextView);
            tvFoodStatus = itemView.findViewById(R.id.foodStatusTextView);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            orderButton = itemView.findViewById(R.id.orderButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    // Tạo ViewHolder cho mỗi item
    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    // Hiển thị dữ liệu món ăn lên ViewHolder
    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        final int pos = position;
        Food food = foodList.get(pos);
        // Set thông tin món ăn lên các TextView
        holder.tvFoodName.setText(food.getName());
        holder.tvFoodPrice.setText("Giá: " + food.getPrice() + " VNĐ");
        holder.tvFoodStatus.setText(food.isStatus() ? "Còn bán" : "Ngừng bán");
        // Tuỳ vào quyền user, hiển thị nút khác nhau
        User user = SessionManager.getInstance().getCurrentUser();
        if (user.getRole().equalsIgnoreCase("user")) {
            holder.addToCartButton.setVisibility(food.isStatus() ? View.VISIBLE : View.GONE);
            holder.orderButton.setVisibility(food.isStatus() ? View.VISIBLE : View.GONE);
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
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
        return foodList.size(); // Số lượng item hiển thị
    }

    private void showEditFoodDialog(Context context, int position, Food food, Consumer<Food> onFoodUpdated) {
        // Inflate layout sửa món ăn từ file XML
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_food, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        // Lấy các view từ layout
        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);
        Button selectImageButton = dialogView.findViewById(R.id.selectImageButton);
        CheckBox chkStatus = dialogView.findViewById(R.id.availableCheckBox);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        RadioGroup categoryRadioGroup = dialogView.findViewById(R.id.categoryRadioGroup);

        // Set dữ liệu cũ
        nameEditText.setText(food.getName());
        priceEditText.setText(String.valueOf(food.getPrice()));
        chkStatus.setChecked(food.isStatus());


        if (food.getCategory().equalsIgnoreCase("food")) {
            categoryRadioGroup.check(R.id.foodRadioButton);
        } else if (food.getCategory().equalsIgnoreCase("drink")) {
            categoryRadioGroup.check(R.id.drinkRadioButton);
        }
        // Hiển thị ảnh món cũ
        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bun)
                .into(previewImage);

        AlertDialog dialog = builder.create();

        // Lưu food hiện tại đang chỉnh sửa vào Session
        SessionManager.getInstance().setCurrentEditingFood(food);
        SessionManager.getInstance().setCurrentImageView(previewImage);
        // Khi người dùng bấm chọn ảnh
        selectImageButton.setOnClickListener(v -> {
            if (imageSelectedListener != null) {
                imageSelectedListener.onRequestImagePick(position);
            } else {
                Toast.makeText(context, "Không thể chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        });
        // Khi bấm "Lưu"
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();
            boolean status = chkStatus.isChecked();
            // Kiểm tra dữ liệu nhập
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
            // Lấy loại món (food hoặc drink)
            int selectedCategoryId = categoryRadioGroup.getCheckedRadioButtonId();
            String category = "";
            if (selectedCategoryId == R.id.foodRadioButton) {
                category = "food";
            } else if (selectedCategoryId == R.id.drinkRadioButton) {
                category = "drink";
            } else {
                Toast.makeText(context, "Vui lòng chọn loại món", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật đối tượng Food
            food.setName(name);
            food.setPrice(price);
            food.setStatus(status);
            food.setCategory(category);
            // Nếu người dùng đã chọn ảnh mới, cập nhật URL ảnh
            if (editDialogImageUri != null) {
                food.setImageUrl(editDialogImageUri.toString());
            }
            // Gọi API để lưu thông tin sửa món
            updateFoodApi(food, success -> {
                if (success) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    onFoodUpdated.accept(food); // Cập nhật lại UI
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
                callback.accept(response.isSuccessful()); // Thành công thì trả về true
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.accept(false);// Thất bại thì trả về false
            }
        });
    }

    private void showOrderDialog(Food food) {
        // Inflate layout dialog đặt món
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        // Khai báo các View trong dialog
        Spinner spinnerTable = dialogView.findViewById(R.id.spinnerTable);
        EditText edtSoLuong = dialogView.findViewById(R.id.numberEditText);
        EditText edtNote = dialogView.findViewById(R.id.noteEditText);
        TextView tvTongTien = dialogView.findViewById(R.id.moneySumTextView);
        Button btnXacNhan = dialogView.findViewById(R.id.confirmButton);
        AlertDialog dialog = builder.create();
        int userId = SessionManager.getInstance().getCurrentUser().getId();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        checkOrderByUserId(new OrderCallback() {
            @Override
            public void onOrderFound(Order order) {
                if (order != null) {
                    // Xử lý đơn hàng
                    updateOrder(apiService,edtSoLuong,edtNote,btnXacNhan,food,userId,dialog,order);
                } else {
                    // Không có đơn hàng "Đang xử lý"
                    // Gọi API lấy danh sách bàn
                    loadTablesAndSetupOrder(apiService, spinnerTable, edtSoLuong, edtNote, btnXacNhan, food, userId, dialog);
                }
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

    public interface OrderCallback {
        void onOrderFound(Order order);
    }

    public void checkOrderByUserId(OrderCallback callback) {
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


    private void updateOrder(ApiService apiService,
                                         EditText edtSoLuong, EditText edtNote, Button btnXacNhan,
                                         Food food, int userId, AlertDialog dialog, Order currentOrder) {

        btnXacNhan.setOnClickListener(v -> {
            String soLuongStr = edtSoLuong.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            int soLuong = Integer.parseInt(soLuongStr);
            TableOrder selectedTable = currentOrder.getTableOrder();

            OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
            orderRequestDTO.setUserId(userId);
            orderRequestDTO.setTableOrder(selectedTable);
            orderRequestDTO.setNote(note);


            List<FoodOrderDetailDTO> listFood = new ArrayList<>();
            for(OrderDetail orderDetail : currentOrder.getListFood()){
                listFood.add(MappingDto.orderDetailToDto(orderDetail));
            }
            listFood.add(new FoodOrderDetailDTO(food.getId(), soLuong, "Đang xử lý"));
            orderRequestDTO.setListFood(listFood);
            // gửi api update
            Call<Void> call = apiService.updateOrder(currentOrder.getOrderId(), orderRequestDTO); // truyền orderId ở path + object ở body

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("UPDATE_ORDER", "Cập nhật đơn hàng thành công");
                        Toast.makeText(context,"Đã gọi thêm món thành công",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Log.e("UPDATE_ORDER", "Lỗi cập nhật: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("UPDATE_ORDER", "Gọi API thất bại: " + t.getMessage());
                }
            });

        });
    }


    private void loadTablesAndSetupOrder(ApiService apiService, Spinner spinnerTable,
                                         EditText edtSoLuong, EditText edtNote, Button btnXacNhan,
                                         Food food, int userId, AlertDialog dialog) {
        apiService.getTableOrder().enqueue(new Callback<List<TableOrder>>() {
            @Override
            public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TableOrder> tables = response.body();

                    List<String> tableNames = new ArrayList<>();
                    for (TableOrder table : tables) {
                        tableNames.add(table.getNameTable());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, tableNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTable.setAdapter(adapter);

                    btnXacNhan.setOnClickListener(v -> {
                        String soLuongStr = edtSoLuong.getText().toString().trim();
                        int selectedTableIndex = spinnerTable.getSelectedItemPosition();
                        String note = edtNote.getText().toString().trim();
                        if (soLuongStr.isEmpty() || selectedTableIndex < 0) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int soLuong = Integer.parseInt(soLuongStr);
                        double tongTien = soLuong * food.getPrice();
                        TableOrder selectedTable = tables.get(selectedTableIndex);

                        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
                        orderRequestDTO.setUserId(userId);
                        orderRequestDTO.setTableOrder(selectedTable);
                        orderRequestDTO.setNote(note);
                        List<FoodOrderDetailDTO> listFood = new ArrayList<>();
                        listFood.add(new FoodOrderDetailDTO(food.getId(), soLuong, "Đang xử lý"));
                        orderRequestDTO.setListFood(listFood);

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
    }



    private void handleTableAndOrder(ApiService apiService, TableOrder selectedTable, OrderRequestDTO orderRequestDTO,
                                     double tongTien, Food food, AlertDialog dialog) {
        // 1. Cập nhật trạng thái bàn thành "đang sử dụng" (false = đang có người)
        selectedTable.setStatus(false);

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
                    // Hiển thị thông báo cho người dùng
                    Toast.makeText(context,
                            "Đã đặt " + orderRequestDTO.getListFood().get(0).getQuantity() + " x " + food.getName() +
                                    " tại " + selectedTable.getNameTable() + ". Tổng: " + tongTien + " VNĐ",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();// Đóng dialog
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
