package org.pagebyfeel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pagebyfeel.entity.user.Provider;
import org.pagebyfeel.entity.user.Role;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID userId;
    private String email;
    private String nickname;
    private Role role;
    private Provider provider;
}
