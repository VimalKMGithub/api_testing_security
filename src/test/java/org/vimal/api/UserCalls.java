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
}
