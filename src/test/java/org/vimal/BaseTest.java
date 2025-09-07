package org.vimal;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.vimal.dtos.RoleDto;
import org.vimal.dtos.UserDto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.vimal.api.AdminCallsUsingGlobalAdminUser.createUsers;
import static org.vimal.api.AuthenticationCalls.getAccessToken;
import static org.vimal.api.AuthenticationCalls.logout;
import static org.vimal.constants.Common.MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME;
import static org.vimal.helpers.CleanUpHelper.cleanUpTestUsers;
import static org.vimal.helpers.DtosHelper.createRandomUserDto;
import static org.vimal.helpers.DtosHelper.createRandomUserDtoWithRandomValidEmail;

@Slf4j
public abstract class BaseTest {
    protected static final Set<UserDto> TEST_USERS = ConcurrentHashMap.newKeySet();
    protected static final Set<RoleDto> TEST_ROLES = ConcurrentHashMap.newKeySet();
    private static final String BASE_URL = "http://localhost:8080";
    private static final String BASE_PATH = "api/v1";
    public static final String TEST_EMAIL = System.getenv("TEST_EMAIL");
    public static final String TEST_EMAIL_PASSWORD = System.getenv("TEST_EMAIL_PASSWORD");
    public static final String GLOBAL_ADMIN_USERNAME = System.getenv("GLOBAL_ADMIN_USERNAME");
    public static final String GLOBAL_ADMIN_PASSWORD = System.getenv("GLOBAL_ADMIN_PASSWORD");
    public static String GLOBAL_ADMIN_ACCESS_TOKEN;

    @BeforeSuite
    public void setUpBeforeSuite() {
        log.info(
                "Setting RestAssured with base Url: '{}' & base path: '{}'",
                BASE_URL,
                BASE_PATH
        );
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = BASE_PATH;
        log.info("Enabling logging of request & response if validation fails.");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        GLOBAL_ADMIN_ACCESS_TOKEN = getAccessToken(
                GLOBAL_ADMIN_USERNAME,
                GLOBAL_ADMIN_PASSWORD
        );
    }

    @AfterSuite
    public void cleanupAfterSuite() {
        log.info("Cleaning up environment after all tests.");
        if (!TEST_USERS.isEmpty()) {
            log.info("Deleting test users.");
            cleanUpTestUsers(TEST_USERS);
            TEST_USERS.clear();
        }
        try {
            logout(GLOBAL_ADMIN_ACCESS_TOKEN);
        } catch (Exception ignored) {
        }
        log.info("Cleanup completed.");
    }

    protected static UserDto createTestUser() {
        return createTestUser(createRandomUserDto());
    }

    private static UserDto createTestUserRandomValidEmail() {
        return createTestUser(createRandomUserDtoWithRandomValidEmail());
    }

    private static UserDto createTestUser(Set<String> roles) {
        return createTestUser(createRandomUserDto(roles));
    }

    private static UserDto createTestUser(UserDto user) {
        createTestUsers(Set.of(user));
        return user;
    }

    private static void createTestUsers(Set<UserDto> users) {
        Iterator<UserDto> iterator = users.iterator();
        Set<UserDto> batch = new HashSet<>();
        Response response;
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_CREATION_AT_A_TIME) {
                batch.add(iterator.next());
            }
            response = createUsers(
                    batch,
                    null
            );
            response.then()
                    .statusCode(200);
            TEST_USERS.addAll(batch);
        }
    }
}
