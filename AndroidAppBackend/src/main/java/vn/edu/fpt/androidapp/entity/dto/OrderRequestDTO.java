package vn.edu.fpt.androidapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.fpt.androidapp.entity.TableOrder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {
    private int userId;
    private TableOrder tableOrder;
    private String status;
    private List<FoodOrderDetailDTO> listFood;
}
