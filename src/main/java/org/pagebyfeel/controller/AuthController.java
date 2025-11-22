package org.pagebyfeel.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pagebyfeel.dto.request.RefreshTokenRequest;
import org.pagebyfeel.dto.request.UpdateUserRequest;
import org.pagebyfeel.dto.response.AuthResponse;
import org.pagebyfeel.dto.response.UserResponse;
import org.pagebyfeel.security.oauth.CustomOAuth2User;
import org.pagebyfeel.service.AuthService;
import org.pagebyfeel.service.UserService;
import org.pagebyfeel.util.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomOAuth2User user,
            HttpServletRequest request
    ) {
        if (user != null) {
            String authHeader = request.getHeader("Authorization");
            String accessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }

            authService.logout(user.getUserId(), accessToken);
        }
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomOAuth2User user) {
        UserResponse response = userService.getUserInfo(user.getUserId());
        return ApiResponse.success(response);
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User user,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse updated = userService.updateUser(user.getUserId(), request);
        return ApiResponse.success(updated);
    }
}
