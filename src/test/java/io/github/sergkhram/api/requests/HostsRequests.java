package io.github.sergkhram.api.requests;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class HostsRequests {

    public static Response getHosts(String baseUrl) {
        return RestAssured
            .given()
            .when()
            .log().all(false)
            .get(baseUrl + "/hosts")
            .then()
            .log().all(false)
            .statusCode(200)
            .extract()
            .response();
    }

    public static Response getHostById(String baseUrl, String id) {
        return RestAssured
            .given()
            .when()
            .log().all(false)
            .get(baseUrl + "/host/" + id)
            .then()
            .log().all(false)
            .statusCode(200)
            .extract()
            .response();
    }
}
