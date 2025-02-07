package com.nevc.api.video_streaming.services;

import com.nevc.api.video_streaming.entities.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);
    User saveUser(User user);
}
