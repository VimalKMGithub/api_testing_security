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
import static org.vimal.enums.RequestMethods.GET;
import static org.vimal.enums.RequestMethods.POST;

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

    public static Response forgotPasswordMethodSelection(String usernameOrEmail,
                                                         String method) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/forgot/password/methodSelection",
                        null,
                        Map.of(
                                "usernameOrEmail", usernameOrEmail,
                                "method", method
                        )
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

    public static Response changePasswordMethodSelection(String accessToken,
                                                         String method) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        USER + "/change/password/methodSelection",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        Map.of("method", method)
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
}
