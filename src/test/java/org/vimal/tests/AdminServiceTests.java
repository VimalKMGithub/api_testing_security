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

import static org.vimal.api.AdminCalls.createUsers;
import static org.vimal.api.AuthenticationCalls.getAccessToken;
import static org.vimal.constants.Common.MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME;
import static org.vimal.enums.Roles.*;
import static org.vimal.helpers.DtosHelper.createRandomUserDto;
import static org.vimal.helpers.ResponseValidatorHelper.validateResponseOfUsersCreation;

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

    @Test
    public void test_Create_Users_Using_User_With_Role_Super_Admin() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(Set.of(ROLE_SUPER_ADMIN.name()));
        Set<UserDto> usersThatCanBeCreatedBySuperAdmin = new HashSet<>();
        usersThatCanBeCreatedBySuperAdmin.add(createRandomUserDto());
        usersThatCanBeCreatedBySuperAdmin.add(createRandomUserDto(ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_SUPER_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeCreatedBySuperAdmin.add(createRandomUserDto(Set.of(role)));
        }
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        Iterator<UserDto> iterator = usersThatCanBeCreatedBySuperAdmin.iterator();
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
                    batch
            );
        }
    }

    @Test
    public void test_Create_Users_Using_User_With_Role_Admin() throws ExecutionException, InterruptedException {
        UserDto creator = createTestUser(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS);
        Set<UserDto> usersThatCanBeCreatedByAdmin = new HashSet<>();
        usersThatCanBeCreatedByAdmin.add(createRandomUserDto());
        usersThatCanBeCreatedByAdmin.add(createRandomUserDto(ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS));
        for (String role : ROLE_SET_FOR_ADMIN_CAN_CREATE_UPDATE_DELETE_USERS) {
            usersThatCanBeCreatedByAdmin.add(createRandomUserDto(Set.of(role)));
        }
        String accessToken = getAccessToken(
                creator.getUsername(),
                creator.getPassword()
        );
        Iterator<UserDto> iterator = usersThatCanBeCreatedByAdmin.iterator();
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
                    batch
            );
        }
    }
}
