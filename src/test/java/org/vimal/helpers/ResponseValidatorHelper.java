package org.vimal.helpers;

import io.restassured.response.Response;
import org.vimal.dtos.RoleDto;
import org.vimal.dtos.UserDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public final class ResponseValidatorHelper {
    private ResponseValidatorHelper() {
    }

    public static void validateResponseOfGetSelfDetails(Response response,
                                                        UserDto user) {
        response.then()
                .statusCode(200)
                .body("username", equalTo(user.getUsername()))
                .body("email", equalTo(user.getEmail()))
                .body("firstName", equalTo(user.getFirstName()))
                .body("middleName", equalTo(user.getMiddleName()))
                .body("lastName", equalTo(user.getLastName()))
                .body("roles", (user.getRoles() != null) ?
                        containsInAnyOrder(user.getRoles().toArray()) :
                        empty());
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
                    .body("not_allowed_to_assign_roles", not(empty()));
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

    public static void validateResponseOfUsersUpdation(Response response,
                                                       UserDto updater,
                                                       Set<UserDto> users,
                                                       Set<UserDto> updatedInputs,
                                                       int statusCode,
                                                       String pathPrefix) {
        response.then()
                .statusCode(statusCode);
        if (statusCode != 200) {
            response.then()
                    .body("cannot_update_users_having_roles_higher_or_equal_than_updater", not(empty()));
            return;
        }
        response.then()
                .body(pathPrefix + "size()", equalTo(users.size()));
        Map<String, UserDto> oldUsernameToUpdatedInputMap = new HashMap<>();
        String findPath;
        for (UserDto updatedInput : updatedInputs) {
            oldUsernameToUpdatedInputMap.put(updatedInput.getOldUsername(), updatedInput);
        }
        UserDto updatedInput;
        for (UserDto user : users) {
            updatedInput = oldUsernameToUpdatedInputMap.get(user.getUsername());
            findPath = pathPrefix + "find { it.username == '" + updatedInput.getUsername() + "' }.";
            response.then()
                    .body(findPath + "email", updatedInput.getEmail() != null ? equalTo(updatedInput.getEmail()) : equalTo(user.getEmail()))
                    .body(findPath + "firstName", updatedInput.getFirstName() != null ? equalTo(updatedInput.getFirstName()) : equalTo(user.getFirstName()))
                    .body(findPath + "middleName", updatedInput.getMiddleName() != null ? equalTo(updatedInput.getMiddleName()) : equalTo(user.getMiddleName()))
                    .body(findPath + "lastName", updatedInput.getLastName() != null ? equalTo(updatedInput.getLastName()) : equalTo(user.getLastName()))
                    .body(findPath + "updatedBy", equalTo(updater.getUsername()))
                    .body(findPath + "roles", (updatedInput.getRoles() != null) ?
                            containsInAnyOrder(updatedInput.getRoles().toArray()) :
                            (user.getRoles() != null) ?
                                    containsInAnyOrder(user.getRoles().toArray()) :
                                    empty());
        }
    }

    public static void validateResponseOfRolesCreationOrRead(Response response,
                                                             UserDto creatorOrReader,
                                                             Set<RoleDto> roles,
                                                             String pathPrefix) {
        response.then()
                .statusCode(200)
                .body(pathPrefix + "size()", equalTo(roles.size()));
        String findPath;
        for (RoleDto role : roles) {
            findPath = pathPrefix + "find { it.roleName == '" + role.getRoleName() + "' }.";
            response.then()
                    .body(findPath + "description", equalTo(role.getDescription()))
                    .body(findPath + "permissions", (role.getPermissions() != null) ?
                            containsInAnyOrder(role.getPermissions().toArray()) :
                            empty());
            if (pathPrefix.equals("created_users.")) {
                response.then()
                        .body(findPath + "createdBy", equalTo(creatorOrReader.getUsername()));
            }
        }
    }

    public static void validateResponseOfRolesUpdation(Response response,
                                                       UserDto updater,
                                                       Set<RoleDto> roles,
                                                       Set<RoleDto> updatedInputs,
                                                       String pathPrefix) {
        response.then()
                .statusCode(200)
                .body(pathPrefix + "size()", equalTo(roles.size()));
        Map<String, RoleDto> roleNameToUpdatedInputMap = new HashMap<>();
        String findPath;
        for (RoleDto updatedInput : updatedInputs) {
            roleNameToUpdatedInputMap.put(updatedInput.getRoleName(), updatedInput);
        }
        RoleDto updatedInput;
        for (RoleDto role : roles) {
            updatedInput = roleNameToUpdatedInputMap.get(role.getRoleName());
            findPath = pathPrefix + "find { it.roleName == '" + updatedInput.getRoleName() + "' }.";
            response.then()
                    .body(findPath + "description", updatedInput.getDescription() != null ? equalTo(updatedInput.getDescription()) : equalTo(role.getDescription()))
                    .body(findPath + "updatedBy", equalTo(updater.getUsername()))
                    .body(findPath + "permissions", (updatedInput.getPermissions() != null) ?
                            containsInAnyOrder(updatedInput.getPermissions().toArray()) :
                            (role.getPermissions() != null) ?
                                    containsInAnyOrder(role.getPermissions().toArray()) :
                                    empty());
        }
    }
}
