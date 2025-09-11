package org.vimal.api;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.vimal.api.ApiCalls.executeRequest;
import static org.vimal.api.Common.waitForResponse;
import static org.vimal.constants.Common.AUTHORIZATION;
import static org.vimal.constants.Common.BEARER;
import static org.vimal.constants.SubPaths.USER;
import static org.vimal.enums.RequestMethods.*;

public final class UserCalls {
    private UserCalls() {
    }

    public static Response register(UserDto user) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/register",
                        null,
                        null,
                        null,
                        user
                )
        );
    }

    public static Response getSelfDetails(String accessToken) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        GET,
                        USER + "/getSelfDetails",
                        Map.of(AUTHORIZATION, BEARER + accessToken)
                )
        );
    }

    public static Response forgotPassword(String usernameOrEmail) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/forgot/password",
                        null,
                        Map.of("usernameOrEmail", usernameOrEmail)
                )
        );
    }

    public static Response resetPassword(Map<String, String> body) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/reset/password",
                        null,
                        null,
                        null,
                        body
                )
        );
    }

    public static Response changePassword(String accessToken,
                                          Map<String, String> body) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/change/password",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        null,
                        null,
                        body
                )
        );
    }

    public static Response verifyChangePassword(String accessToken,
                                                Map<String, String> body) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/verify/change/password",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        null,
                        null,
                        body
                )
        );
    }

    public static Response deleteAccount(String accessToken,
                                         String password) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        DELETE,
                        USER + "/delete/account",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        Map.of("password", password)
                )
        );
    }

    public static Response verifyDeleteAccount(String accessToken,
                                               String otpTotp,
                                               String method) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        DELETE,
                        USER + "/verify/delete/account",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        Map.of(
                                "otpTotp", otpTotp,
                                "method", method
                        )
                )
        );
    }

    public static Response updateDetails(String accessToken,
                                         Map<String, String> body) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        PUT,
                        USER + "/update/details",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        null,
                        null,
                        body
                )
        );
    }
}
