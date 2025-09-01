package org.vimal;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static org.vimal.helpers.AuthCallsHelper.getAccessToken;
import static org.vimal.helpers.AuthCallsHelper.logout;

@Slf4j
public abstract class BaseTest {
    private static final String BASE_URL = "http://localhost:8080";
    private static final String BASE_PATH = "api/v1";
    public static final String TEST_EMAIL = System.getenv("TEST_EMAIL");
    public static final String TEST_EMAIL_PASSWORD = System.getenv("TEST_EMAIL_PASSWORD");
    public static final String GLOBAL_ADMIN_USERNAME = System.getenv("GLOBAL_ADMIN_USERNAME");
    public static final String GLOBAL_ADMIN_PASSWORD = System.getenv("GLOBAL_ADMIN_PASSWORD");
    public static String GLOBAL_ADMIN_ACCESS_TOKEN;

    @BeforeSuite
    public void setUpBeforeSuite() {
        log.info("Setting RestAssured with base Url: '{}' & base path: '{}'", BASE_URL, BASE_PATH);
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = BASE_PATH;
        log.info("Enabling logging of request & response if validation fails.");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        GLOBAL_ADMIN_ACCESS_TOKEN = getAccessToken(GLOBAL_ADMIN_USERNAME, GLOBAL_ADMIN_PASSWORD);
    }

    @AfterSuite
    public void cleanupAfterSuite() {
        log.info("Cleaning up environment after all tests.");
        try {
            logout(GLOBAL_ADMIN_ACCESS_TOKEN);
        } catch (Exception ignored) {
        }
        log.info("Cleanup completed.");
    }
}
