package org.vimal.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AdminCalls.createUsers;
import static org.vimal.api.AdminCalls.deleteUsers;
import static org.vimal.api.AuthenticationCalls.getAccessToken;
import static org.vimal.constants.Common.ENABLE;
import static org.vimal.constants.Common.MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME;
import static org.vimal.enums.Roles.*;
import static org.vimal.helpers.DtosHelper.createRandomUserDto;
import static org.vimal.helpers.InvalidInputsHelper.*;
import static org.vimal.helpers.ResponseValidatorHelper.validateResponseOfUsersCreation;
import static org.vimal.utils.DateTimeUtility.getCurrentFormattedLocalTimeStamp;
import static org.vimal.utils.RandomStringUtility.generateRandomStringAlphaNumeric;

public class AdminServiceTests extends BaseTest {
    private static final Set<String> USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS = Set.of(
            ROLE_MANAGE_ROLES.name(),
            ROLE_MANAGE_PERMISSIONS.name()
    );
    private static final Set<String> ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS = buildRoleSetForAdminCanCreateUpdateDeleteUsers();
    private static final Set<String> ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS = buildRoleSetForSuperAdminCanCreateUpdateDeleteUsers();
    private static final Set<String> ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS = Set.of(ROLE_SUPER_ADMIN.name());
    private static final Set<String> ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS = buildRoleSetForAdminCannotCreateUpdateDeleteUsers();
    private static final Set<String> USERS_WITH_THESE_ROLES_CAN_READ_USERS = Set.of(
            ROLE_MANAGE_USERS.name(),
            ROLE_ADMIN.name(),
            ROLE_SUPER_ADMIN.name()
    );

    private static Set<String> buildRoleSetForAdminCanCreateUpdateDeleteUsers() {
        Set<String> roles = new HashSet<>(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS);
        roles.add(ROLE_MANAGE_USERS.name());
        return Collections.unmodifiableSet(roles);
    }

    private static Set<String> buildRoleSetForSuperAdminCanCreateUpdateDeleteUsers() {
        Set<String> roles = new HashSet<>(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS);
        roles.add(ROLE_ADMIN.name());
        return Collections.unmodifiableSet(roles);
    }

    private static Set<String> buildRoleSetForAdminCannotCreateUpdateDeleteUsers() {
        Set<String> roles = new HashSet<>(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS);
        roles.add(ROLE_ADMIN.name());
        return Collections.unmodifiableSet(roles);
    }

