package vn.edu.fpt.shopapp.entity;

public class TableOrder {
    private int tableId;
    private String tableName;
    private Boolean status;

    public TableOrder() {
    }

    public TableOrder(int tableId,String nameTable, Boolean status) {
        this.tableId = tableId;
        this.tableName = nameTable;
        this.status = status;
    }

    public String getNameTable() {
        return tableName;
    }

    public void setNameTable(String nameTable) {
        this.tableName = nameTable;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }
}
