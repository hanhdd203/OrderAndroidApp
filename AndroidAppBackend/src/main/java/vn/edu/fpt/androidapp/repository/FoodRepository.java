package vn.edu.fpt.androidapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.androidapp.entity.Food;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Integer> {
    List<Food> findByStatusTrueOrderByIdDesc();

    List<Food> findAllByOrderByIdDesc();
}
