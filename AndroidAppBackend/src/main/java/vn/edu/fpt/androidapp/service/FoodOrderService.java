package vn.edu.fpt.androidapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.androidapp.entity.Food;
import vn.edu.fpt.androidapp.entity.FoodOrder;
import vn.edu.fpt.androidapp.entity.FoodOrderDetail;
import vn.edu.fpt.androidapp.entity.User;
import vn.edu.fpt.androidapp.entity.dto.FoodOrderDetailDTO;
import vn.edu.fpt.androidapp.entity.dto.OrderRequestDTO;
import vn.edu.fpt.androidapp.repository.FoodOrderRepository;
import vn.edu.fpt.androidapp.repository.FoodRepository;
import vn.edu.fpt.androidapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FoodOrderService {
    @Autowired
    private FoodOrderRepository foodOrderRepository;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private FoodRepository foodRepo;

    public List<FoodOrder> getAllOrders() {

        return foodOrderRepository.findAllPrioritizeDangXuLyNative();

    }
    public List<FoodOrder> getAll() {
        return foodOrderRepository.findAll();
    }

    public FoodOrder getOrderById(int id) {
        return foodOrderRepository.findById(id).orElse(null);
    }

    public FoodOrder saveOrder(FoodOrder order) {
        return foodOrderRepository.save(order);
    }

    public void deleteOrder(int id) {
        foodOrderRepository.deleteById(id);
    }

    public List<FoodOrder> getOrdersByUserId(int userId) {
        return foodOrderRepository.findByUser_IdOrderByOrderIdDesc(userId);
    }

    public void createOrder(OrderRequestDTO request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setUser(user);
        foodOrder.setTime(LocalDateTime.now());
        foodOrder.setTableOrder(request.getTableOrder());
        foodOrder.setNote(request.getNote());
        foodOrder.setStatus("Đang xử lý");

        List<FoodOrderDetail> items = new ArrayList<>();

        for (FoodOrderDetailDTO itemDTO : request.getListFood()) {
            Food food = foodRepo.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: ID " + itemDTO.getFoodId()));

            FoodOrderDetail item = new FoodOrderDetail();
            item.setFood(food);
            item.setNumber(itemDTO.getQuantity());
            item.setFoodOrder(foodOrder); // thiết lập mối quan hệ hai chiều
            item.setStatus("Đang chế biến");
            items.add(item);
        }

        foodOrder.setListFood(items);
        foodOrderRepository.save(foodOrder);
    }

    public void updateWholeOrder(int orderId, OrderRequestDTO request) {
        // 1. Lấy đơn hàng cần cập nhật
        FoodOrder foodOrder = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: ID " + orderId));

        // 2. Cập nhật các thông tin cơ bản
        foodOrder.setNote(request.getNote());
        foodOrder.setTableOrder(request.getTableOrder());
        foodOrder.setTime(LocalDateTime.now()); // cập nhật lại thời gian

        // 3. Xóa danh sách món cũ (nếu quan hệ là Cascade.ALL và orphanRemoval = true thì tự động xóa)
        foodOrder.getListFood().clear();

        // 4. Tạo danh sách món mới
        List<FoodOrderDetail> newItems = new ArrayList<>();
        for (FoodOrderDetailDTO itemDTO : request.getListFood()) {
            Food food = foodRepo.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: ID " + itemDTO.getFoodId()));

            FoodOrderDetail item = new FoodOrderDetail();
            item.setFood(food);
            item.setNumber(itemDTO.getQuantity());
            item.setFoodOrder(foodOrder);
            item.setStatus("Đang chế biến");

            newItems.add(item);
        }

        // 5. Gán lại danh sách mới
        foodOrder.getListFood().addAll(newItems);

        // 6. Lưu lại
        foodOrderRepository.save(foodOrder);
    }


}

