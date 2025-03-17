    package com.sme.service;

    import com.sme.dto.UserDTO;
    import com.sme.entity.User;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.util.List;

    public interface UserService {

        UserDTO createUser(UserDTO userDTO, MultipartFile file);

        UserDTO getUserById(Long id);

        List<UserDTO> getAllUsers();

        List<UserDTO> getAllInactiveUsers();

        UserDTO updateUser(Long id, UserDTO userDTO, MultipartFile file) throws IOException;

        void softDeleteUser(Long id);

        void restoreUser(Long id);

        void deleteUser(Long id) throws IOException;

        User getUserEntityById(Long id);

        User getUserEntityByUsername(String username);

        User save(User user);

        boolean authenticate(String rawPassword, String storedHashedPassword);


    }
