package io.github.sergkhram.managers.idb;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sergkhram.data.entity.*;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.managers.Manager;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.sergkhram.data.converters.Converters.convertStringToJsonNode;
import static io.github.sergkhram.utils.Const.LOCAL_HOST;

@Service
public class IdbManager implements Manager {
    private final File directory = new File(System.getProperty("user.home"));
    private final String idbCmdPrefix = "idb";
    private final List<String> devicesListCmd = List.of(idbCmdPrefix, "list-targets", "--json");
    private final List<String> bootDeviceCmd = List.of(idbCmdPrefix, "boot");
    private final List<String> remoteHostConnectCmd = List.of(idbCmdPrefix, "connect");
    private final List<String> remoteHostDisconnectCmd = List.of(idbCmdPrefix, "disconnect");
    private final List<String> deviceInfoCmd = List.of(idbCmdPrefix, "describe", "--json", "--udid");
    private final List<String> listOfFilesCmd = List.of(idbCmdPrefix, "file", "ls", "--udid");

    @SneakyThrows
    @Override
    public List<Device> getListOfDevices(Host host) {
        List<IOSDevice> iosDeviceList = new ArrayList();
        ProcessBuilder pB = new ProcessBuilder(devicesListCmd);
        pB.directory(directory);
        Process p = null;
        try {
            p = pB.start();
            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

            String outputCommandLine;

            while ((outputCommandLine = readOutput.readLine()) != null) {
                iosDeviceList.add(
                    parseToIOSDevice(outputCommandLine)
                );
            }
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
        if (!host.getAddress().equals(LOCAL_HOST)) {
            return convert(
                iosDeviceList
                    .parallelStream()
                    .filter(
                        it -> getCompanionInfoBySerial(it)
                            .getAddress()
                            .contains(
                                host.getAddress()
                            )
                    )
                    .collect(
                        Collectors.toList()
                    ),
                host
            );
        } else {
            return convert(
                iosDeviceList
                    .parallelStream()
                    .filter(
                        it -> getCompanionInfoBySerial(it)
                            .getIsLocal()
                    )
                    .collect(
                        Collectors.toList()
                    ),
                host
            );
        }
    }

    @Override
    public void rebootDevice(Device device) {
        List<String> cmd = new ArrayList<>(bootDeviceCmd);
        cmd.add(device.getSerial());
        ProcessBuilder pB = new ProcessBuilder(cmd);
        pB.directory(directory);
        Process p = null;
        try {
            p = pB.start();
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
    }

    private List<Device> convert(List<IOSDevice> iosDevices, Host host) {
        return iosDevices.stream().map(
            it -> {
                Device device = new Device();
                device.setSerial(it.getSerial());
                device.setState(it.getState());
                device.setIsActive(!it.getState().equals("Shutdown"));
                device.setHost(host);
                device.setDeviceType(DeviceType.IOS);
                device.setName(it.getName());
                return device;
            }
        ).collect(Collectors.toList());
    }

    private IOSDevice parseToIOSDevice(String output) {
        JsonNode json = convertStringToJsonNode(output);
        IOSDevice device = new IOSDevice();
        device.setName(json.get("name").asText());
        device.setSerial(json.get("udid").asText());
        device.setState(json.get("state").asText());
        device.setType(json.get("type").asText());
        device.setIosVersion(json.get("os_version").asText());
        device.setArchitecture(json.get("architecture").asText());
        return device;
    }

    @Override
    public void connectToHost(String host, Integer port) {
        if(!host.equals(LOCAL_HOST)) {
            List<String> cmd = new ArrayList<>(remoteHostConnectCmd);
            cmd.addAll(List.of(host, port.toString()));
            ProcessBuilder pB = new ProcessBuilder(cmd);
            pB.directory(directory);
            Process p = null;
            try {
                p = pB.start();
                int exitCode = p.waitFor();
                System.out.println("\nExited with error code : " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (p != null) p.destroy();
            }
        }
    }

    @Override
    public void disconnectHost(String host, Integer port) {
        if(!host.equals(LOCAL_HOST)) {
            List<String> cmd = new ArrayList<>(remoteHostDisconnectCmd);
            cmd.addAll(List.of(host, port.toString()));
            ProcessBuilder pB = new ProcessBuilder(cmd);
            pB.directory(directory);
            Process p = null;
            try {
                p = pB.start();
                int exitCode = p.waitFor();
                System.out.println("\nExited with error code : " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (p != null) p.destroy();
            }
        }
    }

    public IOSDevice getDeviceInfo(Device device) {
        List<String> cmd = new ArrayList<>(deviceInfoCmd);
        cmd.add(device.getSerial());
        ProcessBuilder pB = new ProcessBuilder(cmd);
        pB.directory(directory);
        Process p = null;
        IOSDevice iosDevice = null;
        try {
            p = pB.start();
            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

            String outputCommandLine;

            while ((outputCommandLine = readOutput.readLine()) != null) {
                iosDevice = parseToIOSDevice(outputCommandLine);
            }
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
        return iosDevice;
    }

    public IOSCompanionInfo getCompanionInfoBySerial(IOSDevice device) {
        List<String> cmd = new ArrayList<>(deviceInfoCmd);
        cmd.add(device.getSerial());
        ProcessBuilder pB = new ProcessBuilder(cmd);
        pB.directory(directory);
        Process p = null;
        IOSCompanionInfo iosCompanionInfo = null;
        try {
            p = pB.start();
            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

            String outputCommandLine;

            while ((outputCommandLine = readOutput.readLine()) != null) {
                iosCompanionInfo = parseToCompanionInfo(outputCommandLine);
            }
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
        return iosCompanionInfo;
    }

    private IOSCompanionInfo parseToCompanionInfo(String output) {
        JsonNode json = convertStringToJsonNode(output);
        IOSCompanionInfo iosCompanionInfo = new IOSCompanionInfo();
        iosCompanionInfo.setIsLocal(json.get("companion_info").get("is_local").asBoolean());
        iosCompanionInfo.setAddress(
            json.get("companion_info").get("is_local").asBoolean()
                ? json.get("companion_info").get("address").get("path").asText()
                : json.get("companion_info").get("address").get("host").asText()
        );
        return iosCompanionInfo;
    }

    @Override
    public Map<String, String> getDevicesStates() {
        Map<String, String> devicesMap = new HashMap<>();
        ProcessBuilder pB = new ProcessBuilder(devicesListCmd);
        pB.directory(directory);
        Process p = null;
        try {
            p = pB.start();
            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

            String outputCommandLine;

            while ((outputCommandLine = readOutput.readLine()) != null) {
                IOSDevice device = parseToIOSDevice(outputCommandLine);
                devicesMap.put(device.getSerial(), device.getState());
            }
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
        return devicesMap;
    }

    public List<DeviceDirectoryElement> getListFiles(Device device, String path, IOSPackageType iosPackageType) {
        List<DeviceDirectoryElement> list = new ArrayList<>();
        List<String> cmd = new ArrayList<>(listOfFilesCmd);
        cmd.addAll(
            List.of(
                device.getSerial(),
                iosPackageType!=null
                    ? iosPackageType.value
                    : IOSPackageType.APPLICATION.value,
                path
            )
        );
        ProcessBuilder pB = new ProcessBuilder(cmd);
        pB.directory(directory);
        Process p = null;
        try {
            p = pB.start();
            BufferedReader readOutput =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

            String outputCommandLine;

            while ((outputCommandLine = readOutput.readLine()) != null) {
                DeviceDirectoryElement element = new DeviceDirectoryElement();
                element.name = outputCommandLine;
                element.path = path;
                list.add(element);
            }
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
        return list;
    }
}
