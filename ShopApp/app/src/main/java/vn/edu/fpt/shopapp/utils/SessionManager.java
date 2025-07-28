package vn.edu.fpt.shopapp.utils;

import android.widget.ImageView;
import vn.edu.fpt.shopapp.entity.Food;
import vn.edu.fpt.shopapp.entity.FoodCart;
import vn.edu.fpt.shopapp.entity.User;

public class SessionManager {

    private static SessionManager sessionManager;
    private User currentUser;

    private FoodCart foodCart;
    private ImageView currentImageView;
    private Food currentEditingFood;
    private SessionManager(){
        foodCart = new FoodCart();
    }

    public static SessionManager getInstance(){
        if(sessionManager == null){
            sessionManager = new SessionManager();
        }
        return sessionManager;
    }
    public void setUser(User user){
        this.currentUser = user;
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public void setFoodCart(FoodCart foodCart){
        this.foodCart = foodCart;
    }
    public FoodCart getFoodCart(){
        return foodCart;
    }
    public void setCurrentEditingFood(Food food) {
        this.currentEditingFood = food;
    }

    public Food getCurrentEditingFood() {
        return this.currentEditingFood;
    }
    public void setCurrentImageView(ImageView imageView) {
        this.currentImageView = imageView;
    }

    public ImageView getCurrentImageView() {
        return currentImageView;
    }
}
