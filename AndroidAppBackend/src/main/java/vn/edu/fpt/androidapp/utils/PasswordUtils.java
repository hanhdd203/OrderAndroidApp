package vn.edu.fpt.androidapp.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }


    public static void main(String[] args) {
        String hashedPassword = hashPassword("<PASSWORD>");
        System.out.println(hashedPassword);
        System.out.println(checkPassword("<PASSWORD>", hashedPassword));
    }
}
