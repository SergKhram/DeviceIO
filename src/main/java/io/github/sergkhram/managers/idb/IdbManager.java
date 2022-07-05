package io.github.sergkhram.managers.idb;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sergkhram.data.entity.*;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.executors.CommandExecutor;
import io.github.sergkhram.managers.Manager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static io.github.sergkhram.data.converters.Converters.convertStringToJsonNode;
import static io.github.sergkhram.utils.Const.LOCAL_HOST;

@Service
@Slf4j
public class IdbManager implements Manager {
    private final String idbCmdPrefix = "idb";
    private final List<String> devicesListCmd = List.of(idbCmdPrefix, "list-targets", "--json");
    private final List<String> bootDeviceCmd = List.of(idbCmdPrefix, "boot");
    private final List<String> remoteHostConnectCmd = List.of(idbCmdPrefix, "connect");
    private final List<String> remoteHostDisconnectCmd = List.of(idbCmdPrefix, "disconnect");
    private final List<String> deviceInfoCmd = List.of(idbCmdPrefix, "describe", "--json", "--udid");
    private final List<String> listOfFilesCmd = List.of(idbCmdPrefix, "file", "ls", "--udid");
    private final List<String> pullFileCmd = List.of(idbCmdPrefix, "file", "pull", "--udid");

    @SneakyThrows
    @Override
    public List<Device> getListOfDevices(Host host) {
        UUID processUuid = UUID.randomUUID();
        List<IOSDevice> iosDeviceList = new ArrayList();
        log.info(String.format("[%s] Get list of devices process started", processUuid));

        CommandExecutor cmdExecutor = new CommandExecutor(devicesListCmd);
        cmdExecutor.execute(
            (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, devicesListCmd)),
            (outputCmdLine) -> {
                log.debug(String.format("[%s] Received device info:" + outputCmdLine, processUuid));
                iosDeviceList.add(
                    parseToIOSDevice(outputCmdLine)
                );
            },
            (errorCmdLine) -> {},
            (code) -> log.info(
                String.format("[%s] Get list of devices process finished with exit code: " + code, processUuid)
            )
        );
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
        UUID processUuid = UUID.randomUUID();
        List<String> cmd = new ArrayList<>(bootDeviceCmd);
        cmd.add(device.getSerial());
        log.info(
            String.format("[%s] Reboot/boot device %s process started", processUuid, device.getSerial())
        );

