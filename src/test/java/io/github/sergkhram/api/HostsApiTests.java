package io.github.sergkhram.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sergkhram.api.requests.HostsRequests;
import io.github.sergkhram.data.entity.Device;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.github.sergkhram.data.entity.Host;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static io.github.sergkhram.Generator.*;
import static io.github.sergkhram.utils.Const.LOCAL_HOST;
import static io.github.sergkhram.utils.CustomAssertions.*;
import static io.github.sergkhram.utils.json.JsonTestUtil.*;

import java.util.List;
import java.util.Map;

@Epic("DeviceIO")
@Feature("API")
@Story("Hosts")
public class HostsApiTests extends ApiTestsBase {

    @BeforeEach
    public void beforeTest() {
        hostRepository.deleteAll();
    }

    @Autowired
    private Environment env;

    @Test
    @DisplayName("Check get hosts api request")
    public void checkGetHostsRequest() {
        Response response = HostsRequests.getHosts(getBaseUrl());
        assertTrueWithAllure(response.jsonPath().getList("", Host.class).isEmpty());
        int hostsCount = 100;
        List<Host> hosts = generateHosts(hostsCount);
        hostRepository.saveAll(hosts);
        hosts = hostRepository.findAll();
        setDevices(hosts, List.of());
        response = HostsRequests.getHosts(getBaseUrl());
        List<Host> responseHosts = response.jsonPath().getList("", Host.class);
        assertWithAllure(hostsCount, responseHosts.size());
        assertContainsAllWithAllure(hosts, responseHosts);
    }

    @Test
    @DisplayName("Check get host info by id api request")
    public void checkGetHostRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        host.setDevices(List.of());
        Response response = HostsRequests.getHostById(getBaseUrl(), host.getId());
        assertWithAllure(
            host,
            response.as(Host.class)
        );
    }

    @Test
    @DisplayName("Check post host api request")
    public void checkCreateHostRequest() {
        Host host = generateHosts(1).get(0);
        Response response = HostsRequests.postHost(
            getBaseUrl(),
            convertModelToStringWONullValues(host)
        );
        String id = response.jsonPath().getString("id");
        JsonNode expectedHost = convertModelToJsonNode(host);
        ((ObjectNode)expectedHost).put("id", "regexp:(.*)");
        assertWithAllureWRegex(
            expectedHost,
            response.as(JsonNode.class)
        );
        ((ObjectNode)expectedHost).putArray("devices");
        response = HostsRequests.getHostById(getBaseUrl(), id);
        assertWithAllureWRegex(
            expectedHost,
            response.as(JsonNode.class)
        );
    }

    @Test
    @DisplayName("Check update host api request")
    public void checkUpdateHostRequest() {
        Host host = generateHosts(1).get(0);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId();
        host.setName(generateRandomString());
        host.setAddress(generateRandomString());
        host.setPort(generateRandomInt(0, 65535));

        Response response = HostsRequests.updateHost(
            getBaseUrl(),
            id,
            convertModelToStringWONullValues(host)
        );

        host.setId(id);
        JsonNode expectedHost = convertModelToJsonNode(host);
        assertWithAllureWRegex(
            expectedHost,
            response.as(JsonNode.class)
        );

        ((ObjectNode)expectedHost).putArray("devices");
        response = HostsRequests.getHostById(getBaseUrl(), id);
        assertWithAllureWRegex(
            expectedHost,
            response.as(JsonNode.class)
        );
    }

    @Test
    @DisplayName("Check delete host api request")
    public void checkDeleteHostRequest() {
        Host host = generateHosts(1).get(0);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId();
        HostsRequests.deleteHost(getBaseUrl(), id);
        Response response = HostsRequests.getHostById(getBaseUrl(), id, 400);
        assertWithAllure(
            "There is no host with id " + id,
            response.asString()
        );
    }

    @Test
    @DisplayName("Check connect host api request")
    public void checkConnectHostRequest() {
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress("localhost");
        host.setPort(65535);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId();
        Mockito.doNothing().when(this.idbManager).connectToHost("localhost", 65535);
        Mockito.doNothing().when(this.adbManager).connectToHost("localhost", 65535);
        HostsRequests.connectHost(getBaseUrl(), id);
    }

    @Test
    @DisplayName("Check disconnect host api request")
    public void checkDisconnectHostRequest() {
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress("localhost");
        host.setPort(65535);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId();
        Mockito.doNothing().when(this.idbManager).disconnectHost("localhost", 65535);
        Mockito.doNothing().when(this.adbManager).disconnectHost("localhost", 65535);
        HostsRequests.disconnectHost(getBaseUrl(), id);
    }

    @Test
    @DisplayName("Check update host state api request")
    public void checkUpdateHostStateRequest() {
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress(LOCAL_HOST);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId();
        HostsRequests.updateHostState(getBaseUrl(), id, Map.of("deleteDevices", false));
        Response response = HostsRequests.getHostById(getBaseUrl(), id);
        assertWithAllure(
            true,
            response.jsonPath().getBoolean("isActive")
        );
    }

    @SneakyThrows
    private void setDevices(List<Host> hosts, List<Device> devices) {
        hosts.parallelStream().forEach(
            it -> it.setDevices(devices)
        );
    }
}
