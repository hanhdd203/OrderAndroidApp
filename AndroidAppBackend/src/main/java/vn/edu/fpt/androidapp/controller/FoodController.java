package vn.edu.fpt.androidapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.androidapp.entity.Food;
import vn.edu.fpt.androidapp.service.FoodService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/foods")
public class FoodController {
    @Autowired
    private FoodService foodService;

    @GetMapping
    public ResponseEntity<?> getAllFoods() {
        return ResponseEntity.ok(foodService.getAllFoods());
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllFoodsNotStatus() {
        return ResponseEntity.ok(foodService.getAllFoodsNotStatus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFood(@PathVariable int id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @PostMapping
    public ResponseEntity<?> saveFood(@RequestBody Food food) {
        foodService.saveFood(food);
        return ResponseEntity.ok("Saved");
    }


    @PostMapping("/add")
    public ResponseEntity<?> uploadFood(
            @RequestParam("image") MultipartFile image,
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("status") boolean status
    ) {
        try {
            // Đường dẫn thư mục static/images
            String staticPath = new ClassPathResource("static/images").getFile().getAbsolutePath();

            String originalFilename = image.getOriginalFilename();
            Path destination = Paths.get(staticPath, originalFilename);


            if (!Files.exists(destination)) {
                Files.copy(image.getInputStream(), destination);
            }


            String imageUrl = "http://10.0.2.2:8080/images/" + originalFilename;

            // Tạo món ăn mới
            Food food = new Food();
            food.setName(name);
            food.setPrice(price);
            food.setStatus(status);
            food.setImageUrl(imageUrl); // Gán đường dẫn ảnh (cũ hoặc mới)

            foodService.saveFood(food);

            return ResponseEntity.ok("Đã thêm món ăn thành công");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xử lý ảnh");
        }
    }


    @PutMapping()
    public ResponseEntity<?> updateFood( @RequestBody Food food) {
        foodService.saveFood2(food);
        return ResponseEntity.ok("Updated");
    }



    @PutMapping("/{id}")
    public ResponseEntity<?> updateFoodWithImage(
            @PathVariable("id") int id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("status") boolean status
    ) {
        try {

            Food food = foodService.getFoodById(id);
            food.setName(name);
            food.setPrice(price);
            food.setStatus(status);

            if (image != null && !image.isEmpty()) {
                String staticPath = new ClassPathResource("static/images").getFile().getAbsolutePath();
                String originalFilename = image.getOriginalFilename();

                // Tránh đè ảnh cũ (tuỳ ý)
                Path destination = Paths.get(staticPath, originalFilename);
                Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

                String imageUrl = "http://10.0.2.2:8080/images/" + originalFilename;
                food.setImageUrl(imageUrl);
            }

            foodService.saveFood2(food); // Cập nhật vào DB
            return ResponseEntity.ok("Đã cập nhật món ăn thành công");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xử lý ảnh");
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable int id) {
        foodService.deleteFood(id);
        return ResponseEntity.ok("Deleted");
    }
}