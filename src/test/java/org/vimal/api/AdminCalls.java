package org.vimal.api;

import io.restassured.response.Response;
import org.vimal.dtos.RoleDto;
import org.vimal.dtos.UserDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.vimal.api.ApiCalls.executeRequest;
import static org.vimal.api.Common.waitForResponse;
import static org.vimal.constants.Common.*;
import static org.vimal.constants.SubPaths.ADMIN;
import static org.vimal.enums.RequestMethods.*;

public final class AdminCalls {
    private AdminCalls() {
    }

    public static Response createUsers(String accessToken,
                                       Set<UserDto> users,
                                       String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        ADMIN + "/create/users",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        users
                )
        );
    }

    public static Response deleteUsers(String accessToken,
                                       Set<String> usernamesOrEmails,
                                       String hard,
                                       String leniency) throws ExecutionException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        if (hard != null &&
                !hard.isBlank()) {
            params.put(HARD, hard);
        }
        if (leniency != null &&
                !leniency.isBlank()) {
            params.put(LENIENCY, leniency);
        }
        return waitForResponse(() -> executeRequest(
                        DELETE,
                        ADMIN + "/delete/users",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        params.isEmpty() ? null : params,
                        null,
                        usernamesOrEmails
                )
        );
    }

    public static Response readUsers(String accessToken,
                                     Set<String> usernamesOrEmails,
                                     String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        GET,
                        ADMIN + "/read/users",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        usernamesOrEmails
                )
        );
    }

    public static Response updateUsers(String accessToken,
                                       Set<UserDto> users,
                                       String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        PUT,
                        ADMIN + "/update/users",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        users
                )
        );
    }

    public static Response createRoles(String accessToken,
                                       Set<RoleDto> roles,
                                       String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        POST,
                        ADMIN + "/create/roles",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        roles
                )
        );
    }

    public static Response deleteRoles(String accessToken,
                                       Set<String> roleNames,
                                       String force,
                                       String leniency) throws ExecutionException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        if (force != null &&
                !force.isBlank()) {
            params.put(FORCE, force);
        }
        if (leniency != null &&
                !leniency.isBlank()) {
            params.put(LENIENCY, leniency);
        }
        return waitForResponse(() -> executeRequest(
                        DELETE,
                        ADMIN + "/delete/roles",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        params.isEmpty() ? null : params,
                        null,
                        roleNames
                )
        );
    }

    public static Response readRoles(String accessToken,
                                     Set<String> roleNames,
                                     String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        GET,
                        ADMIN + "/read/roles",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        roleNames
                )
        );
    }

    public static Response updateRoles(String accessToken,
                                       Set<RoleDto> roles,
                                       String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        PUT,
                        ADMIN + "/update/roles",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        roles
                )
        );
    }

    public static Response readPermissions(String accessToken,
                                           Set<String> permissionNames,
                                           String leniency) throws ExecutionException, InterruptedException {
        return waitForResponse(() -> executeRequest(
                        GET,
                        ADMIN + "/read/permissions",
                        Map.of(AUTHORIZATION, BEARER + accessToken),
                        (leniency == null ||
                                leniency.isBlank()) ? null : Map.of(LENIENCY, leniency),
                        null,
                        permissionNames
                )
        );
    }
}
