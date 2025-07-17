package vn.edu.fpt.androidapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AndroidAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AndroidAppApplication.class, args);
    }


}
