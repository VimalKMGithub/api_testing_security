package org.vimal.helpers;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.Set;

import static org.hamcrest.Matchers.*;

public final class ResponseValidatorHelper {
    private ResponseValidatorHelper() {
    }

    private static final String PATH_PREFIX_FOR_CREATED_USERS = "created_users.";

    public static void validateResponseOfUsersCreation(Response response,
                                                       UserDto creator,
                                                       Set<UserDto> users,
                                                       int statusCode) {
        response.then()
                .statusCode(statusCode);
        if (statusCode != 200) {
            response.then()
                    .body("invalid_inputs", not(empty()));
            return;
        }
        response.then()
                .body(PATH_PREFIX_FOR_CREATED_USERS + "size()", equalTo(users.size()));
        String findPath;
        for (UserDto user : users) {
            findPath = PATH_PREFIX_FOR_CREATED_USERS + "find { it.username == '" + user.getUsername() + "' }.";
            response.then()
                    .body(findPath + "email", equalTo(user.getEmail()))
                    .body(findPath + "firstName", equalTo(user.getFirstName()))
                    .body(findPath + "middleName", equalTo(user.getMiddleName()))
                    .body(findPath + "lastName", equalTo(user.getLastName()))
                    .body(findPath + "createdBy", equalTo(creator.getUsername()))
                    .body(findPath + "roles", (user.getRoles() != null) ?
                            containsInAnyOrder(user.getRoles().toArray()) :
                            empty());
        }
    }
}
