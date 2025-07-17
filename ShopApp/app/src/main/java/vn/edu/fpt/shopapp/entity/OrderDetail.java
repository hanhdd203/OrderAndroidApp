package vn.edu.fpt.shopapp.entity;


public class OrderDetail {
    private int orderDetailId;
    private Food food;
    private int orderID;
    private int number;
    private String status;

    public OrderDetail() {
    }

    public OrderDetail(int orderDetailId, Food food, int orderID, int number, String status) {
        this.orderDetailId = orderDetailId;
        this.food = food;
        this.orderID = orderID;
        this.number = number;
        this.status = status;
    }

    public int getOrderDetailId() {return orderDetailId;}

    public void setOrderDetailId(int orderDetailId) {this.orderDetailId = orderDetailId;}

    public Food getFood() {return food;}

    public void setFood(Food food) {this.food = food;}

    public int getOrderID() {return orderID;}

    public void setOrderID(int orderID) {this.orderID = orderID;}

    public int getNumber() {return number;}

    public void setNumber(int number) {this.number = number;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}
}
