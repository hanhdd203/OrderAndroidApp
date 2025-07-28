package vn.edu.fpt.androidapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.androidapp.entity.Food;
import vn.edu.fpt.androidapp.repository.FoodRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FoodService {
    @Autowired
    private FoodRepository foodRepository;

    public List<Food> getAllFoods() {
        return foodRepository.findByStatusTrueOrderByIdDesc();
    }
    public List<Food> getAllFoodsNotStatus() {
        return foodRepository.findAllByOrderByIdDesc();
    }

    public Food getFoodById(int id) {
        return foodRepository.findById(id).orElse(null);
    }

    public Food saveFood(Food food){
        return foodRepository.save(food);
    }

    public Food saveFood2(Food food) {
        // Nếu có id và tồn tại trong DB => cập nhật
        Optional<Food> existingFood = foodRepository.findById(food.getId());
        if(existingFood.isPresent()) {
            Food foodToUpdate = existingFood.get();
            foodToUpdate.setName(food.getName());
            foodToUpdate.setPrice(food.getPrice());
            foodToUpdate.setStatus(food.isStatus());
            foodToUpdate.setImageUrl(food.getImageUrl());
            foodToUpdate.setCategory(food.getCategory());
            return foodRepository.save(foodToUpdate);
        }
        return null;
    }

    public void deleteFood(int id) {
        foodRepository.deleteById(id);
    }
}
