package io.github.sergkhram.api;

import io.github.sergkhram.data.repository.HostRepository;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import io.github.sergkhram.data.entity.Host;

import static io.github.sergkhram.Generator.generateHosts;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
        "grpc.server.port=-1"
    }
)
public class HostsApiTests {
    @Autowired
    HostRepository hostRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void checkHostsListTest() {
        Response response = RestAssured
            .given()
            .when()
            .get(getBaseUrl() + "/hosts")
            .then()
            .statusCode(200)
            .extract()
            .response();
        Assertions.assertTrue(response.jsonPath().getList("", Host.class).isEmpty());
        List<Host> hosts = generateHosts(100);
        hostRepository.saveAll(hosts);
        hosts = hostRepository.findAll();
        response = RestAssured
            .given()
            .when()
            .get(getBaseUrl() + "/hosts")
            .then()
            .statusCode(200)
            .extract()
            .response();

        Assertions.assertEquals(hosts.size(), response.jsonPath().getList("", Host.class).size());
        assertThat(response.jsonPath().getList("", Host.class))
            .containsAll(hosts);
    }

    private String getBaseUrl() {
        return restTemplate.getRootUri() + "/api";
    }
}
