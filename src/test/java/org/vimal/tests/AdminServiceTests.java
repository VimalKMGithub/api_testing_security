package org.vimal.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.RoleDto;
import org.vimal.dtos.UserDto;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AdminCalls.*;
import static org.vimal.api.AuthenticationCalls.getAccessToken;
import static org.vimal.constants.Common.*;
import static org.vimal.enums.Permissions.CAN_CREATE_USER;
import static org.vimal.enums.Roles.*;
import static org.vimal.helpers.DtosHelper.*;
import static org.vimal.helpers.InvalidInputsHelper.*;
import static org.vimal.helpers.ResponseValidatorHelper.*;
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
    private static final Set<String> USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES = Set.of(
            ROLE_MANAGE_USERS.name(),
            ROLE_MANAGE_PERMISSIONS.name()
    );
    private static final Set<String> USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES = Set.of(
            ROLE_MANAGE_ROLES.name(),
            ROLE_ADMIN.name(),
            ROLE_SUPER_ADMIN.name()
    );
    private static final Set<String> USERS_WITH_THESE_ROLES_CANNOT_READ_PERMISSIONS = Set.of(
            ROLE_MANAGE_USERS.name(),
            ROLE_MANAGE_ROLES.name()
    );
    private static final Set<String> USERS_WITH_THESE_ROLES_CAN_READ_PERMISSIONS = Set.of(
            ROLE_MANAGE_PERMISSIONS.name(),
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
        UserDto user;
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME) {
                user = iterator.next();
                TEST_USERS.add(user);
                batch.add(user);
            }
            response = createUsers(
                    accessToken,
                    batch,
                    null
            );
            validateResponseOfUsersCreationOrRead(
                    response,
                    creator,
                    batch,
                    statusCode,
                    "created_users."
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
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        UserDto user = createRandomUserDto();
        Set<UserDto> testSet = Set.of(user);
        for (String invalidUsername : INVALID_USERNAMES) {
            user.setUsername(invalidUsername);
            createUsers(
                    accessToken,
                    testSet,
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
                    testSet,
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
                    testSet,
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
                    testSet,
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
                    testSet,
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
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setLastName(null);
        user.setRoles(Set.of("InvalidRoleName_" + randomString));
        createUsers(
                accessToken,
                testSet,
                null
        ).then()
                .statusCode(400)
                .body("missing_roles", containsInAnyOrder("InvalidRoleName_" + randomString));
    }

    private void deleteUsersAndVerifyResponse(UserDto deleter,
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
                deleter.getUsername(),
                deleter.getPassword()
        );
        Iterator<String> iterator = identifiers.iterator();
        Set<String> batch = new HashSet<>();
        Response response;
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_DELETION_AT_A_TIME) {
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
                        .body("not_allowed_to_delete_users_having_roles", not(empty()));
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

    private void readUsersAndVerifyResponse(UserDto reader,
                                            Set<UserDto> users) throws ExecutionException, InterruptedException {
        String accessToken = getAccessToken(
                reader.getUsername(),
                reader.getPassword()
        );
        Iterator<UserDto> iterator = users.iterator();
        Set<String> batch = new HashSet<>();
        Set<UserDto> batchUsers = new HashSet<>();
        Response response;
        UserDto user;
        while (iterator.hasNext()) {
            batch.clear();
            batchUsers.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_READ_AT_A_TIME) {
                user = iterator.next();
                batchUsers.add(user);
                batch.add(user.getUsername());
            }
            response = readUsers(
                    accessToken,
                    batch,
                    null
            );
            validateResponseOfUsersCreationOrRead(
                    response,
                    reader,
                    batchUsers,
                    200,
                    "found_users."
            );
        }
    }

    @Test
    public void test_Read_Users_Using_User_With_Role_Can_Read_Users() throws ExecutionException, InterruptedException {
        Set<UserDto> readers = new HashSet<>();
        readers.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CAN_READ_USERS));
        for (String role : USERS_WITH_THESE_ROLES_CAN_READ_USERS) {
            readers.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(readers);
        for (UserDto reader : readers) {
            readUsersAndVerifyResponse(
                    reader,
                    readers
            );
        }
    }

    @Test
    public void test_Read_Users_Using_User_With_Role_Cannot_Read_Users() throws ExecutionException, InterruptedException {
        Set<UserDto> readers = new HashSet<>();
        readers.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS) {
            readers.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(readers);
        for (UserDto reader : readers) {
            readUsers(
                    getAccessToken(
                            reader.getUsername(),
                            reader.getPassword()
                    ),
                    Set.of("someUsername"),
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Read_Users_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto reader = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                reader.getUsername(),
                reader.getPassword()
        );
        for (String invalidIdentifier : INVALID_USERNAMES) {
            readUsers(
                    accessToken,
                    Set.of(invalidIdentifier),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        for (String invalidIdentifier : INVALID_EMAILS) {
            readUsers(
                    accessToken,
                    Set.of(invalidIdentifier),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
    }

    private void updateUsersAndVerifyResponse(UserDto updater,
                                              Set<UserDto> users,
                                              Set<UserDto> updatedInputs,
                                              int statusCode) throws ExecutionException, InterruptedException {
        String accessToken = getAccessToken(
                updater.getUsername(),
                updater.getPassword()
        );
        Map<String, UserDto> usernameToUserMap = new HashMap<>();
        for (UserDto user : users) {
            usernameToUserMap.put(user.getUsername(), user);
        }
        Iterator<UserDto> iterator = updatedInputs.iterator();
        Set<UserDto> batch = new HashSet<>();
        Set<UserDto> batchUsers = new HashSet<>();
        Response response;
        UserDto user;
        while (iterator.hasNext()) {
            batch.clear();
            batchUsers.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_UPDATE_AT_A_TIME) {
                user = iterator.next();
                TEST_USERS.add(user);
                batch.add(user);
                batchUsers.add(usernameToUserMap.get(user.getOldUsername()));
            }
            response = updateUsers(
                    accessToken,
                    batch,
                    null
            );
            validateResponseOfUsersUpdation(
                    response,
                    updater,
                    batchUsers,
                    batch,
                    statusCode,
                    "updated_users."
            );
        }
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Super_Admin() throws ExecutionException, InterruptedException {
        UserDto updater = createRandomUserDto(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCanBeUpdatedBySuperAdmin = new HashSet<>();
        usersThatCanBeUpdatedBySuperAdmin.add(createRandomUserDto());
        usersThatCanBeUpdatedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeUpdatedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCanBeUpdatedBySuperAdmin.add(updater);
        createTestUsers(usersThatCanBeUpdatedBySuperAdmin);
        usersThatCanBeUpdatedBySuperAdmin.remove(updater);
        Set<UserDto> updatedInputs = new HashSet<>();
        UserDto userTemp;
        for (UserDto user : usersThatCanBeUpdatedBySuperAdmin) {
            userTemp = createRandomUserDto();
            userTemp.setOldUsername(user.getUsername());
            updatedInputs.add(userTemp);
        }
        updateUsersAndVerifyResponse(
                updater,
                usersThatCanBeUpdatedBySuperAdmin,
                updatedInputs,
                200
        );
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Admin() throws ExecutionException, InterruptedException {
        UserDto updater = createRandomUserDto(Set.of(ROLE_ADMIN.name()));
        Set<UserDto> usersThatCanBeUpdatedByAdmin = new HashSet<>();
        usersThatCanBeUpdatedByAdmin.add(createRandomUserDto());
        usersThatCanBeUpdatedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeUpdatedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCanBeUpdatedByAdmin.add(updater);
        createTestUsers(usersThatCanBeUpdatedByAdmin);
        usersThatCanBeUpdatedByAdmin.remove(updater);
        Set<UserDto> updatedInputs = new HashSet<>();
        UserDto userTemp;
        for (UserDto user : usersThatCanBeUpdatedByAdmin) {
            userTemp = createRandomUserDto();
            userTemp.setOldUsername(user.getUsername());
            updatedInputs.add(userTemp);
        }
        updateUsersAndVerifyResponse(
                updater,
                usersThatCanBeUpdatedByAdmin,
                updatedInputs,
                200
        );
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Mange_Users() throws ExecutionException, InterruptedException {
        UserDto updater = createRandomUserDto(Set.of(ROLE_MANAGE_USERS.name()));
        Set<UserDto> usersThatCanBeUpdatedByManageUsers = new HashSet<>();
        usersThatCanBeUpdatedByManageUsers.add(createRandomUserDto());
        usersThatCanBeUpdatedByManageUsers.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeUpdatedByManageUsers.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCanBeUpdatedByManageUsers.add(updater);
        createTestUsers(usersThatCanBeUpdatedByManageUsers);
        usersThatCanBeUpdatedByManageUsers.remove(updater);
        Set<UserDto> updatedInputs = new HashSet<>();
        UserDto userTemp;
        for (UserDto user : usersThatCanBeUpdatedByManageUsers) {
            userTemp = createRandomUserDto();
            userTemp.setOldUsername(user.getUsername());
            updatedInputs.add(userTemp);
        }
        updateUsersAndVerifyResponse(
                updater,
                usersThatCanBeUpdatedByManageUsers,
                updatedInputs,
                200
        );
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Cannot_Update_Users() throws ExecutionException, InterruptedException {
        Set<UserDto> updaters = new HashSet<>();
        updaters.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_USERS) {
            updaters.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(updaters);
        Set<UserDto> testSet = Set.of(createRandomUserDto());
        for (UserDto updater : updaters) {
            updateUsers(
                    getAccessToken(
                            updater.getUsername(),
                            updater.getPassword()
                    ),
                    testSet,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Super_Admin_Not_Allowed_To_Update_Users() throws ExecutionException, InterruptedException {
        UserDto updater = createRandomUserDto(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCannotBeUpdatedBySuperAdmin = new HashSet<>();
        usersThatCannotBeUpdatedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeUpdatedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCannotBeUpdatedBySuperAdmin.add(updater);
        createTestUsers(usersThatCannotBeUpdatedBySuperAdmin);
        usersThatCannotBeUpdatedBySuperAdmin.remove(updater);
        Set<UserDto> updatedInputs = new HashSet<>();
        UserDto userTemp;
        for (UserDto user : usersThatCannotBeUpdatedBySuperAdmin) {
            userTemp = createRandomUserDto();
            userTemp.setOldUsername(user.getUsername());
            updatedInputs.add(userTemp);
        }
        updateUsersAndVerifyResponse(
                updater,
                usersThatCannotBeUpdatedBySuperAdmin,
                updatedInputs,
                400
        );
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Admin_Not_Allowed_To_Update_Users() throws ExecutionException, InterruptedException {
        UserDto updater = createRandomUserDto(Set.of(ROLE_ADMIN.name()));
        Set<UserDto> usersThatCannotBeUpdatedByAdmin = new HashSet<>();
        usersThatCannotBeUpdatedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeUpdatedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCannotBeUpdatedByAdmin.add(updater);
        createTestUsers(usersThatCannotBeUpdatedByAdmin);
        usersThatCannotBeUpdatedByAdmin.remove(updater);
        Set<UserDto> updatedInputs = new HashSet<>();
        UserDto userTemp;
        for (UserDto user : usersThatCannotBeUpdatedByAdmin) {
            userTemp = createRandomUserDto();
            userTemp.setOldUsername(user.getUsername());
            updatedInputs.add(userTemp);
        }
        updateUsersAndVerifyResponse(
                updater,
                usersThatCannotBeUpdatedByAdmin,
                updatedInputs,
                400
        );
    }

    @Test
    public void test_Update_Users_Using_User_With_Role_Mange_Users_Not_Allowed_To_Update_Users() throws ExecutionException, InterruptedException {
        UserDto updater = createRandomUserDto(Set.of(ROLE_MANAGE_USERS.name()));
        Set<UserDto> usersThatCannotBeUpdatedByManageUsers = new HashSet<>();
        usersThatCannotBeUpdatedByManageUsers.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CANNOT_CREATE_UPDATE_DELETE_USERS) {
            usersThatCannotBeUpdatedByManageUsers.add(createRandomUserDto(Set.of(role)));
        }
        usersThatCannotBeUpdatedByManageUsers.add(updater);
        createTestUsers(usersThatCannotBeUpdatedByManageUsers);
        usersThatCannotBeUpdatedByManageUsers.remove(updater);
        Set<UserDto> updatedInputs = new HashSet<>();
        UserDto userTemp;
        for (UserDto user : usersThatCannotBeUpdatedByManageUsers) {
            userTemp = createRandomUserDto();
            userTemp.setOldUsername(user.getUsername());
            updatedInputs.add(userTemp);
        }
        updateUsersAndVerifyResponse(
                updater,
                usersThatCannotBeUpdatedByManageUsers,
                updatedInputs,
                400
        );
    }

    @Test
    public void test_Update_Users_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto updater = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                updater.getUsername(),
                updater.getPassword()
        );
        UserDto user = createRandomUserDto();
        Set<UserDto> testSet = Set.of(user);
        for (String invalidUsername : INVALID_USERNAMES) {
            user.setOldUsername(invalidUsername);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        String randomString = getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric();
        user.setOldUsername("AutoTestUser_" + randomString);
        for (String invalidUsername : INVALID_USERNAMES) {
            user.setUsername(invalidUsername);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setUsername("AutoTestUser_" + randomString);
        for (String invalidEmail : INVALID_EMAILS) {
            user.setEmail(invalidEmail);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setEmail("user_" + randomString + "@example.com");
        for (String invalidPassword : INVALID_PASSWORDS) {
            user.setPassword(invalidPassword);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setPassword("Password@1_" + randomString);
        for (String invalidFirstName : INVALID_NAMES) {
            user.setFirstName(invalidFirstName);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setFirstName("AutoTestUser");
        for (String invalidMiddleName : INVALID_NAMES) {
            user.setMiddleName(invalidMiddleName);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setMiddleName(null);
        for (String invalidLastName : INVALID_NAMES) {
            user.setLastName(invalidLastName);
            updateUsers(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        user.setLastName(null);
        user.setRoles(Set.of("InvalidRoleName" + randomString));
        updateUsers(
                accessToken,
                testSet,
                null
        ).then()
                .statusCode(400)
                .body("missing_roles", not(empty()));
    }

    @Test
    public void test_Create_Roles_Using_User_With_Role_Who_Can_Create_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> creators = new HashSet<>();
        creators.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES) {
            creators.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(creators);
        Set<RoleDto> tempSet;
        RoleDto role;
        Response response;
        for (UserDto creator : creators) {
            role = createRandomRoleDto();
            tempSet = Set.of(role);
            TEST_ROLES.add(role);
            response = createRoles(
                    getAccessToken(
                            creator.getUsername(),
                            creator.getPassword()
                    ),
                    tempSet,
                    null
            );
            validateResponseOfRolesCreationOrRead(
                    response,
                    creator,
                    tempSet,
                    "created_roles."
            );
        }
    }

    @Test
    public void test_Create_Roles_Using_User_With_Role_Who_Cannot_Create_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> creators = new HashSet<>();
        creators.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES) {
            creators.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(creators);
        Set<RoleDto> testSet = Set.of(createRandomRoleDto());
        for (UserDto creator : creators) {
            createRoles(
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
    public void test_Create_Roles_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        RoleDto role = createRandomRoleDto();
        Set<RoleDto> testSet = Set.of(role);
        for (String invalidRoleName : INVALID_ROLE_OR_PERMISSION_NAMES) {
            role.setRoleName(invalidRoleName);
            createRoles(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        String randomString = getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric();
        role.setRoleName("AutoTestRole_" + randomString);
        role.setDescription("d".repeat(256));
        createRoles(
                accessToken,
                testSet,
                null
        ).then()
                .statusCode(400)
                .body("invalid_inputs", not(empty()));
        role.setDescription("AutoTestRole created by AdminServiceTests");
        role.setPermissions(Set.of("InvalidPermissionName_" + randomString));
        createRoles(
                accessToken,
                testSet,
                null
        ).then()
                .statusCode(400)
                .body("missing_permissions", containsInAnyOrder("InvalidPermissionName_" + randomString));
    }

    @Test
    public void test_Delete_Roles_Using_User_With_Role_Who_Can_Delete_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> deleters = new HashSet<>();
        deleters.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES) {
            deleters.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(deleters);
        Set<RoleDto> rolesToBeDeleted = createRandomRoleDtos(deleters.size());
        createTestRoles(rolesToBeDeleted);
        Iterator<RoleDto> iterator = rolesToBeDeleted.iterator();
        for (UserDto deleter : deleters) {
            deleteRoles(
                    getAccessToken(
                            deleter.getUsername(),
                            deleter.getPassword()
                    ),
                    Set.of(iterator.next().getRoleName()),
                    ENABLE,
                    null
            ).then()
                    .statusCode(200)
                    .body("message", containsStringIgnoringCase("Roles deleted successfully"));
        }
    }

    @Test
    public void test_Delete_Roles_Using_User_With_Role_Who_Cannot_Delete_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> deleters = new HashSet<>();
        deleters.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES) {
            deleters.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(deleters);
        Set<String> testSet = Set.of("someRoleName");
        for (UserDto deleter : deleters) {
            deleteRoles(
                    getAccessToken(
                            deleter.getUsername(),
                            deleter.getPassword()
                    ),
                    testSet,
                    ENABLE,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Delete_Roles_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto deleter = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                deleter.getUsername(),
                deleter.getPassword()
        );
        for (String invalidRoleName : INVALID_ROLE_OR_PERMISSION_NAMES) {
            deleteRoles(
                    accessToken,
                    Set.of(invalidRoleName),
                    ENABLE,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        deleteRoles(
                accessToken,
                Set.of(ROLE_SUPER_ADMIN.name()),
                ENABLE,
                null
        ).then()
                .statusCode(400)
                .body("system_roles_cannot_be_deleted", containsInAnyOrder(ROLE_SUPER_ADMIN.name()));
    }

    @Test
    public void test_Read_Roles_Using_User_With_Role_Who_Can_Read_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> readers = new HashSet<>();
        readers.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES) {
            readers.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(readers);
        Response response;
        RoleDto role = createTestRole();
        Set<String> roleNames = Set.of(role.getRoleName());
        Set<RoleDto> tempSet = Set.of(role);
        for (UserDto reader : readers) {
            response = readRoles(
                    getAccessToken(
                            reader.getUsername(),
                            reader.getPassword()
                    ),
                    roleNames,
                    null
            );
            validateResponseOfRolesCreationOrRead(
                    response,
                    reader,
                    tempSet,
                    "found_roles."
            );
        }
    }

    @Test
    public void test_Read_Roles_Using_User_With_Role_Who_Cannot_Read_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> readers = new HashSet<>();
        readers.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES) {
            readers.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(readers);
        Set<String> testSet = Set.of("someRoleName");
        for (UserDto reader : readers) {
            readRoles(
                    getAccessToken(
                            reader.getUsername(),
                            reader.getPassword()
                    ),
                    testSet,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Read_Roles_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto reader = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                reader.getUsername(),
                reader.getPassword()
        );
        for (String invalidRoleName : INVALID_ROLE_OR_PERMISSION_NAMES) {
            readRoles(
                    accessToken,
                    Set.of(invalidRoleName),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_role_names", not(empty()));
        }
    }

    @Test
    public void test_Update_Roles_Using_User_With_Role_Who_Can_Update_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> updaters = new HashSet<>();
        updaters.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CAN_CREATE_DELETE_READ_UPDATE_ROLES) {
            updaters.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(updaters);
        Set<RoleDto> rolesThatCanBeUpdated = createRandomRoleDtos(updaters.size());
        createTestRoles(rolesThatCanBeUpdated);
        Set<RoleDto> updatedInputs = new HashSet<>();
        Map<String, RoleDto> roleNameToRoleMap = new HashMap<>();
        for (RoleDto role : rolesThatCanBeUpdated) {
            roleNameToRoleMap.put(role.getRoleName(), role);
            updatedInputs.add(new RoleDto(
                            role.getRoleName(),
                            role.getDescription() + " - updated",
                            role.getPermissions() != null ? new HashSet<>(role.getPermissions()) : null
                    )
            );
        }
        Iterator<RoleDto> iterator = updatedInputs.iterator();
        Response response;
        RoleDto updatedInput;
        Set<RoleDto> tempSet;
        for (UserDto updater : updaters) {
            updatedInput = iterator.next();
            tempSet = Set.of(updatedInput);
            response = updateRoles(
                    getAccessToken(
                            updater.getUsername(),
                            updater.getPassword()
                    ),
                    tempSet,
                    null
            );
            validateResponseOfRolesUpdation(
                    response,
                    updater,
                    Set.of(roleNameToRoleMap.get(updatedInput.getRoleName())),
                    tempSet,
                    "updated_roles."
            );
        }
    }

    @Test
    public void test_Update_Roles_Using_User_With_Role_Who_Cannot_Update_Roles() throws ExecutionException, InterruptedException {
        Set<UserDto> updaters = new HashSet<>();
        updaters.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_CREATE_READ_UPDATE_DELETE_ROLES) {
            updaters.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(updaters);
        Set<RoleDto> updatedInputs = Set.of(createRandomRoleDto());
        for (UserDto updater : updaters) {
            updateRoles(
                    getAccessToken(
                            updater.getUsername(),
                            updater.getPassword()
                    ),
                    updatedInputs,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Update_Roles_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto updater = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                updater.getUsername(),
                updater.getPassword()
        );
        RoleDto role = new RoleDto();
        Set<RoleDto> testSet = Set.of(role);
        for (String invalidRoleName : INVALID_ROLE_OR_PERMISSION_NAMES) {
            role.setRoleName(invalidRoleName);
            updateRoles(
                    accessToken,
                    testSet,
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        String randomString = getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric();
        role.setRoleName("AutoTestRole_" + randomString);
        role.setDescription("d".repeat(256));
        updateRoles(
                accessToken,
                testSet,
                null
        ).then()
                .statusCode(400)
                .body("invalid_inputs", not(empty()));
    }

    @Test
    public void test_Read_Permissions_Using_User_With_Role_Who_Can_Read_Permissions() throws ExecutionException, InterruptedException {
        Set<UserDto> readers = new HashSet<>();
        readers.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CAN_READ_PERMISSIONS));
        for (String role : USERS_WITH_THESE_ROLES_CAN_READ_PERMISSIONS) {
            readers.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(readers);
        Set<String> permissionNames = Set.of(CAN_CREATE_USER.name());
        for (UserDto reader : readers) {
            readPermissions(
                    getAccessToken(
                            reader.getUsername(),
                            reader.getPassword()
                    ),
                    permissionNames,
                    null
            ).then()
                    .statusCode(200)
                    .body("found_permissions.size()", equalTo(permissionNames.size()))
                    .body("found_permissions.permissionName", containsInAnyOrder(permissionNames.toArray()));
        }
    }

    @Test
    public void test_Read_Permissions_Using_User_With_Role_Who_Cannot_Read_Permissions() throws ExecutionException, InterruptedException {
        Set<UserDto> readers = new HashSet<>();
        readers.add(createRandomUserDto(USERS_WITH_THESE_ROLES_CANNOT_READ_PERMISSIONS));
        for (String role : USERS_WITH_THESE_ROLES_CANNOT_READ_PERMISSIONS) {
            readers.add(createRandomUserDto(Set.of(role)));
        }
        createTestUsers(readers);
        Set<String> testSet = Set.of(CAN_CREATE_USER.name());
        for (UserDto reader : readers) {
            readPermissions(
                    getAccessToken(
                            reader.getUsername(),
                            reader.getPassword()
                    ),
                    testSet,
                    null
            ).then()
                    .statusCode(403)
                    .body("message", containsStringIgnoringCase("Access Denied"));
        }
    }

    @Test
    public void test_Read_Permissions_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto reader = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        String accessToken = getAccessToken(
                reader.getUsername(),
                reader.getPassword()
        );
        for (String invalidPermissionName : INVALID_ROLE_OR_PERMISSION_NAMES) {
            readPermissions(
                    accessToken,
                    Set.of(invalidPermissionName),
                    null
            ).then()
                    .statusCode(400)
                    .body("invalid_permission_names", not(empty()));
        }
    }
}
