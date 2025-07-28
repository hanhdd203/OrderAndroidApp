package vn.edu.fpt.shopapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.fpt.shopapp.AdminOrderActivity;
import vn.edu.fpt.shopapp.CartActivity;
import vn.edu.fpt.shopapp.HomeActivity;
import vn.edu.fpt.shopapp.MainActivity;
import vn.edu.fpt.shopapp.OrderHistoryActivity;
import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.adapter.TableAdapter;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.TableOrder;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.FileUtils;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class HeaderFragment extends Fragment {

    private TextView welcomeTextView;
    private ShapeableImageView avatarImageView;

    private String role = "user"; // Vai trò mặc định là user

    private Uri selectedImageUri = null; // Uri của ảnh được chọn (khi thêm món ăn)

    private ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri; // Lưu lại Uri ảnh được chọn
                    updateSelectedImagePreview(); // Cập nhật hiển thị ảnh vừa chọn
                }
            }
    );

    // Hàm cập nhật ảnh xem trước khi chọn ảnh thêm món
    private void updateSelectedImagePreview() {
        if (selectedImageUri != null && imageSelectedImageView != null) {
            imageSelectedImageView.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(selectedImageUri)// Load ảnh từ Uri
                    .placeholder(R.drawable.bun)  // Ảnh mặc định khi đang tải
                    .into(imageSelectedImageView);
        }
    }
    // ImageView trong dialog để xem trước ảnh chọn
    private ImageView imageSelectedImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        welcomeTextView = view.findViewById(R.id.welcomeAppTextView);
        avatarImageView = view.findViewById(R.id.imageView);

        // Lấy thông tin user hiện tại
        User user = SessionManager.getInstance().getCurrentUser();
        role = user.getRole();
        // Set nội dung chào
        if (role.equals("admin")) {
            welcomeTextView.setText("Xin chào Quản trị viên");
        } else {
            welcomeTextView.setText("Chào mừng "+user.getName());
        }

        // Xử lý click vào avatar
        avatarImageView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            MenuInflater inflaterMenu = popup.getMenuInflater();

            if (role.equals("admin")) {
                // Nếu là admin thì inflate menu admin
                inflaterMenu.inflate(R.menu.admin_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_home) {
                        // Chọn Home sẽ chuyển đến HomeActivity
                        Intent intent = new Intent(new Intent(requireActivity(), HomeActivity.class));
                        startActivity(intent);
                        return true;
                    }

                    if (itemId == R.id.add_food) {
                        // Mở dialog thêm món ăn mới
                        showAddFoodDialog();
                        return true;
                    }
                    if (itemId == R.id.order_history) {
                        // Mở màn hình quản lý đơn hàng admin
                        Intent intent = new Intent(new Intent(requireActivity(), AdminOrderActivity.class));
                        startActivity(intent);
                        return true;
                    }

                    if(itemId == R.id.view_order_history){
                        // Mở màn hình xem lịch sử đặt món
                        Intent intent = new Intent(new Intent(requireActivity(), OrderHistoryActivity.class));
                        startActivity(intent);

                        return true;
                    }
                    if(itemId == R.id.clearTable){
                        // Hiển thị dialog dọn bàn
                        showClearTableDialog();
                        return true;
                    }

                    if (itemId == R.id.menu_logout) {
                        // Xử lý Logout
                        Intent intent = new Intent(new Intent(requireActivity(), MainActivity.class));
                        startActivity(intent);
                        SessionManager.getInstance().setUser(null); // Xóa thông tin user trong session
                        Toast.makeText(requireContext(), "Bạn đã đăng xuất", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });

            } else {
                // Nếu là user thì inflate menu người dùng thường
                inflaterMenu.inflate(R.menu.avatar_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    int itemid = item.getItemId();
                    if (itemid == R.id.menu_home) {
                        // Xử lý khi chọn home
                        Intent intent = new Intent(new Intent(requireActivity(), HomeActivity.class));
                        startActivity(intent);
                        return true;
                    }

                    if (itemid == R.id.menu_card) {
                        // Mở giỏ hàng
                        Intent intent = new Intent(new Intent(requireActivity(), CartActivity.class));
                        startActivity(intent);
                        return true;
                    }
                    if (itemid == R.id.menu_order) {
                        // Mở lịch sử đơn hàng
                        Intent intent = new Intent(new Intent(requireActivity(), OrderHistoryActivity.class));
                        startActivity(intent);
                        return true;
                    }


                    if (itemid == R.id.menu_logout) {
                        // Xử lý Logout
                        Intent intent = new Intent(new Intent(requireActivity(), MainActivity.class));
                        startActivity(intent);
                        SessionManager.getInstance().setUser(null);// Xóa user session
                        Toast.makeText(requireContext(), "Bạn đã đăng xuất", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });

            }

            popup.show(); // Hiển thị popup menu
        });

        return view;
    }

    // hiển thị dialog thêm món ăn mới (chỉ admin dùng)
    private void showAddFoodDialog() {
        selectedImageUri = null;// Reset ảnh đã chọn trước đó
        // Inflate layout dialog thêm món
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food, null);
        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        Button selectImageButton = dialogView.findViewById(R.id.selectImageButton);
        imageSelectedImageView = dialogView.findViewById(R.id.imageSelectedImageView);
        CheckBox availableCheckBox = dialogView.findViewById(R.id.availableCheckBox);
        RadioGroup categoryRadioGroup = dialogView.findViewById(R.id.categoryRadioGroup);
        Button addFoodButton = dialogView.findViewById(R.id.addFoodButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Bắt sự kiện chọn ảnh khi nhấn nút chọn ảnh
        selectImageButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        addFoodButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();
            boolean available = availableCheckBox.isChecked();

            // Lấy id radio button được chọn (food hay drink)
            int selectedCategoryId = categoryRadioGroup.getCheckedRadioButtonId();
            if (selectedCategoryId == -1) {
                Toast.makeText(requireContext(), "Vui lòng chọn loại món (Đồ ăn / Thức uống)", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = "";
            if (selectedCategoryId == R.id.foodRadioButton) {
                category = "food";
            } else if (selectedCategoryId == R.id.drinkRadioButton) {
                category = "drink";
            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn loại món", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra thông tin nhập liệ
            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra ảnh đã chọn chưa
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ảnh món ăn", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr); // Chuyển giá từ string sang double
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lấy file ảnh từ Uri
            File file = FileUtils.getFileFromUri(requireContext(), selectedImageUri);
            if (file == null || !file.exists()) {
                Toast.makeText(requireContext(), "Không thể đọc file ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo multipart body cho ảnh
            RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), imageRequestBody);

            // Tạo request body cho các trường text
            RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
            RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));
            RequestBody statusPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(available));
            RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category);

            // Gọi API
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<ResponseBody> call = apiService.uploadFood(imagePart, namePart, pricePart, statusPart,categoryPart);
            // Xử lý callback khi API trả kết quả
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Đã thêm món thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();// Đóng dialog
                        Intent intent = requireActivity().getIntent();
                        requireActivity().finish();
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireContext(), "Thêm món thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("API_ADD", "Error: " + t.getMessage());
                }
            });
        });

        dialog.show();// Hiển thị dialog
    }


    // hiển thị dialog danh sách bàn đang sử dụng để dọn bàn (chỉ admin dùng)
    private void showClearTableDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_clear_table, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerTables);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Danh sách bàn đang sử dụng")
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .create();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        final TableAdapter[] adapter = new TableAdapter[1];
        // Gọi API lấy danh sách bàn đang sử dụng
        apiService.getUsingTables().enqueue(new Callback<List<TableOrder>>() {
            @Override
            public void onResponse(Call<List<TableOrder>> call, Response<List<TableOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TableOrder> tableList = response.body();

                    adapter[0] = new TableAdapter(requireContext(), tableList, table -> {
                        // Cập nhật trạng thái trước khi gửi
                        table.setStatus(true);

                        apiService.updateTableStatus(table).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Đã dọn bàn " + table.getNameTable(), Toast.LENGTH_SHORT).show();
                                    tableList.remove(table); // Xóa bàn vừa dọn khỏi danh sách
                                    adapter[0].notifyDataSetChanged();
                                } else {
                                    Toast.makeText(requireContext(), "Dọn bàn thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    recyclerView.setAdapter(adapter[0]); // Set adapter cho RecyclerView
                } else {
                    Toast.makeText(requireContext(), "Không lấy được danh sách bàn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TableOrder>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }




}
