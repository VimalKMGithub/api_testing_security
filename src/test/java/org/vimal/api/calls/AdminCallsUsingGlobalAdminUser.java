package org.vimal.api.calls;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.Set;

import static org.vimal.BaseTest.*;

public final class AdminCallsUsingGlobalAdminUser {
    private AdminCallsUsingGlobalAdminUser() {
    }

    public static Response createUsers(Set<UserDto> users) {
        var response = AdminCalls.createUsers(GLOBAL_ADMIN_ACCESS_TOKEN, users);
        if (response.statusCode() == 401) {
            GLOBAL_ADMIN_ACCESS_TOKEN = AuthenticationCalls.getAccessToken(GLOBAL_ADMIN_USERNAME, GLOBAL_ADMIN_PASSWORD);
            response = AdminCalls.createUsers(GLOBAL_ADMIN_ACCESS_TOKEN, users);
        }
        return response;
    }
}
