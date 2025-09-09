package org.vimal.tests;

import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.login;
import static org.vimal.helpers.InvalidInputsHelper.*;

public class AuthenticationServiceTests extends BaseTest {
    @Test
    public void test_Login_Success(ITestContext context) {
        UserDto user = createTestUser();
        Response response = login(
                user.getUsername(),
                user.getPassword()
        );
        response.then().statusCode(200)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue())
                .body("expires_in_seconds", equalTo(1800))
                .body("token_type", containsStringIgnoringCase("Bearer"));
        context.setAttribute("user_of_test_Login_Success", user);
        context.setAttribute("access_token", response.jsonPath()
                .getString("access_token"));
    }

    @Test
    public void test_Login_Failure_InvalidCredentials() {
        Response response = login(
                "invalidUser",
                "wrongPassword@1"
        );
        response.then().statusCode(401)
                .body("error", containsStringIgnoringCase("Unauthorized"))
                .body("message", containsStringIgnoringCase("Invalid credentials"));
        for (String invalidUsername : INVALID_USERNAMES) {
            response = login(
                    invalidUsername,
                    "SomePassword@1"
            );
            response.then().statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        for (String invalidEmail : INVALID_EMAILS) {
            response = login(
                    invalidEmail,
                    "SomePassword@1"
            );
            response.then().statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        for (String invalidPassword : INVALID_PASSWORDS) {
            response = login(
                    "SomeUser",
                    invalidPassword
            );
            response.then().statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
    }

    @Test
    public void test_Account_Lockout_After_Multiple_Failed_Login_Attempts() {
        UserDto user = createTestUser();
        for (int i = 0; i < 5; i++) {
            Response response = login(
                    user.getUsername(),
                    "WrongPassword@1"
            );
            response.then().statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Bad credentials"));
        }
        Response response = login(
                user.getUsername(),
                "WrongPassword@1"
        );
        response.then().statusCode(401)
                .body("error", containsStringIgnoringCase("Unauthorized"))
                .body("message", containsStringIgnoringCase("Account is temporarily locked"));
    }
}
