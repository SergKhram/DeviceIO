package io.github.sergkhram;

import com.mifmif.common.regex.Generex;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import lombok.SneakyThrows;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class Generator {
    public static String generateRandomString(String pattern) {
        return new Generex(pattern).random();
    }

    public static String generateRandomString(Integer length) {
        return generateRandomString(String.format("[A-Z]{1}[a-z]{%s}", length));
    }

    public static String generateRandomString() {
        return generateRandomString(10);
    }

    public static Integer generateRandomInt() {
        return generateRandomInt(1, 20);
    }

    @SneakyThrows
    public static Integer generateRandomInt(Integer startInt, Integer endInt) {
        return SecureRandom.getInstance("SHA1PRNG").ints(startInt, endInt).findFirst().getAsInt();
    }

    public static CopyOnWriteArrayList<Host> generateHosts(int count) {
        CopyOnWriteArrayList<Host> hosts = new CopyOnWriteArrayList<>();
        IntStream.range(0, count).parallel().forEach(
            it -> {
                Host host = new Host();
                host.setName(generateRandomString());
                host.setAddress(generateRandomString());
                hosts.add(
                    host
                );
            }
        );
        return hosts;
    }

    public static CopyOnWriteArrayList<Device> generateDevices(Host host, int count, DeviceType deviceType, OsType osType) {
        CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
        IntStream.range(0, count).parallel().forEach(
            it -> {
                Device device = new Device();
                device.setName(generateRandomString());
                device.setDeviceType(deviceType);
                device.setOsType(osType);
                device.setIsActive(false);
                device.setState("Disconnected");
                device.setSerial(UUID.randomUUID().toString());
                device.setHost(host);
                device.setOsVersion(
                    device.getOsType().equals(OsType.IOS) ? "iOS 14" : "30"
                );
                devices.add(device);
            }
        );
        return devices;
    }

    public static CopyOnWriteArrayList<Device> generateDevices(Host host, int count, OsType osType) {
        return generateDevices(host, count, DeviceType.SIMULATOR, osType);
    }

    public static CopyOnWriteArrayList<Device> generateDevices(Host host, int count, DeviceType deviceType) {
        return generateDevices(host, count, deviceType, OsType.ANDROID);
    }

    public static CopyOnWriteArrayList<Device> generateDevices(Host host, int count) {
        return generateDevices(host, count, DeviceType.SIMULATOR, OsType.ANDROID);
    }
}
