package vn.edu.fpt.androidapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.androidapp.entity.FoodOrder;
import vn.edu.fpt.androidapp.entity.FoodOrderDetail;
import vn.edu.fpt.androidapp.repository.FoodOrderDetailRepository;
import vn.edu.fpt.androidapp.repository.FoodOrderRepository;

import java.util.List;

@Service
public class FoodOrderDetailService {
    @Autowired
    private FoodOrderDetailRepository foodOrderDetailRepository;

    @Autowired
    private FoodOrderRepository orderRepository;
    public List<FoodOrderDetail> getAllDetails() {
        return foodOrderDetailRepository.findAll();
    }

    public FoodOrderDetail getDetailById(int id) {
        return foodOrderDetailRepository.findById(id).orElse(null);
    }

    public FoodOrderDetail saveDetail(FoodOrderDetail detail) {
        return foodOrderDetailRepository.save(detail);
    }

    public void deleteDetail(int id) {
        foodOrderDetailRepository.deleteById(id);
    }
    public void updateStatus(int id, String status){
        FoodOrderDetail detail = foodOrderDetailRepository.findById(id).orElse(null);
        detail.setStatus(status);
        foodOrderDetailRepository.save(detail);
    }

    public FoodOrder getOrderByDetailId(int detailId) {
        FoodOrderDetail detail = foodOrderDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món chi tiết"));

        return detail.getFoodOrder(); // ← lấy ra đơn hàng chứa món này
    }

    public void checkAndUpdateOrderStatus(FoodOrder foodOrder) {

        List<FoodOrderDetail> details = foodOrder.getListFood();

        boolean allDelivered = details.stream()
                .allMatch(d -> "Đã giao".equalsIgnoreCase(d.getStatus()));

        if (allDelivered) {
            FoodOrder order = orderRepository.findById(foodOrder.getOrderId()).orElseThrow();
            order.setStatus("Hoàn thành");
            orderRepository.save(order);
        }
    }

}
