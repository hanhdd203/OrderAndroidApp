package vn.edu.fpt.androidapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.androidapp.entity.User;
import vn.edu.fpt.androidapp.entity.dto.DTOMapping;
import vn.edu.fpt.androidapp.entity.dto.LoginRequest;
import vn.edu.fpt.androidapp.entity.dto.UserDTO;
import vn.edu.fpt.androidapp.service.UserService;
import vn.edu.fpt.androidapp.utils.PasswordUtils;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping()
    public ResponseEntity<?> getAllUser(){
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : userService.getAllUser()) {
            userDTOS.add(DTOMapping.toUserDTO(user));
        }
        return ResponseEntity.ok(userDTOS);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id){
        return ResponseEntity.ok(DTOMapping.toUserDTO(userService.getUserById(id).orElse(null)) );
    }

    @PostMapping()
    public ResponseEntity<?> saveUser(@RequestBody User user){
        userService.saveUser(user);
        return ResponseEntity.ok("Saved");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByPhone(loginRequest.getPhone());

        boolean login = false;
        login = PasswordUtils.checkPassword(loginRequest.getPassword(), user.getPassword());
        if (user != null && login) {
            return ResponseEntity.ok(DTOMapping.toUserDTO(user));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai số điện thoại hoặc mật khẩu");
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable  int id){
        userService.deleteUser(id);
        return ResponseEntity.ok("Deleted");
    }

}
