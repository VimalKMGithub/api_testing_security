package org.vimal.tests;

import com.google.zxing.NotFoundException;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.*;
import static org.vimal.constants.Common.AUTHENTICATOR_APP_MFA;
import static org.vimal.constants.Common.ENABLE;
import static org.vimal.helpers.InvalidInputsHelper.*;
import static org.vimal.utils.DateTimeUtility.getCurrentFormattedLocalTimeStamp;
import static org.vimal.utils.QrUtility.extractSecretFromByteArrayOfQrCode;
import static org.vimal.utils.RandomStringUtility.generateRandomStringAlphaNumeric;
import static org.vimal.utils.TotpUtility.generateTotp;

public class AuthenticationServiceTests extends BaseTest {
    @Test
    public void test_Login_Success(ITestContext context) throws ExecutionException, InterruptedException {
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
    public void test_Login_Failure_InvalidCredentials() throws ExecutionException, InterruptedException {
        for (String invalidUsername : INVALID_USERNAMES) {
            login(
                    invalidUsername,
                    "SomePassword@1"
            ).then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        for (String invalidEmail : INVALID_EMAILS) {
            login(
                    invalidEmail,
                    "SomePassword@1"
            ).then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        for (String invalidPassword : INVALID_PASSWORDS) {
            login(
                    "SomeUser",
                    invalidPassword
            ).then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Invalid credentials"));
        }
        login(
                "nonexistentUser_" + getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric(),
                "SomePassword@1"
        ).then()
                .statusCode(401)
                .body("error", containsStringIgnoringCase("Unauthorized"))
                .body("message", containsStringIgnoringCase("Invalid credentials"));
    }

    @Test
    public void test_Account_Lockout_After_Multiple_Failed_Login_Attempts() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        for (int i = 0; i < 5; i++) {
            login(
                    user.getUsername(),
                    "WrongPassword@1"
            ).then()
                    .statusCode(401)
                    .body("error", containsStringIgnoringCase("Unauthorized"))
                    .body("message", containsStringIgnoringCase("Bad credentials"));
        }
        login(
                user.getUsername(),
                "WrongPassword@1"
        ).then()
                .statusCode(401)
                .body("error", containsStringIgnoringCase("Unauthorized"))
                .body("message", containsStringIgnoringCase("Account is temporarily locked"));
    }

    @Test(dependsOnMethods = {
            "test_Login_Success",
            "test_Request_To_Enable_Authenticator_App_Mfa_Success",
            "test_Verify_To_Enable_Authenticator_App_Mfa_Success"
    })
    public void test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled(ITestContext context) throws ExecutionException, InterruptedException {
        String attributeName = "user_from_test_Login_Success";
        UserDto user = (UserDto) context.getAttribute(attributeName);
        context.removeAttribute(attributeName);
        Response response = login(
                user.getUsername(),
                user.getPassword()
        );
        response.then()
                .statusCode(200)
                .body("state_token", notNullValue());
        context.setAttribute("state_token_from_test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled", response.jsonPath()
                .getString("state_token"));
    }

    @Test
    public void test_Logout_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        logout(getAccessToken(
                        user.getUsername(),
                        user.getPassword()
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Logout successful"));
    }

    @Test(dependsOnMethods = {"test_Revoke_Access_Token_Success"})
    public void test_Refresh_Access_Token_Success(ITestContext context) throws ExecutionException, InterruptedException {
        String attributeName = "refresh_token_from_test_Revoke_Access_Token_Success";
        Response response = refreshAccessToken((String) context.getAttribute(attributeName));
        context.removeAttribute(attributeName);
        response.then()
                .statusCode(200)
                .body("access_token", notNullValue())
                .body("expires_in_seconds", equalTo(1800))
                .body("token_type", containsStringIgnoringCase("Bearer"));
    }

    @Test
    public void test_Refresh_Access_Token_Failure_Invalid_Refresh_Token() throws ExecutionException, InterruptedException {
        refreshAccessToken("invalidRefreshToken").then()
                .statusCode(400)
                .body("error", containsStringIgnoringCase("Bad Request"))
                .body("message", containsStringIgnoringCase("Invalid refresh token"));
        for (String invalidRefreshToken : INVALID_UUIDS) {
            refreshAccessToken(invalidRefreshToken).then()
                    .statusCode(400)
                    .body("error", containsStringIgnoringCase("Bad Request"))
                    .body("message", containsStringIgnoringCase("Invalid refresh token"));
        }
    }

    @Test
    public void test_Revoke_Access_Token_Success(ITestContext context) throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        Response response = login(
                user.getUsername(),
                user.getPassword()
        );
        response.then()
                .statusCode(200);
        context.setAttribute("refresh_token_from_test_Revoke_Access_Token_Success", response.jsonPath()
                .getString("refresh_token"));
        response = revokeAccessToken(response.jsonPath()
                .getString("access_token"));
        response.then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Access token revoked successfully"));
    }

    @Test
    public void test_Revoke_Refresh_Token_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        revokeRefreshToken(getRefreshToken(
                        user.getUsername(),
                        user.getPassword()
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Refresh token revoked successfully"));
    }

    @Test(dependsOnMethods = {"test_Login_Success"})
    public void test_Request_To_Enable_Authenticator_App_Mfa_Success(ITestContext context) throws ExecutionException, InterruptedException {
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
            "test_Request_To_Enable_Authenticator_App_Mfa_Success",
            "test_Verify_To_Enable_Authenticator_App_Mfa_Success",
            "test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled",
            "test_Verify_Mfa_To_Login_Success"
    })
    public void test_Request_To_Enable_Authenticator_App_Mfa_Failure_Already_Enabled(ITestContext context) throws ExecutionException, InterruptedException {
        requestToToggleMfa(
                (String) context.getAttribute("access_token_from_test_Verify_Mfa_To_Login_Success"),
                AUTHENTICATOR_APP_MFA,
                ENABLE
        ).then()
                .statusCode(400)
                .body("error", containsStringIgnoringCase("Bad Request"))
                .body("message", containsStringIgnoringCase("Mfa is already enabled"));
    }

    @Test(dependsOnMethods = {
            "test_Login_Success",
            "test_Request_To_Enable_Authenticator_App_Mfa_Success"
    })
    public void test_Verify_To_Enable_Authenticator_App_Mfa_Success(ITestContext context) throws NotFoundException, IOException, InvalidKeyException, ExecutionException, InterruptedException {
        String attributeName = "access_token_from_test_Login_Success";
        Response response = verifyToggleMfa(
                (String) context.getAttribute(attributeName),
                AUTHENTICATOR_APP_MFA,
                ENABLE,
                generateTotp(extractSecretFromByteArrayOfQrCode((byte[]) context.getAttribute("mfa_secret_from_test_Request_To_Enable_Authenticator_App_Mfa_Success")))
        );
        context.removeAttribute(attributeName);
        response.then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Authenticator app Mfa enabled successfully"));
    }

    @Test(dependsOnMethods = {
            "test_Login_Success",
            "test_Request_To_Enable_Authenticator_App_Mfa_Success",
            "test_Verify_To_Enable_Authenticator_App_Mfa_Success",
            "test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled",
            "test_Verify_Mfa_To_Login_Success"
    })
    public void test_Verify_To_Enable_Authenticator_App_Mfa_Failure_Already_Enabled(ITestContext context) throws ExecutionException, InterruptedException {
        verifyToggleMfa(
                (String) context.getAttribute("access_token_from_test_Verify_Mfa_To_Login_Success"),
                AUTHENTICATOR_APP_MFA,
                ENABLE,
                "123456"
        ).then()
                .statusCode(400)
                .body("error", containsStringIgnoringCase("Bad Request"))
                .body("message", containsStringIgnoringCase("Mfa is already enabled"));
    }

    @Test
    public void test_Verify_To_Enable_Authenticator_App_Mfa_Failure_Invalid_Otp() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        verifyToggleMfa(
                accessToken,
                AUTHENTICATOR_APP_MFA,
                ENABLE,
                "123456"
        ).then()
                .statusCode(400)
                .body("error", containsStringIgnoringCase("Bad Request"))
                .body("message", containsStringIgnoringCase("Invalid Totp"));
        for (String invalidOtp : INVALID_OTPS) {
            verifyToggleMfa(
                    accessToken,
                    AUTHENTICATOR_APP_MFA,
                    ENABLE,
                    invalidOtp
            ).then()
                    .statusCode(400)
                    .body("error", containsStringIgnoringCase("Bad Request"))
                    .body("message", containsStringIgnoringCase("Invalid Otp/Totp"));
        }
    }

    @Test(dependsOnMethods = {
            "test_Login_Success",
            "test_Request_To_Enable_Authenticator_App_Mfa_Success",
            "test_Verify_To_Enable_Authenticator_App_Mfa_Success",
            "test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled"
    })
    public void test_Verify_Mfa_To_Login_Success(ITestContext context) throws InvalidKeyException, NotFoundException, IOException, ExecutionException, InterruptedException {
        String attributeName = "state_token_from_test_Get_StateToken_On_Login_When_Any_Mfa_Is_Enabled";
        String attributeNameForMfa = "mfa_secret_from_test_Request_To_Enable_Authenticator_App_Mfa_Success";
        Response response = verifyMfaToLogin(
                AUTHENTICATOR_APP_MFA,
                (String) context.getAttribute(attributeName),
                generateTotp(extractSecretFromByteArrayOfQrCode((byte[]) context.getAttribute(attributeNameForMfa)))
        );
        context.removeAttribute(attributeName);
        context.removeAttribute(attributeNameForMfa);
        response.then()
                .statusCode(200)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue())
                .body("expires_in_seconds", equalTo(1800))
                .body("token_type", containsStringIgnoringCase("Bearer"));
        context.setAttribute("access_token_from_test_Verify_Mfa_To_Login_Success", response.jsonPath()
                .getString("access_token"));
    }
}
