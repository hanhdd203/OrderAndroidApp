package vn.edu.fpt.shopapp.services;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.LoginRequest;
import vn.edu.fpt.shopapp.entity.Order;
import vn.edu.fpt.shopapp.entity.TableOrder;
import vn.edu.fpt.shopapp.entity.User;
import vn.edu.fpt.shopapp.entity.dto.OrderRequestDTO;

public interface ApiService {

    @POST("/users")
    Call<ResponseBody> saveUser(@Body User user);
    @POST("users/login")
    Call<User> login(@Body LoginRequest request);
    @GET("foods")
    Call<List<Food>> getFoods();
    @GET("foods/all")
    Call<List<Food>> getFoodAll();
    @GET("orders/table")
    Call<List<TableOrder>> getTableOrder();
    @POST("orders")
    Call<ResponseBody> createOrder(@Body OrderRequestDTO orderRequest);
    @POST("orders/table")
    Call<ResponseBody> updateTableStatus(@Body TableOrder tableOrder);
    @GET("orders")
    Call<List<Order>> getAllOrders();
    @GET("orders/get/{id}")
    Call<List<Order>> getOrdersByUserId(@Path("id") int userId);
    @POST("api/order-details/{id}")
    Call<Void> updateStatus(@Path("id") int id);



    @Multipart
    @POST("foods/add")
    Call<ResponseBody> uploadFood(
            @Part MultipartBody.Part image,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("status") RequestBody status
    );


    // Cập nhật không có ảnh
    @PUT("foods")
    Call<ResponseBody> updateFood(@Body Food food);


    @GET("orders/table/clear")
    Call<List<TableOrder>> getUsingTables();


}
