package vn.edu.fpt.androidapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.androidapp.entity.FoodOrderDetail;
import vn.edu.fpt.androidapp.service.FoodOrderDetailService;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
public class FoodOrderDetailController {
    @Autowired
    private FoodOrderDetailService detailService;

    @GetMapping
    public List<FoodOrderDetail> getAllDetails() {
        return detailService.getAllDetails();
    }

    @GetMapping("/{id}")
    public FoodOrderDetail getDetail(@PathVariable int id) {
        return detailService.getDetailById(id);
    }

    @PostMapping
    public FoodOrderDetail saveDetail(@RequestBody FoodOrderDetail detail) {
        return detailService.saveDetail(detail);
    }

    @DeleteMapping("/{id}")
    public void deleteDetail(@PathVariable int id) {
        detailService.deleteDetail(id);
    }
    @PostMapping("/{id}")
    public void updateStatusDetail(@PathVariable int id) {
        detailService.updateStatus(id,"Đã giao");
        detailService.checkAndUpdateOrderStatus(detailService.getOrderByDetailId(id));
    }
}
