package org.vimal.tests;

import com.google.zxing.NotFoundException;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.*;
import static org.vimal.api.UserCalls.*;
import static org.vimal.constants.Common.*;
import static org.vimal.helpers.DtosHelper.createRandomUserDto;
import static org.vimal.helpers.InvalidInputsHelper.INVALID_OTPS;
import static org.vimal.helpers.InvalidInputsHelper.INVALID_PASSWORDS;
import static org.vimal.utils.QrUtility.extractSecretFromByteArrayOfQrCode;
import static org.vimal.utils.TotpUtility.generateTotp;

public class UserServiceTests extends BaseTest {
    @Test
    public void test_Registration_Success() throws ExecutionException, InterruptedException {
        UserDto user = createRandomUserDto();
        TEST_USERS.add(user);
        register(user).then()
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

    @Test
    public void test_Get_Self_Details_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        getSelfDetails(getAccessToken(
                        user.getUsername(),
                        user.getPassword()
                )
        ).then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("username", equalTo(user.getUsername()))
                .body("email", equalTo(user.getEmail()))
                .body("firstName", equalTo(user.getFirstName()))
                .body("middleName", equalTo(user.getMiddleName()))
                .body("lastName", equalTo(user.getLastName()));
    }

    @Test
    public void test_Forgot_Password_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        forgotPassword(user.getUsername()).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Please select a method for password reset"))
                .body("methods", hasItem(EMAIL_MFA));
    }

    @Test
    public void test_Forgot_Password_Failure_Email_Not_Verified() throws ExecutionException, InterruptedException {
        UserDto user = createRandomUserDto();
        user.setEmailVerified(false);
        createTestUser(user);
        forgotPassword(user.getUsername()).then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("Email is not verified"));
        forgotPassword(user.getEmail()).then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("Email is not verified"));
    }

    @Test
    public void test_Forgot_Password_Method_Selection_Success(ITestContext context) throws ExecutionException, InterruptedException, NotFoundException, IOException, InvalidKeyException {
        Map<String, Object> map = createTestUserAuthenticatorAppMfaEnabled();
        UserDto user = (UserDto) map.get("user");
        context.setAttribute("user_from_test_Forgot_Password_Method_Selection_Success", user);
        context.setAttribute("secret_from_test_Forgot_Password_Method_Selection_Success", map.get("secret"));
        forgotPasswordMethodSelection(
                user.getUsername(),
                AUTHENTICATOR_APP_MFA
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Please proceed to verify Totp"));
    }

    private Map<String, Object> createTestUserAuthenticatorAppMfaEnabled() throws ExecutionException, InterruptedException, NotFoundException, IOException, InvalidKeyException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        Response response = requestToToggleMfa(
                accessToken,
                AUTHENTICATOR_APP_MFA,
                ENABLE
        );
        response.then()
                .statusCode(200)
                .contentType("image/png");
        String secret = extractSecretFromByteArrayOfQrCode(response.asByteArray());
        verifyToggleMfa(
                accessToken,
                AUTHENTICATOR_APP_MFA,
                ENABLE,
                generateTotp(secret)
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Authenticator app Mfa enabled successfully"));
        return Map.of(
                "user", user,
                "accessToken", accessToken,
                "secret", secret
        );
    }

    @Test(dependsOnMethods = {"test_Forgot_Password_Method_Selection_Success"})
    public void test_Reset_Password_Success(ITestContext context) throws ExecutionException, InterruptedException, InvalidKeyException {
        String attributeName = "user_from_test_Forgot_Password_Method_Selection_Success";
        String attributeNameForSecret = "secret_from_test_Forgot_Password_Method_Selection_Success";
        UserDto user = (UserDto) context.getAttribute(attributeName);
        context.removeAttribute(attributeName);
        context.removeAttribute(attributeNameForSecret);
        resetPassword(Map.of(
                        "usernameOrEmail", user.getUsername(),
                        "otpTotp", generateTotp(attributeNameForSecret),
                        "method", AUTHENTICATOR_APP_MFA,
                        "password", "NewPassword@123",
                        "confirmPassword", "NewPassword@123"
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Password reset successful"));
    }

    @Test
    public void test_Reset_Password_Invalid_Inputs() throws ExecutionException, InterruptedException {
        Map<String, String> map = new HashMap<>();
        map.put("usernameOrEmail", "SomeUsername");
        map.put("method", EMAIL_MFA);
        map.put("password", "SomePassword@123");
        map.put("confirmPassword", "SomePassword@123");
        for (String invalidOtp : INVALID_OTPS) {
            map.put("otpTotp", invalidOtp);
            resetPassword(map).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        map.put("otpTotp", "123456");
        for (String invalidPassword : INVALID_PASSWORDS) {
            map.put("password", invalidPassword);
            resetPassword(map).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        map.put("password", "ValidPassword@123");
        map.put("confirmPassword", "DifferentPassword@123");
        resetPassword(map).then()
                .statusCode(400)
                .body("invalid_inputs", not(empty()));
    }

    @Test
    public void test_Change_Password_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        changePassword(
                getAccessToken(
                        user.getUsername(),
                        user.getPassword()
                ),
                Map.of(
                        "oldPassword", user.getPassword(),
                        "password", "NewPassword@123",
                        "confirmPassword", "NewPassword@123"
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Password changed successfully"));
    }

    @Test
    public void test_Change_Password_Invalid_Inputs() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        Map<String, String> map = new HashMap<>();
        for (String invalidPassword : INVALID_PASSWORDS) {
            map.put("oldPassword", invalidPassword);
            changePassword(
                    accessToken,
                    map
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        map.put("oldPassword", user.getPassword());
        for (String invalidPassword : INVALID_PASSWORDS) {
            map.put("oldPassword", invalidPassword);
            changePassword(
                    accessToken,
                    map
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        map.put("password", "ValidPassword@123");
        map.put("confirmPassword", "DifferentPassword@123");
        changePassword(
                accessToken,
                map
        ).then()
                .statusCode(400)
                .body("invalid_inputs", not(empty()));
        map.put("confirmPassword", "ValidPassword@123");
        map.put("oldPassword", "SomeWrongOldPassword@123");
        changePassword(
                accessToken,
                map
        ).then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("Invalid old password"));
    }

    @Test
    public void test_Change_Password_Method_Selection_Success(ITestContext context) throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        context.setAttribute("access_token_from_test_Change_Password_Method_Selection_Success", accessToken);
        changePasswordMethodSelection(
                accessToken,
                EMAIL_MFA
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Otp sent"));
    }
}
