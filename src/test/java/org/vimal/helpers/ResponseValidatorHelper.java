package org.vimal.helpers;

import io.restassured.response.Response;
import org.vimal.dtos.UserDto;

import java.util.Set;

import static org.hamcrest.Matchers.*;

public final class ResponseValidatorHelper {
    private ResponseValidatorHelper() {
    }

    public static void validateResponseOfUsersCreationOrRead(Response response,
                                                             UserDto creatorOrReader,
                                                             Set<UserDto> users,
                                                             int statusCode,
                                                             String pathPrefix) {
        response.then()
                .statusCode(statusCode);
        if (statusCode != 200) {
            response.then()
                    .body("invalid_inputs", not(empty()));
            return;
        }
        response.then()
                .body(pathPrefix + "size()", equalTo(users.size()));
        String findPath;
        for (UserDto user : users) {
            findPath = pathPrefix + "find { it.username == '" + user.getUsername() + "' }.";
            response.then()
                    .body(findPath + "email", equalTo(user.getEmail()))
                    .body(findPath + "firstName", equalTo(user.getFirstName()))
                    .body(findPath + "middleName", equalTo(user.getMiddleName()))
                    .body(findPath + "lastName", equalTo(user.getLastName()))
                    .body(findPath + "roles", (user.getRoles() != null) ?
                            containsInAnyOrder(user.getRoles().toArray()) :
                            empty());
            if (pathPrefix.equals("created_users.")) {
                response.then()
                        .body(findPath + "createdBy", equalTo(creatorOrReader.getUsername()));
            }
        }
    }
}
