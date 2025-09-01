package org.vimal.helpers;


import io.restassured.response.Response;

import java.util.Map;

import static org.vimal.constants.SubPaths.AUTH;
import static org.vimal.enums.RequestMethods.POST;
import static org.vimal.utils.ApiRequestUtility.executeRequest;

public final class AuthCallsHelper {
    private AuthCallsHelper() {
    }

    public static Response login(String usernameOrEmail, String password) {
        return executeRequest(POST, AUTH + "/login", null, Map.of("usernameOrEmail", usernameOrEmail, "password", password));
    }

    public static String getAccessToken(String usernameOrEmail, String password) {
        Response response = login(usernameOrEmail, password);
        response.then().statusCode(200);
        return response.jsonPath().getString("access_token");
    }
}
