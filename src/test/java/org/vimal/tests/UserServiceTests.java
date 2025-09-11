package org.vimal.tests;

import com.google.zxing.NotFoundException;
import io.restassured.response.Response;
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
import static org.vimal.constants.Common.AUTHENTICATOR_APP_MFA;
import static org.vimal.constants.Common.ENABLE;
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
                .statusCode(200);
        String secret = extractSecretFromByteArrayOfQrCode(response.asByteArray());
        verifyToggleMfa(
                accessToken,
                AUTHENTICATOR_APP_MFA,
                ENABLE,
                generateTotp(secret)
        ).then()
                .statusCode(200);
        return Map.of(
                "user", user,
                "secret", secret
        );
    }

    @Test
    public void test_Reset_Password_Success() throws ExecutionException, InterruptedException, InvalidKeyException, NotFoundException, IOException {
        Map<String, Object> map = createTestUserAuthenticatorAppMfaEnabled();
        resetPassword(Map.of(
                        "usernameOrEmail", ((UserDto) map.get("user")).getUsername(),
                        "otpTotp", generateTotp((String) map.get("secret")),
                        "method", AUTHENTICATOR_APP_MFA,
                        "password", "NewPassword@123",
                        "confirmPassword", "NewPassword@123"
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Password reset successful"));
    }

    @Test
    public void test_Reset_Password_Failure_Invalid_Input() throws ExecutionException, InterruptedException {
        Map<String, String> map = new HashMap<>();
        map.put("usernameOrEmail", "SomeUsername");
        map.put("method", AUTHENTICATOR_APP_MFA);
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
    public void test_Change_Password_Failure_Invalid_Input() throws ExecutionException, InterruptedException {
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
    }

    @Test
    public void test_Verify_Change_Password_Success() throws NotFoundException, IOException, ExecutionException, InvalidKeyException, InterruptedException {
        Map<String, Object> map = createTestUserAuthenticatorAppMfaEnabled();
        verifyChangePassword(
                getAccessTokenForUserWhoseAuthenticatorAppMfaIsEnabled(map),
                Map.of(
                        "otpTotp", generateTotp((String) map.get("secret")),
                        "method", AUTHENTICATOR_APP_MFA,
                        "password", "NewPassword@123",
                        "confirmPassword", "NewPassword@123"
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Password changed successfully"));
    }

    private String getAccessTokenForUserWhoseAuthenticatorAppMfaIsEnabled(Map<String, Object> map) throws ExecutionException, InterruptedException, InvalidKeyException {
        UserDto user = (UserDto) map.get("user");
        Response response = verifyMfaToLogin(
                AUTHENTICATOR_APP_MFA,
                getStateToken(
                        user.getUsername(),
                        user.getPassword()
                ),
                generateTotp((String) map.get("secret"))
        );
        response.then()
                .statusCode(200);
        return response.jsonPath()
                .getString("access_token");
    }

    @Test
    public void test_Verify_Change_Password_Failure_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        Map<String, String> map = new HashMap<>();
        for (String invalidOtp : INVALID_OTPS) {
            map.put("otpTotp", invalidOtp);
            map.put("method", AUTHENTICATOR_APP_MFA);
            verifyChangePassword(
                    accessToken,
                    map
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        map.put("otpTotp", "123456");
        for (String invalidPassword : INVALID_PASSWORDS) {
            map.put("password", invalidPassword);
            verifyChangePassword(
                    accessToken,
                    map
            ).then()
                    .statusCode(400)
                    .body("invalid_inputs", not(empty()));
        }
        map.put("password", "ValidPassword@123");
        map.put("confirmPassword", "DifferentPassword@123");
        verifyChangePassword(
                accessToken,
                map
        ).then()
                .statusCode(400)
                .body("invalid_inputs", not(empty()));
    }

    @Test
    public void test_Delete_Account_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        deleteAccount(getAccessToken(
                        user.getUsername(),
                        user.getPassword()
                ),
                user.getPassword()
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Account deleted successfully"));
    }

    @Test
    public void test_Delete_Account_Failure_Invalid_Password() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        for (String invalidPassword : INVALID_PASSWORDS) {
            deleteAccount(
                    accessToken,
                    invalidPassword
            ).then()
                    .statusCode(400)
                    .body("message", containsStringIgnoringCase("Invalid password"));
        }
    }

    @Test
    public void test_Verify_Delete_Account_Success() throws NotFoundException, IOException, ExecutionException, InvalidKeyException, InterruptedException {
        Map<String, Object> map = createTestUserAuthenticatorAppMfaEnabled();
        verifyDeleteAccount(
                getAccessTokenForUserWhoseAuthenticatorAppMfaIsEnabled(map),
                generateTotp((String) map.get("secret")),
                AUTHENTICATOR_APP_MFA
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Account deleted successfully"));
    }

    @Test
    public void test_Verify_Delete_Account_Failure_Invalid_Input() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        for (String invalidOtp : INVALID_OTPS) {
            verifyDeleteAccount(
                    accessToken,
                    invalidOtp,
                    AUTHENTICATOR_APP_MFA
            ).then()
                    .statusCode(400)
                    .body("message", containsStringIgnoringCase("Invalid Otp/Totp"));
        }
    }

    @Test
    public void test_Update_Details_Success() throws ExecutionException, InterruptedException {
        UserDto user = createTestUser();
        String accessToken = getAccessToken(
                user.getUsername(),
                user.getPassword()
        );
        Map<String, String> body = new HashMap<>();
        body.put("username", "Updated_" + user.getUsername());
        body.put("firstName", "Updated " + user.getFirstName());
        body.put("oldPassword", user.getPassword());
        updateDetails(
                accessToken,
                body
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("User details updated successfully"))
                .body("user.username", equalTo("Updated_" + user.getUsername()))
                .body("user.firstName", equalTo("Updated " + user.getFirstName()))
                .body("user.updatedBy", containsStringIgnoringCase("SELF"));
    }
}
