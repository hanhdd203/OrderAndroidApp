package vn.edu.fpt.androidapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.androidapp.entity.FoodOrderDetail;

public interface FoodOrderDetailRepository extends JpaRepository<FoodOrderDetail, Integer> {
}
