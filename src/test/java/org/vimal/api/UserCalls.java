package org.vimal.api;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import static org.vimal.constants.SubPaths.USER;

public final class UserCalls {
    private UserCalls() {
    }

    public static Response register(UserDto user) {
        return ApiCalls.executeRequest(
                org.vimal.enums.RequestMethods.POST,
                USER + "/register",
                null,
                null,
                null,
                user
        );
    }
}
