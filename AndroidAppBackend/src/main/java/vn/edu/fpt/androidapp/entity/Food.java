package vn.edu.fpt.androidapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "foods")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name", length = 50, columnDefinition = "nvarchar(255)")
    private String name;
    private double price;
    @Column(name = "status", length = 50, columnDefinition = "nvarchar(255)")
    private boolean status;
    private String imageUrl;
}
