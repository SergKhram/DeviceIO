package io.github.sergkhram.managers;

import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface Manager {
    void connectToHost(String host, Integer port);
    void disconnectHost(String host, Integer port);
    List<Device> getListOfDevices(Host host);
    Map<String, String> getDevicesStates();
    void rebootDevice(Device device);
    File makeScreenshot(Device device, String filePath);
    List<AppDescription> getAppsList(Device device);
}
