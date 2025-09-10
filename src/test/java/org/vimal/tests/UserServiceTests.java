package org.vimal.tests;

import jakarta.mail.MessagingException;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.vimal.BaseTest;
import org.vimal.dtos.UserDto;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.vimal.api.AuthenticationCalls.getAccessToken;
import static org.vimal.api.UserCalls.*;
import static org.vimal.constants.Common.EMAIL_MFA;
import static org.vimal.helpers.DtosHelper.createRandomUserDto;
import static org.vimal.utils.MailReaderUtility.getOtp;

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
    public void test_Forgot_Password_Method_Selection_Success(ITestContext context) throws ExecutionException, InterruptedException {
        UserDto user = createTestUserRandomValidEmail();
        context.setAttribute("user_from_test_Forgot_Password_Method_Selection_Success", user);
        forgotPasswordMethodSelection(
                user.getUsername(),
                EMAIL_MFA
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Otp sent"));
    }

    @Test(dependsOnMethods = {"test_Forgot_Password_Method_Selection_Success"})
    public void test_Reset_Password_Success(ITestContext context) throws ExecutionException, InterruptedException, MessagingException, IOException {
        UserDto user = (UserDto) context.getAttribute("user_from_test_Forgot_Password_Method_Selection_Success");
        resetPassword(Map.of(
                        "usernameOrEmail", user.getUsername(),
                        "otpTotp", getOtp(
                                user.getEmail(),
                                TEST_EMAIL_PASSWORD,
                                "Otp for resetting password"
                        ),
                        "method", EMAIL_MFA,
                        "password", "NewPassword@123",
                        "confirmPassword", "NewPassword@123"
                )
        ).then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("Password reset successful"));
    }
}
