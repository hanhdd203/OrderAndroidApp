package vn.edu.fpt.shopapp.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int orderId;
    private User user;
    private String time;
    private TableOrder tableOrder;
    private String status;

    private String note;
    private List<OrderDetail> listFood;

    public Order() {}

    public Order(int orderId, User user, String time, TableOrder tableOrder, String status,String note, List<OrderDetail> listFood) {
        this.orderId = orderId;
        this.user = user;
        this.time = time;
        this.tableOrder = tableOrder;
        this.status = status;
        this.listFood = listFood;
        this.note = note;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getOrderId() {return orderId;}

    public void setOrderId(int orderId) {this.orderId = orderId;}

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTime() {return time;}

    public void setTime(String time) {this.time = time;}

    public TableOrder getTableOrder() {
        return tableOrder;
    }

    public void setTableOrder(TableOrder tableOrder) {
        this.tableOrder = tableOrder;
    }

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public List<OrderDetail> getListFood() {return listFood;}

    public void setListFood(List<OrderDetail> listFood) {this.listFood = listFood;}
}
