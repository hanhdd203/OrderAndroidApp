package vn.edu.fpt.shopapp.entity;

import java.util.HashMap;
import java.util.List;

public class FoodCart {
    private int id;
    private User user;
    private HashMap<Food, Integer> foodList;

    public FoodCart( User user, HashMap<Food, Integer> foodList) {
        this.user = user;
        this.foodList = foodList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUserID(User user) {
        this.user = user;
    }

    public HashMap<Food, Integer> getFoodList() {
        return foodList;
    }

    public void setFoodList(HashMap<Food, Integer> foodList) {
        this.foodList = foodList;
    }
    public void removeFood(Food food){
        foodList.remove(food);
    }
}
