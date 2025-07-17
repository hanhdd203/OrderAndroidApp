package vn.edu.fpt.androidapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.androidapp.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByPhone(String phone);
}
