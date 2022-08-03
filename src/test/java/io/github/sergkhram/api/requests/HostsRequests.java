package io.github.sergkhram.api.requests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;

@Slf4j
public class HostsRequests {

    protected static RequestSpecification specification = new RequestSpecBuilder()
        .build()
        .contentType(JSON)
        .filter(new MyAllureRestAssured(log))
        .relaxedHTTPSValidation();

    public static Response getHosts(String baseUrl) {
        return getHosts(baseUrl, 200);
    }

    public static Response getHosts(String baseUrl, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .get(baseUrl + "/hosts")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response getHostById(String baseUrl, String id) {
        return getHostById(baseUrl, id, 200);
    }

    public static Response getHostById(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .get(baseUrl + "/host/" + id)
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response postHost(String baseUrl, String body) {
        return postHost(baseUrl, body, 200);
    }

    public static Response postHost(String baseUrl, String body, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .body(body)
            .when()
            .post(baseUrl + "/host")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response updateHost(String baseUrl, String id, String body) {
        return updateHost(baseUrl, id, body, 200);
    }

    public static Response updateHost(String baseUrl, String id, String body, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .body(body)
            .when()
            .put(baseUrl + "/host/" + id)
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response deleteHost(String baseUrl, String id) {
        return deleteHost(baseUrl, id, 202);
    }

    public static Response deleteHost(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .delete(baseUrl + "/host/" + id)
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response connectHost(String baseUrl, String id) {
        return connectHost(baseUrl, id, 200);
    }

    public static Response connectHost(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .post(baseUrl + "/host/" + id + "/connect")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response disconnectHost(String baseUrl, String id) {
        return disconnectHost(baseUrl, id, 200);
    }

    public static Response disconnectHost(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .post(baseUrl + "/host/" + id + "/disconnect")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response updateHostState(String baseUrl, String id, Map<String, Object> queryParams) {
        return updateHostState(baseUrl, id, queryParams, 200);
    }

    public static Response updateHostState(String baseUrl, String id, Map<String, Object> queryParams, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .queryParams(queryParams)
            .when()
            .get(baseUrl + "/host/" + id + "/updateState")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }
}
