package org.pagebyfeel.service;

import org.pagebyfeel.dto.request.UpdateUserRequest;
import org.pagebyfeel.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
    void logout(UUID userId);
    UserResponse getUserInfo(UUID userId);
    UserResponse updateUser(UUID userId, UpdateUserRequest request);
}
