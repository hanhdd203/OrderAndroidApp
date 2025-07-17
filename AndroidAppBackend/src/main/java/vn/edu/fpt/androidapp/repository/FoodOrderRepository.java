package vn.edu.fpt.androidapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.androidapp.entity.FoodOrder;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Integer> {
    List<FoodOrder> findByUser_IdOrderByOrderIdDesc(int userId);
    @Query(value = "SELECT * FROM orders o " +
            "ORDER BY CASE WHEN o.status = N'Đang xử lý' THEN 0 ELSE 1 END, o.order_id DESC",
            nativeQuery = true)
    List<FoodOrder> findAllPrioritizeDangXuLyNative();

}
