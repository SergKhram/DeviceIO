package io.github.sergkhram.data.idb;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.sergkhram.data.entity.IOSDevice;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static io.github.sergkhram.data.converters.Converters.convertStringToJsonNode;

@Service
public class IdbManager {
    private final File directory = new File(System.getProperty("user.home"));
    private final String idbCmdPrefix = "idb";
    private final List<String> devicesListCmd = List.of(idbCmdPrefix, "list-targets", "--json");
    private final List<String> bootDeviceCmd = List.of(idbCmdPrefix, "boot");
    private final List<String> remoteHostCmd = List.of(idbCmdPrefix, "connect");

    @SneakyThrows
    public List<IOSDevice> getListOfDevices() {
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
                    convert(outputCommandLine)
                );
            }
            int exitCode = p.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(p != null) p.destroy();
        }
        return iosDeviceList;
    }

    public void rebootDevice(IOSDevice iosDevice) {
        List<String> cmd = new ArrayList<>(bootDeviceCmd);
        cmd.add(iosDevice.getSerial());
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

    private IOSDevice convert(String output) {
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

    public void connectToDevice(String host, Integer port) {
        List<String> cmd = new ArrayList<>(remoteHostCmd);
        cmd.addAll(List.of(host, port.toString()));
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
}
