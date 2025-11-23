package org.pagebyfeel.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("OAuth2 인증 실패: {}", exception.getMessage(), exception);

        // 에러 메시지 추출
        String errorMessage = "인증에 실패했습니다.";
        String errorCode = "authentication_failed";

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            if (oauth2Exception.getError() != null) {
                errorCode = oauth2Exception.getError().getErrorCode();
                if (oauth2Exception.getError().getDescription() != null) {
                    errorMessage = oauth2Exception.getError().getDescription();
                }
            }
        }

        log.warn("OAuth2 실패 상세 - errorCode: {}, message: {}", errorCode, errorMessage);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "true")
                .queryParam("errorCode", errorCode)
                .queryParam("message", errorMessage)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        log.info("OAuth2 실패 리다이렉트: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
