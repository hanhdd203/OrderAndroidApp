package vn.edu.fpt.androidapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FoodDTO {
    private String name;
    private double price;
    private boolean status;
    private String imageUrl;
}
