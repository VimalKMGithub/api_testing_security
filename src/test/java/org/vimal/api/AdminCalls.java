package org.vimal.api;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.vimal.api.ApiCalls.executeRequest;
import static org.vimal.constants.Common.*;
import static org.vimal.constants.SubPaths.ADMIN;
import static org.vimal.enums.RequestMethods.DELETE;
import static org.vimal.enums.RequestMethods.POST;

public final class AdminCalls {
    private AdminCalls() {
    }

    public static Response createUsers(String accessToken,
                                       Set<UserDto> users,
                                       String leniency) {
        return executeRequest(
                POST,
                ADMIN + "/create/users",
                Map.of(AUTHORIZATION, BEARER + accessToken),
                (leniency == null ||
                        leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                null,
                users
        );
    }

    public static Response deleteUsers(String accessToken,
                                       Set<String> usernamesOrEmails,
                                       String hard,
                                       String leniency) {
        Map<String, String> params = new HashMap<>();
        if (hard != null &&
                !hard.isBlank()) {
            params.put(HARD, hard);
        }
        if (leniency != null &&
                !leniency.isBlank()) {
            params.put(LENIENCY, leniency);
        }
        return executeRequest(
                DELETE,
                ADMIN + "/delete/users",
                Map.of(AUTHORIZATION, BEARER + accessToken),
                params.isEmpty() ? null : params,
                null,
                usernamesOrEmails
        );
    }
}
