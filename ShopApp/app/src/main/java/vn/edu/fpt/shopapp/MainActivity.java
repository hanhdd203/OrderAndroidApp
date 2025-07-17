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

    private EditText phoneEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneEditText = (EditText) findViewById(R.id.phoneEditText);
                passwordEditText = (EditText) findViewById(R.id.passwordEditText);

                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if(phone.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Hãy nhập đày đủ số đện thoại và mật khẩu", Toast.LENGTH_LONG).show();
                }else{

                    LoginRequest request = new LoginRequest(phone, password);
                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    Call<User> call = apiService.login(request);

                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                User user = response.body();

                                Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);

                                SessionManager.getInstance().setUser(user);
                                SessionManager.getInstance().setFoodCart(new FoodCart(user, new HashMap<>()));

                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Sai số điện thoại hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        registerLink = (TextView) findViewById(R.id.linkRegisterTextView);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}