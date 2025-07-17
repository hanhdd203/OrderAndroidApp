package vn.edu.fpt.androidapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", length = 50,columnDefinition = "nvarchar(255)")
    private String name;
    @Column(name = "phone", length = 50, unique = true)
    private String phone;
    @Column(name = "password", length = 255)
    private String password;
    @Column(name = "role", length = 50)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
    @JsonBackReference
    private List<FoodOrder> listOrder;
}
