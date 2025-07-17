package vn.edu.fpt.androidapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "food_order_details")
public class FoodOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderDetailId;
    @ManyToOne
    @JoinColumn(name = "orderId")
    @JsonBackReference
    private FoodOrder foodOrder;

    @ManyToOne
    @JoinColumn(name = "foodId")
    private Food food;

    private int number;
    @Column(name = "status", length = 50, columnDefinition = "nvarchar(255)")
    private String status;
}
