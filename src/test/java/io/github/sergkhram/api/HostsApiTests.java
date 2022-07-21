package io.github.sergkhram.api;

import io.github.sergkhram.api.requests.HostsRequests;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.github.sergkhram.data.entity.Host;

import static io.github.sergkhram.Generator.generateHosts;
import static io.github.sergkhram.utils.CustomAssertions.*;

import java.util.List;

@Epic("DeviceIO")
@Feature("API")
@Story("Hosts")
public class HostsApiTests extends ApiTestsBase {

    @BeforeEach
    public void beforeTest() {
        hostRepository.deleteAll();
    }

    @Test
    @DisplayName("Check get hosts api request")
    public void checkHostsListTest() {
        Response response = HostsRequests.getHosts(getBaseUrl());

        assertTrueWithAllure(response.jsonPath().getList("", Host.class).isEmpty());

        List<Host> hosts = generateHosts(100);
        hostRepository.saveAll(hosts);
        hosts = hostRepository.findAll();
        response = HostsRequests.getHosts(getBaseUrl());

        assertWithAllure(hosts.size(), response.jsonPath().getList("", Host.class).size());
        assertContainsAllWithAllure(response.jsonPath().getList("", Host.class), hosts);
    }

    @Test
    @DisplayName("Check get host info by id api request")
    public void checkHostInfoTest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Response response = HostsRequests.getHostById(getBaseUrl(), host.getId().toString());
        assertWithAllure(
            host,
            response.as(Host.class)
        );
    }
}
