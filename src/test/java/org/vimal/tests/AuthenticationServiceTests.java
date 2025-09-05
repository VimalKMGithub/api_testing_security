package org.vimal.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.login;

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
}
