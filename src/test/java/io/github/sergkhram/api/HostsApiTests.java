package io.github.sergkhram.api;

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

import static io.github.sergkhram.Generator.generateHosts;
import static io.github.sergkhram.utils.CustomAssertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        setDevices(hosts, List.of());
        response = HostsRequests.getHosts(getBaseUrl());

        assertWithAllure(hosts.size(), response.jsonPath().getList("", Host.class).size());
        assertContainsAllWithAllure(hosts, response.jsonPath().getList("", Host.class));
    }

    @Test
    @DisplayName("Check get host info by id api request")
    public void checkHostInfoTest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        setDevices(host, List.of());
        Response response = HostsRequests.getHostById(getBaseUrl(), host.getId().toString());
        assertWithAllure(
            host,
            response.as(Host.class)
        );
    }

    @SneakyThrows
    private void setDevices(List<Host> hosts, List<Device> devices) {
        hosts.parallelStream().forEach(
            it -> {
                try {
                    getSetDevicesMethod().invoke(it, devices);
                } catch (IllegalAccessException | InvocationTargetException ignored) {}
            }
        );
    }

    @SneakyThrows
    private void setDevices(Host host, List<Device> devices) {
        getSetDevicesMethod().invoke(host, devices);
    }

    @SneakyThrows
    private Method getSetDevicesMethod() {
        Method setDevicesMethod = Host.class.getDeclaredMethod("setDevices", List.class);
        setDevicesMethod.setAccessible(true);
        return setDevicesMethod;
    }
}
