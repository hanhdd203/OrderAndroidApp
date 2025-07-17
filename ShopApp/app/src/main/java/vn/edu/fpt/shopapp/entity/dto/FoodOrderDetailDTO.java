package vn.edu.fpt.shopapp.entity.dto;

public class FoodOrderDetailDTO {
    private int foodId;
    private int quantity;
    private String status;

    public FoodOrderDetailDTO(int foodId, int quantity, String status) {
        this.foodId = foodId;
        this.quantity = quantity;
        this.status = status;
    }

    public FoodOrderDetailDTO() {
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
