package vn.edu.fpt.shopapp.entity.dto;

import java.util.List;

import vn.edu.fpt.shopapp.entity.TableOrder;

public class OrderRequestDTO {
    private int userId;
    private TableOrder tableOrder;
    private String status;
    private String note;
    private List<FoodOrderDetailDTO> listFood;

    public OrderRequestDTO() {
    }

    public OrderRequestDTO(int userId, TableOrder tableOrder, String status,String note, List<FoodOrderDetailDTO> listFood) {
        this.userId = userId;
        this.tableOrder = tableOrder;
        this.status = status;
        this.listFood = listFood;
        this.note = note;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public TableOrder getTableOrder() {
        return tableOrder;
    }

    public void setTableOrder(TableOrder tableOrder) {
        this.tableOrder = tableOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FoodOrderDetailDTO> getListFood() {
        return listFood;
    }

    public void setListFood(List<FoodOrderDetailDTO> listFood) {
        this.listFood = listFood;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
