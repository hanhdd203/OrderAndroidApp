package vn.edu.fpt.androidapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "orders")
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;
    private LocalDateTime time;
    @ManyToOne
    @JoinColumn(name = "table_id")
    private TableOrder tableOrder;
    @Column(name = "status", length = 50, columnDefinition = "nvarchar(255)")
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
//    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
    @JsonManagedReference
    private List<FoodOrderDetail> listFood;
}
