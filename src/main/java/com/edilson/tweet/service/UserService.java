package com.edilson.tweet.service;

import com.edilson.tweet.dto.CreateUserDto;
import com.edilson.tweet.entitie.Role;
import com.edilson.tweet.entitie.User;
import com.edilson.tweet.repository.RoleRepository;
import com.edilson.tweet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void createUser(CreateUserDto userDto) {
        var basicRole = roleRepository.findByName(Role.values.BASIC.name());
        var user = userRepository.findByUsername(userDto.username());

        if (user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var newUser = new User();
        newUser.setUsername(userDto.username());
        newUser.setPassword(passwordEncoder.encode(userDto.password()));
        newUser.setRoles(Set.of(basicRole));

        userRepository.save(newUser);
    }
}
