package vn.edu.fpt.shopapp.entity;

import java.util.Objects;

public class Food {
    private int id;
    private String name;
    private double price;
    private boolean status;
    private String imageUrl;

    private String category;

    public Food() {
    }

    public Food(int id, String name, double price, boolean status, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = status;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public double getPrice() {return price;}

    public void setPrice(double price) {this.price = price;}

    public boolean isStatus() {return status;}

    public void setStatus(boolean status) {this.status = status;}

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Food food = (Food) obj;
        return id == food.id; // hoặc so sánh theo id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // id là thuộc tính duy nhất phân biệt món ăn
    }

}
