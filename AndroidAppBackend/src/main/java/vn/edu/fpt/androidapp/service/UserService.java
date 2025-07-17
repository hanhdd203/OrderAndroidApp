package vn.edu.fpt.androidapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.androidapp.entity.User;
import vn.edu.fpt.androidapp.repository.UserRepository;
import vn.edu.fpt.androidapp.utils.PasswordUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUser(){
        return userRepository.findAll();
    }
    public Optional<User> getUserById(int id){
        return userRepository.findById(id);
    }
    public void saveUser(User user){
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));
        userRepository.save(user);
    }
    public void deleteUser(int id){
        userRepository.deleteById(id);
    }

    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

}
