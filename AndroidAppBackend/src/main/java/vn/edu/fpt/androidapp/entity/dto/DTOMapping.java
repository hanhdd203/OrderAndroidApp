package vn.edu.fpt.androidapp.entity.dto;

import vn.edu.fpt.androidapp.entity.User;

public class DTOMapping {

    public static UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setPhone(user.getPhone());
        userDTO.setRole(user.getRole());
        return userDTO;
    }
}
