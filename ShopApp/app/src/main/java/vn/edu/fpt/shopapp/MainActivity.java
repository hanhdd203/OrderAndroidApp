package vn.edu.fpt.shopapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.fpt.shopapp.entity.FoodCart;
import vn.edu.fpt.shopapp.entity.LoginRequest;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.services.ApiService;
import vn.edu.fpt.shopapp.services.RetrofitClient;
import vn.edu.fpt.shopapp.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private EditText phoneEditText;      // Ô nhập số điện thoại
    private EditText passwordEditText;   // Ô nhập mật khẩu
    private Button loginButton;          // Nút đăng nhập
    private TextView registerLink;       // TextView dẫn đến trang đăng ký

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Gán layout giao diện

        // Gán view từ layout
        loginButton = (Button) findViewById(R.id.loginButton);

        // Xử lý khi người dùng nhấn nút "Đăng nhập"
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneEditText = (EditText) findViewById(R.id.phoneEditText);
                passwordEditText = (EditText) findViewById(R.id.passwordEditText);

                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Kiểm tra nếu thiếu thông tin
                if (phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Hãy nhập đầy đủ số điện thoại và mật khẩu", Toast.LENGTH_LONG).show();
                } else {
                    // Tạo request đăng nhập
                    LoginRequest request = new LoginRequest(phone, password);

                    // Gọi API thông qua Retrofit
                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    Call<User> call = apiService.login(request);

                    // Gửi yêu cầu đăng nhập
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            // Đăng nhập thành công
                            if (response.isSuccessful() && response.body() != null) {
                                User user = response.body();

                                Toast.makeText(MainActivity.this,
                                        "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                                // Lưu thông tin user và tạo giỏ hàng trống
                                SessionManager.getInstance().setUser(user);
                                SessionManager.getInstance().setFoodCart(new FoodCart(user, new HashMap<>()));

                                // Chuyển sang HomeActivity
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish(); // Kết thúc MainActivity để không quay lại khi nhấn back
                            } else {
                                // Đăng nhập thất bại do sai thông tin
                                Toast.makeText(MainActivity.this,
                                        "Sai số điện thoại hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            // Xử lý lỗi kết nối
                            Toast.makeText(MainActivity.this,
                                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Gán sự kiện khi nhấn vào dòng "Đăng ký tài khoản"
        registerLink = (TextView) findViewById(R.id.linkRegisterTextView);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở trang đăng ký
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
