package io.github.sergkhram.api;

import io.github.sergkhram.api.requests.HostsRequests;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.github.sergkhram.data.entity.Host;

import static io.github.sergkhram.Generator.generateHosts;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class HostsApiTests extends ApiTestsBase {

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
}
