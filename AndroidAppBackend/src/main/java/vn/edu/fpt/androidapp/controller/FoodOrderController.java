package vn.edu.fpt.androidapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.androidapp.entity.FoodOrder;
import vn.edu.fpt.androidapp.entity.TableOrder;
import vn.edu.fpt.androidapp.entity.dto.OrderRequestDTO;
import vn.edu.fpt.androidapp.repository.TableOrderRepository;
import vn.edu.fpt.androidapp.service.FoodOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class FoodOrderController {
    @Autowired
    private FoodOrderService foodOrderService;

    @Autowired
    private TableOrderRepository tableOrderRepository;

    @GetMapping
    public List<FoodOrder> getAllOrders() {
        return foodOrderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public FoodOrder getOrder(@PathVariable int id) {
        return foodOrderService.getOrderById(id);
    }

    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO request) {
        foodOrderService.createOrder(request);
        return ResponseEntity.ok("Order created");
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable int id) {
        foodOrderService.deleteOrder(id);
    }

    @GetMapping("/get/{id}")
    public List<FoodOrder> getOrdersByUserId(@PathVariable int id) {
        return foodOrderService.getOrdersByUserId(id);
    }

    @GetMapping("table")
    public ResponseEntity<?> getAllTableOrders() {
        List<TableOrder> tableOrders = tableOrderRepository.findByStatusTrue();
        return ResponseEntity.ok(tableOrders);
    }

    @GetMapping("table/clear")
    public ResponseEntity<?> clearTableOrders() {
//        List<TableOrder> tableOrders = tableOrderRepository.findByStatusFalse();
        List<TableOrder> tableOrders = tableOrderRepository.findAvailableTablesWithAllOrdersCompleted("Hoàn thành");
        return ResponseEntity.ok(tableOrders);
    }

    @PostMapping("table")
    public ResponseEntity<?> updateStatusTable(@RequestBody TableOrder tableOrderUpdated) {
        tableOrderRepository.save(tableOrderUpdated);
        return ResponseEntity.ok("Updated");
    }

    @PutMapping("/{id}")
    public void updateOrder(@PathVariable("id") int orderId,@RequestBody OrderRequestDTO request) {
        foodOrderService.updateWholeOrder(orderId, request);
    }
}

