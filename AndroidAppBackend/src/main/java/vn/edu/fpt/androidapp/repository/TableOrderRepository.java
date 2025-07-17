package vn.edu.fpt.androidapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.androidapp.entity.TableOrder;

import java.util.List;

public interface TableOrderRepository extends JpaRepository <TableOrder, Integer>{
    List<TableOrder> findByStatusTrue();
    List<TableOrder> findByStatusFalse();

    @Query("SELECT t FROM TableOrder t " +
            "WHERE t.status = false AND " +
            "NOT EXISTS (" +
            "   SELECT f FROM FoodOrder f " +
            "   WHERE f.tableOrder = t AND f.status <> :completedStatus" +
            ")")
    List<TableOrder> findAvailableTablesWithAllOrdersCompleted(@Param("completedStatus") String completedStatus);

}
