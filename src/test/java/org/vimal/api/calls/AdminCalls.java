package org.vimal.api.calls;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.Map;
import java.util.Set;

import static org.vimal.constants.Common.AUTHORIZATION;
import static org.vimal.constants.Common.BEARER;
import static org.vimal.constants.SubPaths.ADMIN;
import static org.vimal.enums.RequestMethods.POST;
import static org.vimal.utils.ApiRequestUtility.executeRequest;

public final class AdminCalls {
    private AdminCalls() {
    }

    public static Response createUsers(String accessToken, Set<UserDto> users) {
        return executeRequest(POST, ADMIN + "/create/users", Map.of(AUTHORIZATION, BEARER + accessToken), null, null, users);
    }
}
