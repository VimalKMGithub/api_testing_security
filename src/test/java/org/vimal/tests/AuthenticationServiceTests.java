package org.vimal.tests;

import com.google.zxing.NotFoundException;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import java.io.IOException;
import java.security.InvalidKeyException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.*;
import static org.vimal.constants.Common.AUTHENTICATOR_APP_MFA;
import static org.vimal.constants.Common.ENABLE;
import static org.vimal.helpers.InvalidInputsHelper.*;
import static org.vimal.utils.QrUtility.extractSecretFromByteArrayOfQrCode;
import static org.vimal.utils.TotpUtility.generateTotp;

public class AuthenticationServiceTests extends BaseTest {
    @Test
    public void test_Login_Success(ITestContext context) {
        UserDto user = createTestUser();
        Response response = login(
                user.getUsername(),
                user.getPassword()
        );
        response.then()
                .statusCode(200)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue())
                .body("expires_in_seconds", equalTo(1800))
                .body("token_type", containsStringIgnoringCase("Bearer"));
        context.setAttribute("user_from_test_Login_Success", user);
        context.setAttribute("access_token_from_test_Login_Success", response.jsonPath()
                .getString("access_token"));
    }

    @Test
    public void test_Login_Failure_InvalidCredentials() {
        Response response = login(
                "invalidUser",
                "wrongPassword@1"
        );
        response.then()
                .statusCode(401)
                .body("error", containsStringIgnoringCase("Unauthorized"))
                .body("message", containsStringIgnoringCase("Invalid credentials"));
        for (String invalidUsername : INVALID_USERNAMES) {
            response = login(
                    invalidUsername,
                    "SomePassword@1"
            );
            response.then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        for (String invalidEmail : INVALID_EMAILS) {
            response = login(
                    invalidEmail,
                    "SomePassword@1"
            );
            response.then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        for (String invalidPassword : INVALID_PASSWORDS) {
            response = login(
                    "SomeUser",
                    invalidPassword
            );
            response.then()
                    .statusCode(401)
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
            response.then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Bad credentials"));
        }
        Response response = login(
                user.getUsername(),
                "WrongPassword@1"
        );
        response.then()
                .statusCode(401)
                .body("error", containsStringIgnoringCase("Unauthorized"))
                .body("message", containsStringIgnoringCase("Account is temporarily locked"));
    }

    @Test(dependsOnMethods = {"test_Login_Success"})
    public void test_Request_To_Enable_Authenticator_App_Mfa_Success(ITestContext context) {
        Response response = requestToToggleMfa(
                (String) context.getAttribute("access_token_from_test_Login_Success"),
                AUTHENTICATOR_APP_MFA,
                ENABLE
        );
        response.then()
                .statusCode(200)
                .contentType("image/png");
        context.setAttribute("mfa_secret_from_test_Request_To_Enable_Authenticator_App_Mfa_Success", response.asByteArray());
    }

    @Test(dependsOnMethods = {
            "test_Login_Success",
            "test_Request_To_Enable_Authenticator_App_Mfa_Success"
    })
    public void test_Verify_To_Enable_Authenticator_App_Mfa_Success(ITestContext context) throws NotFoundException, IOException, InvalidKeyException {
        Response response = verifyToggleMfa(
                (String) context.getAttribute("access_token_from_test_Login_Success"),
                AUTHENTICATOR_APP_MFA,
                ENABLE,
                generateTotp(extractSecretFromByteArrayOfQrCode((byte[]) context.getAttribute("mfa_secret_from_test_Request_To_Enable_Authenticator_App_Mfa_Success")))
        );
        response.then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Authenticator app Mfa enabled successfully"));
    }

    @Test(dependsOnMethods = {
            "test_Login_Success",
            "test_Request_To_Enable_Authenticator_App_Mfa_Success",
            "test_Verify_To_Enable_Authenticator_App_Mfa_Success"
    })
    public void test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled(ITestContext context) {
    }
}
