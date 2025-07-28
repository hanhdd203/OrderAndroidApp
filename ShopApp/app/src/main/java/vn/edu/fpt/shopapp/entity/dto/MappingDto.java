package vn.edu.fpt.shopapp.entity.dto;

import vn.edu.fpt.shopapp.entity.OrderDetail;

public class MappingDto {

    public static FoodOrderDetailDTO orderDetailToDto(OrderDetail orderDetail){
        FoodOrderDetailDTO orderDetailDTO = new FoodOrderDetailDTO();
        orderDetailDTO.setQuantity(orderDetail.getNumber());
        orderDetailDTO.setFoodId(orderDetail.getFood().getId());
        orderDetailDTO.setStatus(orderDetail.getStatus());
        return orderDetailDTO;
    }

}
