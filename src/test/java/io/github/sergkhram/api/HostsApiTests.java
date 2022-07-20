package io.github.sergkhram.api;

import io.github.sergkhram.api.requests.HostsRequests;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.sergkhram.data.entity.Host;

import static io.github.sergkhram.Generator.generateHosts;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class HostsApiTests extends ApiTestsBase {

    @BeforeEach
    public void beforeTest() {
        hostRepository.deleteAll();
    }

    @Test
    public void checkHostsListTest() {
        Response response = HostsRequests.getHosts(getBaseUrl());

        Assertions.assertTrue(response.jsonPath().getList("", Host.class).isEmpty());

        List<Host> hosts = generateHosts(100);
        hostRepository.saveAll(hosts);
        hosts = hostRepository.findAll();
        response = HostsRequests.getHosts(getBaseUrl());

        Assertions.assertEquals(hosts.size(), response.jsonPath().getList("", Host.class).size());
        assertThat(response.jsonPath().getList("", Host.class))
            .containsAll(hosts);
    }

    @Test
    public void checkHostInfoTest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Response response = HostsRequests.getHostById(getBaseUrl(), host.getId().toString());
        Assertions.assertEquals(
            host,
            response.as(Host.class)
        );
    }
}