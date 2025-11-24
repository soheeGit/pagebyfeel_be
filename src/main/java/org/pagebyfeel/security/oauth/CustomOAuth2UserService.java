package org.pagebyfeel.security.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pagebyfeel.entity.user.Provider;
import org.pagebyfeel.entity.user.Role;
import org.pagebyfeel.entity.user.User;
import org.pagebyfeel.exception.auth.AuthErrorCode;
import org.pagebyfeel.exception.common.BusinessException;
import org.pagebyfeel.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
            
            userRequest.getClientRegistration();

            return processOAuth2User(userRequest, oAuth2User);
        } catch (OAuth2AuthenticationException e) {
            log.error("OAuth2 인증 실패: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 에러 발생", e);
            throw new BusinessException(AuthErrorCode.OAUTH_LOGIN_FAILED);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        Provider provider;
        try {
            provider = Provider.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("지원하지 않는 OAuth Provider: {}", registrationId);
            throw new BusinessException(AuthErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
        }

        String email = extractEmail(registrationId, oAuth2User);
        String nickname = extractNickname(registrationId, oAuth2User);
        
        if (email == null || email.isEmpty()) {
            log.error("이메일을 찾을 수 없음. Provider: {}, Attributes: {}", 
                    registrationId, oAuth2User.getAttributes());
            throw new BusinessException(AuthErrorCode.OAUTH_EMAIL_NOT_FOUND);
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    return userRepository.save(
                            User.builder()
                                    .email(email)
                                    .provider(provider)
                                    .role(Role.USER)
                                    .nickname(nickname != null ? nickname : email.split("@")[0])
                                    .build()
                    );
                });

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        return new CustomOAuth2User(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProvider(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes
        );
    }
    
    private String extractEmail(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("email");
            case "kakao" -> {
                Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
                if (kakaoAccount == null) {
                    log.error("kakao_account 정보 없음");
                    yield null;
                }
                yield (String) kakaoAccount.get("email");
            }
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                if (response == null) {
                    log.error("naver response 정보 없음");
                    yield null;
                }
                yield (String) response.get("email");
            }
            default -> {
                log.warn("알 수 없는 provider: {}", registrationId);
                yield oAuth2User.getAttribute("email");
            }
        };
    }

    private String extractNickname(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name");
            case "kakao" -> {
                Map<String, Object> properties = oAuth2User.getAttribute("properties");
                if (properties != null) {
                    yield (String) properties.get("nickname");
                }
                yield null;
            }
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                if (response != null) {
                    yield (String) response.get("name");
                }
                yield null;
            }
            default -> null;
        };
    }
}
