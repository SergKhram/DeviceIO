package io.github.sergkhram.managers;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;

import java.util.List;
import java.util.Map;

public interface Manager {
    void connectToHost(String host, Integer port);
    void disconnectHost(String host, Integer port);
    List<Device> getListOfDevices(Host host);
    Map<String, String> getDevicesStates();
    void rebootDevice(Device device);
}
