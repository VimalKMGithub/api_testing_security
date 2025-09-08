package org.vimal.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.vimal.enums.RequestMethods;

import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;

public final class ApiCalls {
    private ApiCalls() {
    }

    public static Response executeRequest(RequestMethods method,
                                          String endpoint,
                                          Map<String, String> headers) {
        return executeRequest(
                method,
                endpoint,
                headers,
                null
        );
    }

    public static Response executeRequest(RequestMethods method,
                                          String endpoint,
                                          Map<String, String> headers,
                                          Map<String, String> params) {
        return executeRequest(
                method,
                endpoint,
                headers,
                params,
                null
        );
    }

    public static Response executeRequest(RequestMethods method,
                                          String endpoint,
                                          Map<String, String> headers,
                                          Map<String, String> params,
                                          Map<String, String> pathParams) {
        return executeRequest(
                method,
                endpoint,
                headers,
                params,
                pathParams,
                null
        );
    }

    public static Response executeRequest(RequestMethods method,
                                          String endpoint,
                                          Map<String, String> headers,
                                          Map<String, String> params,
                                          Map<String, String> pathParams,
                                          Object body) {
        RequestSpecification spec = given();
        if (headers != null) {
            headers.forEach(spec::header);
        }
        if (params != null) {
            params.forEach(spec::queryParam);
        }
        if (pathParams != null) {
            spec.pathParams(pathParams);
        }
        if (body != null) {
            processBody(spec, body);
        }
        return executeMethod(method, spec, endpoint);
    }

    private static void processBody(RequestSpecification spec,
                                    Object body) {
        if (body instanceof File) {
            spec.multiPart((File) body);
        } else {
            spec.contentType(ContentType.JSON)
                    .body(body);
        }
    }

    private static Response executeMethod(RequestMethods method,
                                          RequestSpecification spec,
                                          String endpoint) {
        return switch (method) {
            case GET -> spec.get(endpoint);
            case POST -> spec.post(endpoint);
            case PUT -> spec.put(endpoint);
            case DELETE -> spec.delete(endpoint);
        };
    }
}
