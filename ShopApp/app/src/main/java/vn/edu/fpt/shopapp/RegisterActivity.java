package vn.edu.fpt.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.ResponseBody;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;

public class RegisterActivity extends AppCompatActivity {

    private TextView loginLinkTextView;   // TextView để chuyển về màn hình đăng nhập
    private Button registerButton;        // Nút đăng ký

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Gán layout cho activity

        // Gán TextView chuyển hướng về màn hình đăng nhập
        loginLinkTextView = findViewById(R.id.loginLinkTextView);
        loginLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở MainActivity (đăng nhập)
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Gán Button đăng ký
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy dữ liệu từ EditText
                EditText nameEditText = findViewById(R.id.nameEditText);
                EditText phoneEditText = findViewById(R.id.phoneEditText);
                EditText passwordEditText = findViewById(R.id.passwordEditText);

                String name = nameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Kiểm tra xem người dùng đã nhập đủ thông tin chưa
                if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    // Tạo đối tượng User để gửi lên API
                    User user = new User();
                    user.setName(name);
                    user.setPhone(phone);
                    user.setPassword(password);
                    user.setRole("user"); // Mặc định người đăng ký là user

                    // Gọi API để lưu người dùng mới
                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    Call<ResponseBody> call = apiService.saveUser(user);

                    // Thực hiện gọi bất đồng bộ
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    // Đọc phản hồi trả về từ server
                                    String message = response.body().string();
                                    if ("Saved".equalsIgnoreCase(message.trim())) {
                                        Toast.makeText(getApplicationContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                                        // Chuyển về màn hình đăng nhập sau khi đăng ký thành công
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // Kết thúc màn hình hiện tại
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Server trả về lỗi (ví dụ: trùng số điện thoại)
                                Toast.makeText(getApplicationContext(),
                                        "Đăng ký thất bại: Số điện thoại đã tồn tại. Code: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            // Lỗi kết nối đến server
                            Toast.makeText(getApplicationContext(),
                                    "Lỗi kết nối: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