        CommandExecutor cmdExecutor = new CommandExecutor(cmd);
        cmdExecutor.execute(
            (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, cmd)),
            (code) -> log.info(
                String.format(
                    "[%s] Reboot/boot device %s process finished with exit code " + code,
                    processUuid,
                    device.getSerial()
                )
            )
        );
    }

    private List<Device> convert(List<IOSDevice> iosDevices, Host host) {
        return iosDevices.parallelStream().map(
            it -> {
                Device device = new Device();
                device.setSerial(it.getSerial());
                device.setState(it.getState());
                device.setIsActive(!it.getState().equals("Shutdown"));
                device.setHost(host);
                device.setOsType(OsType.IOS);
                device.setName(it.getName());
                device.setDeviceType(it.getType());
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
        device.setType(DeviceType.valueOf(json.get("type").asText().toUpperCase()));
        device.setIosVersion(json.get("os_version").asText());
        device.setArchitecture(json.get("architecture").asText());
        return device;
    }

    private IOSDevice parseDescribeToIOSDevice(String output) {
        JsonNode json = convertStringToJsonNode(output);
        IOSDevice device = new IOSDevice();
        device.setName(json.get("name").asText());
        device.setSerial(json.get("udid").asText());
        device.setState(json.get("state").asText());
        device.setType(DeviceType.valueOf(json.get("target_type").asText().toUpperCase()));
        device.setIosVersion(json.get("os_version").asText());
        device.setArchitecture(json.get("architecture").asText());
        return device;
    }

    @Override
    public void connectToHost(String host, Integer port) {
        if(!host.equals(LOCAL_HOST)) {
            UUID processUuid = UUID.randomUUID();
            List<String> cmd = new ArrayList<>(remoteHostConnectCmd);
            cmd.addAll(List.of(host, port.toString()));
            log.info(
                String.format(
                    "[%s] Connecting to host %s process started", processUuid, host + port
                )
            );
            CommandExecutor cmdExecutor = new CommandExecutor(cmd);
            cmdExecutor.execute(
                (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, cmd)),
                (code) -> log.info(
                    String.format(
                        "[%s] Connecting to host %s process finished with exit code " + code, processUuid
                    )
                )
            );
        }
    }

    @Override
    public void disconnectHost(String host, Integer port) {
        if(!host.equals(LOCAL_HOST)) {
            UUID processUuid = UUID.randomUUID();
            List<String> cmd = new ArrayList<>(remoteHostDisconnectCmd);
            cmd.addAll(List.of(host, port.toString()));
            log.info(
                String.format(
                    "[%s] Disconnecting host %s process started", processUuid, host + port
                )
            );
            CommandExecutor cmdExecutor = new CommandExecutor(cmd);
            cmdExecutor.execute(
                (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, cmd)),
                (code) -> log.info(
                    String.format(
                        "[%s] Disconnecting host %s process finished with exit code " + code, processUuid
                    )
                )
            );
        }
    }

    public IOSDevice getDeviceInfo(Device device) {
        List<String> cmd = new ArrayList<>(deviceInfoCmd);
        cmd.add(device.getSerial());
        AtomicReference<IOSDevice> iosDevice = new AtomicReference<>(new IOSDevice());

        CommandExecutor cmdExecutor = new CommandExecutor(cmd);
        cmdExecutor.execute(
            (code) -> log.info(String.format("Executing '%s'", cmd)),
            (outputCmdLine) -> {
                iosDevice.set(parseDescribeToIOSDevice(outputCmdLine));
            },
            (errorCmdLine) -> {},
            (code) -> log.info("Get device info process finished with exit code " + code)
        );
        return iosDevice.get();
    }

    public IOSCompanionInfo getCompanionInfoBySerial(IOSDevice device) {
        List<String> cmd = new ArrayList<>(deviceInfoCmd);
        cmd.add(device.getSerial());
        log.info(
            String.format(
                "Receiving %s device companion info started", device.getSerial()
            )
        );
        AtomicReference<IOSCompanionInfo> iosCompanionInfo = new AtomicReference<>(new IOSCompanionInfo());

        CommandExecutor cmdExecutor = new CommandExecutor(cmd);
        cmdExecutor.execute(
            (code) -> log.info(String.format("Executing '%s'", cmd)),
            (outputCmdLine) -> {
                iosCompanionInfo.set(parseToCompanionInfo(outputCmdLine));
            },
            (errorCmdLine) -> {},
            (code) -> log.info(
                String.format("Receiving %s device companion info finished with exit code " + code, device.getSerial())
            )
        );
        return iosCompanionInfo.get();
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
        UUID processUuid = UUID.randomUUID();
        Map<String, String> devicesMap = new HashMap<>();
        CommandExecutor cmdExecutor = new CommandExecutor(devicesListCmd);
        cmdExecutor.execute(
            (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, devicesListCmd)),
            (outputCmdLine) -> {
                IOSDevice device = parseToIOSDevice(outputCmdLine);
                devicesMap.put(device.getSerial(), device.getState());
            },
            (errorCmdLine) -> {},
            (code) -> log.info(
                String.format("[%s] Get devices states process finished with exit code " + code, processUuid)
            )
        );
        return devicesMap;
    }

    public List<DeviceDirectoryElement> getListFiles(Device device, String path, IOSPackageType iosPackageType) {
        UUID processUuid = UUID.randomUUID();
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
        CommandExecutor cmdExecutor = new CommandExecutor(cmd);
        cmdExecutor.execute(
            (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, cmd)),
            (outputCmdLine) -> {
                DeviceDirectoryElement element = new DeviceDirectoryElement();
                element.name = outputCmdLine;
                element.path = path;
                list.add(element);
            },
            (errorCmdLine) -> {},
            (code) -> log.info(
                String.format(
                    "[%s] Get list files process for '%s' path and '%s' package finished with exit code %s",
                    processUuid,
                    path,
                    iosPackageType.name(),
                    code
                )
            )
        );
        return list;
    }

    public File download(
        Device device,
        DeviceDirectoryElement deviceDirectoryElement,
        IOSPackageType iosPackageType,
        String destination,
        UnaryOperator<File> action
    ) {
        UUID processUuid = UUID.randomUUID();
        File file = new File(destination + File.separator + deviceDirectoryElement.name);
        List<String> cmd = new ArrayList<>(pullFileCmd);
        String parentPath = deviceDirectoryElement.path.equals("") ? "" : deviceDirectoryElement.path + "/";
        cmd.addAll(
            List.of(
                device.getSerial(),
                iosPackageType!=null
                    ? iosPackageType.value
                    : IOSPackageType.APPLICATION.value,
                parentPath + deviceDirectoryElement.name,
                file.getAbsolutePath()
            )
        );
        log.info(
            String.format(
                "[%s] Pull file/folder %s process started", processUuid, file.getName()
            )
        );
        StringBuilder errorText = new StringBuilder();
        CommandExecutor cmdExecutor = new CommandExecutor(cmd);
        File finalFile = file;
        cmdExecutor.execute(
            (code) -> log.info(String.format("[%s] Executing '%s'", processUuid, cmd)),
            (outputCmdLine) -> {},
            errorText::append,
            (code) -> {
                log.info(
                    String.format(
                        "[%s] Process finished with exit code %s and message '%s'", processUuid, code, errorText
                    )
                );
                log.info(
                    String.format(
                        "[%s] Pull file/folder %s process finished", processUuid, finalFile.getName()
                    )
                );
            }
        );
        file = action.apply(file);
        return file;
    }

    public File downloadFile(Device device,
                   DeviceDirectoryElement deviceDirectoryElement,
                   IOSPackageType iosPackageType,
                   String destination) {
        return download(
            device,
            deviceDirectoryElement,
            iosPackageType,
            destination,
            (file) -> device.getDeviceType().equals(DeviceType.SIMULATOR)
                ? new File(file, deviceDirectoryElement.name)
                : file
        );
    }

    public File downloadFolder(Device device,
                               DeviceDirectoryElement deviceDirectoryElement,
                               IOSPackageType iosPackageType,
                               String destination) {
        return device.getDeviceType().equals(DeviceType.SIMULATOR)
            ? download(
                device,
                deviceDirectoryElement,
                iosPackageType,
                destination,
                (file) -> file
            )
            : recursivelyDownload(
                device,
                deviceDirectoryElement,
                iosPackageType,
                destination
        );
    }

    public File recursivelyDownload(
        Device device,
        DeviceDirectoryElement deviceDirectoryElement,
        IOSPackageType iosPackageType,
        String destination
    ) {
        AtomicReference<File> createdFile = new AtomicReference<>();
        var listOfFiles = getListFiles(
            device,
            deviceDirectoryElement.path + "/" + deviceDirectoryElement.name,
            iosPackageType
        );
        if(
            listOfFiles.size()>0
        ) {
            listOfFiles.parallelStream().forEach(
                it -> {
                    String path = destination + File.separator + deviceDirectoryElement.name;
                    File file = new File(path);
                    file.mkdir();
                    createdFile.set(file);
                    recursivelyDownload(
                        device,
                        new DeviceDirectoryElement(it.name, it.path),
                        iosPackageType,
                        path
                    );
                }
            );
        } else {
            createdFile.set(
                download(
                    device,
                    deviceDirectoryElement,
                    iosPackageType,
                    destination,
                    (file) -> device.getDeviceType().equals(DeviceType.SIMULATOR)
                        ? new File(file, deviceDirectoryElement.name)
                        : file
                )
            );
        }
        return createdFile.get();
    }
}
