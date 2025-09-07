package org.vimal.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.login;
import static org.vimal.helpers.InvalidInputsHelper.*;

public class AuthenticationServiceTests extends BaseTest {
    @Test
    public void test_Login_Success() {
        UserDto user = createTestUser();
        Response response = login(user.getUsername(), user.getPassword());
        response.then().statusCode(200)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue())
                .body("expires_in_seconds", equalTo(1800))
                .body("token_type", containsString("Bearer"));
    }

    @Test
    public void test_Login_Failure_InvalidCredentials() {
        Response response = login("invalidUser", "wrongPassword@1");
        response.then().statusCode(401)
                .body("error", containsString("Unauthorized"))
                .body("message", containsString("Invalid credentials"));
        for (String invalidUsername : INVALID_USERNAMES) {
            response = login(invalidUsername, "SomePassword@1");
            response.then().statusCode(401)
                    .body("error", containsString("Unauthorized"))
                    .body("message", containsString("Invalid credentials"));
        }
        for (String invalidEmail : INVALID_EMAILS) {
            response = login(invalidEmail, "SomePassword@1");
            response.then().statusCode(401)
                    .body("error", containsString("Unauthorized"))
                    .body("message", containsString("Invalid credentials"));
        }
        for (String invalidPassword : INVALID_PASSWORDS) {
            response = login("SomeUser", invalidPassword);
            response.then().statusCode(401)
                    .body("error", containsString("Unauthorized"))
                    .body("message", containsString("Invalid credentials"));
        }
    }
}
