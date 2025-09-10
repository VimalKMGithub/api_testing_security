package org.vimal.api;


import io.restassured.response.Response;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.vimal.api.ApiCalls.executeRequest;
import static org.vimal.api.Common.waitForResponse;
import static org.vimal.constants.Common.AUTHORIZATION;
import static org.vimal.constants.Common.BEARER;
import static org.vimal.constants.SubPaths.AUTH;
import static org.vimal.enums.RequestMethods.POST;

public final class AuthenticationCalls {
    private AuthenticationCalls() {
    }

    public static Response login(String usernameOrEmail,
                                 String password) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/login",
                        null,
                        Map.of(
                                "usernameOrEmail", usernameOrEmail,
                                "password", password
                        )
                )
        );
    }

    public static Response logout(String accessToken) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/logout",
                        Map.of(AUTHORIZATION, BEARER + accessToken)
                )
        );
    }

    public static Response refreshAccessToken(String refreshToken) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/refresh/accessToken",
                        null,
                        Map.of("refreshToken", refreshToken)
                )
        );
    }

    public static Response revokeAccessToken(String accessToken) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/revoke/accessToken",
                        Map.of(AUTHORIZATION, BEARER + accessToken)
                )
        );
    }

    public static Response revokeRefreshToken(String refreshToken) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/revoke/refreshToken",
                        null,
                        Map.of("refreshToken", refreshToken)
                )
        );
    }

    public static Response requestToToggleMfa(String accessToken,
                                              String type,
                                              String toggle) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/mfa/requestTo/toggle",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        Map.of(
                                "type", type,
                                "toggle", toggle
                        )
                )
        );
    }

    public static Response verifyToggleMfa(String accessToken,
                                           String type,
                                           String toggle,
                                           String otpTotp) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/mfa/verifyTo/toggle",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        Map.of(
                                "type", type,
                                "toggle", toggle,
                                "otpTotp", otpTotp
                        )
                )
        );
    }

    public static Response verifyMfaToLogin(String type,
                                            String stateToken,
                                            String otpTotp) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        AUTH + "/mfa/verifyTo/login",
                        null,
                        Map.of(
                                "type", type,
                                "stateToken", stateToken,
                                "otpTotp", otpTotp
                        )
                )
        );
    }

    public static String getAccessToken(String usernameOrEmail,
                                        String password) throws ExecutionException, InterruptedException {
        Response response = login(
                usernameOrEmail,
                password
        );
        response.then()
                .statusCode(200);
        return response.jsonPath()
                .getString("access_token");
    }

    public static String getRefreshToken(String usernameOrEmail,
                                         String password) throws ExecutionException, InterruptedException {
        Response response = login(
                usernameOrEmail,
                password
        );
        response.then()
                .statusCode(200);
        return response.jsonPath()
                .getString("refresh_token");
    }

    public static String getStateToken(String usernameOrEmail,
                                       String password) throws ExecutionException, InterruptedException {
        Response response = login(
                usernameOrEmail,
                password
        );
        response.then()
                .statusCode(200);
        return response.jsonPath()
                .getString("state_token");
    }
}
