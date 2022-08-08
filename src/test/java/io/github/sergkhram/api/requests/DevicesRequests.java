package io.github.sergkhram.api.requests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;

@Slf4j
public class DevicesRequests {
    protected static RequestSpecification specification = new RequestSpecBuilder()
        .build()
        .contentType(JSON)
        .filter(new MyAllureRestAssured(log))
        .relaxedHTTPSValidation();

    public static Response getDeviceById(String baseUrl, String id) {
        return getDeviceById(baseUrl, id, 200);
    }

    public static Response getDeviceById(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .get(baseUrl + "/device/" + id)
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response getDevices(String baseUrl, Map<String, Object> queryParams) {
        return getDevices(baseUrl, queryParams, 200);
    }

    public static Response getDevices(String baseUrl, Map<String, Object> queryParams, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .queryParams(queryParams)
            .when()
            .get(baseUrl + "/devices")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response postDevice(String baseUrl, String body) {
        return postDevice(baseUrl, body, 200);
    }

    public static Response postDevice(String baseUrl, String body, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .body(body)
            .when()
            .post(baseUrl + "/device")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response updateDevice(String baseUrl, String id, String body) {
        return updateDevice(baseUrl, id, body, 200);
    }

    public static Response updateDevice(String baseUrl, String id, String body, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .body(body)
            .when()
            .put(baseUrl + "/device/" + id)
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response deleteDevice(String baseUrl, String id) {
        return deleteDevice(baseUrl, id, 202);
    }

    public static Response deleteDevice(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .delete(baseUrl + "/device/" + id)
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response postDevices(String baseUrl, String body) {
        return postDevices(baseUrl, body, 200);
    }

    public static Response postDevices(String baseUrl, String body, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .body(body)
            .when()
            .post(baseUrl + "/devices")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response postDeviceReboot(String baseUrl, String id) {
        return postDeviceReboot(baseUrl, id, 200);
    }

    public static Response postDeviceReboot(String baseUrl, String id, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .post(baseUrl + "/device/" + id + "/reboot")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response getDevicesStates(String baseUrl) {
        return getDevicesStates(baseUrl, 200);
    }

    public static Response getDevicesStates(String baseUrl, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .when()
            .get(baseUrl + "/devices/states")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response executeShell(String baseUrl, String id, String body) {
        return executeShell(baseUrl, id, body, 200);
    }

    public static Response executeShell(String baseUrl, String id, String body, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .body(body)
            .when()
            .post(baseUrl + "/device/" + id + "/executeShell")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response getFilesList(String baseUrl, String id, Map<String, String> params) {
        return getFilesList(baseUrl, id, params, 200);
    }

    public static Response getFilesList(String baseUrl, String id) {
        return getFilesList(baseUrl, id, Map.of(), 200);
    }

    public static Response getFilesList(String baseUrl, String id, Map<String, String> params, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .queryParams(params)
            .when()
            .get(baseUrl + "/device/" + id + "/files")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }

    public static Response postDownloadFile(String baseUrl, String id, String body, Map<String, String> params) {
        return postDownloadFile(baseUrl, id, body, params, 200);
    }

    public static Response postDownloadFile(String baseUrl, String id, String body) {
        return postDownloadFile(baseUrl, id, body, Map.of(), 200);
    }

    public static Response postDownloadFile(String baseUrl, String id, String body, Map<String, String> params, int code) {
        return RestAssured
            .given()
            .spec(specification)
            .queryParams(params)
            .body(body)
            .when()
            .post(baseUrl + "/device/" + id + "/files/download")
            .then()
            .statusCode(code)
            .extract()
            .response();
    }
}
