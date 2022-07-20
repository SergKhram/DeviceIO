package io.github.sergkhram.api.requests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostsRequests {

    public static Response getHosts(String baseUrl) {
        return RestAssured
            .given()
            .when()
            .filter(new MyAllureRestAssured(log))
            .get(baseUrl + "/hosts")
            .then()
            .statusCode(200)
            .extract()
            .response();
    }

    public static Response getHostById(String baseUrl, String id) {
        return RestAssured
            .given()
            .when()
            .filter(new MyAllureRestAssured(log))
            .get(baseUrl + "/host/" + id)
            .then()
            .statusCode(200)
            .extract()
            .response();
    }
}
