package org.vimal.api;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.Set;

import static org.vimal.BaseTest.*;
import static org.vimal.api.AuthenticationCalls.getAccessToken;

public final class AdminCallsUsingGlobalAdminUser {
    private AdminCallsUsingGlobalAdminUser() {
    }

    public static Response createUsers(Set<UserDto> users, String leniency) {
        Response response = AdminCalls.createUsers(GLOBAL_ADMIN_ACCESS_TOKEN, users, leniency);
        if (response.statusCode() == 401) {
            GLOBAL_ADMIN_ACCESS_TOKEN = getAccessToken(GLOBAL_ADMIN_USERNAME, GLOBAL_ADMIN_PASSWORD);
            response = AdminCalls.createUsers(GLOBAL_ADMIN_ACCESS_TOKEN, users, leniency);
        }
        return response;
    }

    public static Response deleteUsers(Set<String> usernamesOrEmails, String hard, String leniency) {
        Response response = AdminCalls.deleteUsers(GLOBAL_ADMIN_ACCESS_TOKEN, usernamesOrEmails, hard, leniency);
        if (response.statusCode() == 401) {
            GLOBAL_ADMIN_ACCESS_TOKEN = getAccessToken(GLOBAL_ADMIN_USERNAME, GLOBAL_ADMIN_PASSWORD);
            response = AdminCalls.deleteUsers(GLOBAL_ADMIN_ACCESS_TOKEN, usernamesOrEmails, hard, leniency);
        }
        return response;
    }
}
