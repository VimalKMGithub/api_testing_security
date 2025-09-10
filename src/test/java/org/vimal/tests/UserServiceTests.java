package org.vimal.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.UserCalls.register;
import static org.vimal.helpers.DtosHelper.createRandomUserDto;

public class UserServiceTests extends BaseTest {
    @Test
    public void test_Registration_Success() throws ExecutionException, InterruptedException {
        UserDto user = createRandomUserDto();
        TEST_USERS.add(user);
        Response response = register(user);
        response.then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Registration successful"))
                .body("user.id", notNullValue())
                .body("user.username", equalTo(user.getUsername()))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.firstName", equalTo(user.getFirstName()))
                .body("user.middleName", equalTo(user.getMiddleName()))
                .body("user.lastName", equalTo(user.getLastName()))
                .body("user.createdBy", containsStringIgnoringCase("SELF"));
    }
}
