package vn.edu.fpt.androidapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodOrderDetailDTO {
    private int foodId;
    private int quantity;
    private String status;
}