    private void createUsersAndVerifyResponse(UserDto creator,
                                              Set<UserDto> users,
                                              int statusCode) throws ExecutionException, InterruptedException {
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        Iterator<UserDto> iterator = users.iterator();
        Set<UserDto> batch = new HashSet<>();
        Response response;
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME) {
                batch.add(iterator.next());
            }
            TEST_USERS.addAll(batch);
            response = createUsers(
                    accessToken,
                    batch,
                    null
            );
            validateResponseOfUsersCreation(
                    response,
                    creator,
                    batch,
                    statusCode
            );
        }
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Super_Admin() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCanBeCreatedBySuperAdmin = new HashSet<>();
        usersThatCanBeCreatedBySuperAdmin.add(createRandomUserDto());
        usersThatCanBeCreatedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeCreatedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        createUsersAndVerifyResponse(
                creator,
                usersThatCanBeCreatedBySuperAdmin,
                200
        );
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Admin() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_ADMIN.name()));
        Set<UserDto> usersThatCanBeCreatedByAdmin = new HashSet<>();
        usersThatCanBeCreatedByAdmin.add(createRandomUserDto());
        usersThatCanBeCreatedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeCreatedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        createUsersAndVerifyResponse(
                creator,
                usersThatCanBeCreatedByAdmin,
                200
        );
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Mange_Users() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_MANAGE_USERS.name()));
        Set<UserDto> usersThatCanBeCreatedByManageUsers = new HashSet<>();
        usersThatCanBeCreatedByManageUsers.add(createRandomUserDto());
        usersThatCanBeCreatedByManageUsers.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeCreatedByManageUsers.add(createRandomUserDto(Set.of(role)));
        }
        createUsersAndVerifyResponse(
                creator,
                usersThatCanBeCreatedByManageUsers,
                200
        );
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Cannot_Create_Users() throws ExecutionException, InterruptedException {
        Set<UserDto> creators = new HashSet<>();
        creators.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS) {
            creators.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(creators);
        Set<UserDto> testSet = Set.of(createRandomUserDto());
        for (UserDto creator : creators) {
            createUsers(
                    getAccessToken(
                            creator.getUsername(),
                            creator.getPassword()
                    ),
                    testSet,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Super_Admin_Not_Allowed_To_Create_Users() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCannotBeCreatedBySuperAdmin = new HashSet<>();
        usersThatCannotBeCreatedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeCreatedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        createUsersAndVerifyResponse(
                creator,
                usersThatCannotBeCreatedBySuperAdmin,
                400
        );
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Admin_Not_Allowed_To_Create_Users() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_ADMIN.name()));
        Set<UserDto> usersThatCannotBeCreatedByAdmin = new HashSet<>();
        usersThatCannotBeCreatedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeCreatedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        createUsersAndVerifyResponse(
                creator,
                usersThatCannotBeCreatedByAdmin,
                400
        );
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Mange_Users_Not_Allowed_To_Create_Users() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_MANAGE_USERS.name()));
        Set<UserDto> usersThatCannotBeCreatedByManageUsers = new HashSet<>();
        usersThatCannotBeCreatedByManageUsers.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeCreatedByManageUsers.add(createRandomUserDto(Set.of(role)));
        }
        createUsersAndVerifyResponse(
                creator,
                usersThatCannotBeCreatedByManageUsers,
                400
        );
    }

    @Test
    public void test_Create_Users_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        UserDto user = createRandomUserDto();
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        for (String invalidUsername : INVALID_USERNAMES) {
            user.setUsername(invalidUsername);
            createUsers(
                    accessToken,
                    Set.of(user),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        String randomString = getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric();
        user.setUsername("AutoTestUser_" + randomString);
        for (String invalidEmail : INVALID_EMAILS) {
            user.setEmail(invalidEmail);
            createUsers(
                    accessToken,
                    Set.of(user),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setEmail("user_" + randomString + "@example.com");
        for (String invalidPassword : INVALID_PASSWORDS) {
            user.setPassword(invalidPassword);
            createUsers(
                    accessToken,
                    Set.of(user),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setPassword("Password@1_" + randomString);
        for (String invalidFirstName : INVALID_NAMES) {
            user.setFirstName(invalidFirstName);
            createUsers(
                    accessToken,
                    Set.of(user),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setFirstName("AutoTestUser");
        for (String invalidMiddleName : INVALID_NAMES) {
            user.setMiddleName(invalidMiddleName);
            createUsers(
                    accessToken,
                    Set.of(user),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setMiddleName(null);
        for (String invalidLastName : INVALID_NAMES) {
            user.setLastName(invalidLastName);
            createUsers(
                    accessToken,
                    Set.of(user),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setLastName(null);
        user.setRoles(Set.of("InvalidRoleName" + randomString));
        createUsers(
                accessToken,
                Set.of(user),
                null
        ).then()
                .statusCode(400)
                .body("invalid_inputs", not(empty()));
    }

    private void deleteUsersAndVerifyResponse(UserDto creator,
                                              Set<UserDto> users,
                                              int statusCode) throws ExecutionException, InterruptedException {
        Set<String> identifiers = new HashSet<>();
        int i = 0;
        for (UserDto user : users) {
            if (i % 2 == 0) {
                identifiers.add(user.getEmail());
            } else {
                identifiers.add(user.getUsername());
            }
            i++;
        }
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        Iterator<String> iterator = identifiers.iterator();
        Set<String> batch = new HashSet<>();
        Response response;
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME) {
                batch.add(iterator.next());
            }
            response = deleteUsers(
                    accessToken,
                    batch,
                    ENABLE,
                    null
            );
            response.then()
                    .statusCode(statusCode);
            if (statusCode != 200) {
                response.then()
                        .body("invalid_inputs", not(empty()));
            } else {
                response.then()
                        .body("message", containsStringIgnoringCase("Users deleted successfully"));
            }
        }
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Super_Admin() throws ExecutionException, InterruptedException {
        UserDto deleter = createRandomUserDto(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCanBeDeletedBySuperAdmin = new HashSet<>();
        usersThatCanBeDeletedBySuperAdmin.add(createRandomUserDto());
        usersThatCanBeDeletedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeDeletedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCanBeDeletedBySuperAdmin.add(deleter);
        createTestUsers(usersThatCanBeDeletedBySuperAdmin);
        usersThatCanBeDeletedBySuperAdmin.remove(deleter);
        deleteUsersAndVerifyResponse(
                deleter,
                usersThatCanBeDeletedBySuperAdmin,
                200
        );
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Admin() throws ExecutionException, InterruptedException {
        UserDto deleter = createRandomUserDto(Set.of(ROLE_ADMIN.name()));
        Set<UserDto> usersThatCanBeDeletedByAdmin = new HashSet<>();
        usersThatCanBeDeletedByAdmin.add(createRandomUserDto());
        usersThatCanBeDeletedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeDeletedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCanBeDeletedByAdmin.add(deleter);
        createTestUsers(usersThatCanBeDeletedByAdmin);
        usersThatCanBeDeletedByAdmin.remove(deleter);
        deleteUsersAndVerifyResponse(
                deleter,
                usersThatCanBeDeletedByAdmin,
                200
        );
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Mange_Users() throws ExecutionException, InterruptedException {
        UserDto deleter = createRandomUserDto(Set.of(ROLE_MANAGE_USERS.name()));
        Set<UserDto> usersThatCanBeDeletedByManageUsers = new HashSet<>();
        usersThatCanBeDeletedByManageUsers.add(createRandomUserDto());
        usersThatCanBeDeletedByManageUsers.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeDeletedByManageUsers.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCanBeDeletedByManageUsers.add(deleter);
        createTestUsers(usersThatCanBeDeletedByManageUsers);
        usersThatCanBeDeletedByManageUsers.remove(deleter);
        deleteUsersAndVerifyResponse(
                deleter,
                usersThatCanBeDeletedByManageUsers,
                200
        );
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Cannot_Delete_Users() throws ExecutionException, InterruptedException {
        Set<UserDto> deleters = new HashSet<>();
        deleters.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS) {
            deleters.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(deleters);
        for (UserDto deleter : deleters) {
            deleteUsers(
                    getAccessToken(
                            deleter.getUsername(),
                            deleter.getPassword()
                    ),
                    Set.of("someUsername"),
                    ENABLE,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Super_Admin_Not_Allowed_To_Delete_Users() throws ExecutionException, InterruptedException {
        UserDto deleter = createRandomUserDto(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCannotBeDeletedBySuperAdmin = new HashSet<>();
        usersThatCannotBeDeletedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeDeletedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCannotBeDeletedBySuperAdmin.add(deleter);
        createTestUsers(usersThatCannotBeDeletedBySuperAdmin);
        usersThatCannotBeDeletedBySuperAdmin.remove(deleter);
        deleteUsersAndVerifyResponse(
                deleter,
                usersThatCannotBeDeletedBySuperAdmin,
                400
        );
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Admin_Not_Allowed_To_Delete_Users() throws ExecutionException, InterruptedException {
        UserDto deleter = createRandomUserDto(Set.of(ROLE_ADMIN.name()));
        Set<UserDto> usersThatCannotBeDeletedByAdmin = new HashSet<>();
        usersThatCannotBeDeletedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeDeletedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCannotBeDeletedByAdmin.add(deleter);
        createTestUsers(usersThatCannotBeDeletedByAdmin);
        usersThatCannotBeDeletedByAdmin.remove(deleter);
        deleteUsersAndVerifyResponse(
                deleter,
                usersThatCannotBeDeletedByAdmin,
                400
        );
    }

    @Test
    public void test_Delete_Users_Using_User_With_Role_Mange_Users_Not_Allowed_To_Delete_Users() throws ExecutionException, InterruptedException {
        UserDto deleter = createRandomUserDto(Set.of(ROLE_MANAGE_USERS.name()));
        Set<UserDto> usersThatCannotBeDeletedByManageUsers = new HashSet<>();
        usersThatCannotBeDeletedByManageUsers.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeDeletedByManageUsers.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCannotBeDeletedByManageUsers.add(deleter);
        createTestUsers(usersThatCannotBeDeletedByManageUsers);
        usersThatCannotBeDeletedByManageUsers.remove(deleter);
        deleteUsersAndVerifyResponse(
                deleter,
                usersThatCannotBeDeletedByManageUsers,
                400
        );
    }

    @Test
    public void test_Delete_Users_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto deleter = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                deleter.getUsername(),
                deleter.getPassword()
        );
        for (String invalidIdentifier : INVALID_USERNAMES) {
            deleteUsers(
                    accessToken,
                    Set.of(invalidIdentifier),
                    ENABLE,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        for (String invalidIdentifier : INVALID_EMAILS) {
            deleteUsers(
                    accessToken,
                    Set.of(invalidIdentifier),
                    ENABLE,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
    }
}
