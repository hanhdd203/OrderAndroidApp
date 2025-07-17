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

    private TextView loginLinkTextView;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        loginLinkTextView = (TextView) findViewById(R.id.loginLinkTextView);
        loginLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameEditText = (EditText) findViewById(R.id.nameEditText);
                EditText phoneEditText = (EditText) findViewById(R.id.phoneEditText);
                EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
                String name = nameEditText.getText().toString();
                String phone = phoneEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (name.isEmpty() || phone.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }else{
                    User user = new User();
                    user.setName(name);
                    user.setPhone(phone);
                    user.setPassword(password);
                    user.setRole("user");

                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    Call<ResponseBody> call = apiService.saveUser(user);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String message = response.body().string();
                                    if ("Saved".equalsIgnoreCase(message.trim())) {
                                        Toast.makeText(getApplicationContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                        // Chuyển về màn hình đăng nhập
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // Tùy chọn, để đóng màn hình hiện tại
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Đăng ký thất bại: Số điện thoại đã tồn tại. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }



}
