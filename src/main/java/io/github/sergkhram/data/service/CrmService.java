package io.github.sergkhram.data.service;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.entity.Settings;
import io.github.sergkhram.data.repository.DeviceRepository;
import io.github.sergkhram.data.repository.HostRepository;
import io.github.sergkhram.data.repository.SettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CrmService {
    private final HostRepository hostRepository;
    private final DeviceRepository deviceRepository;
    private final SettingsRepository settingsRepository;

    public CrmService(HostRepository hostRepository,
                      DeviceRepository deviceRepository,
                      SettingsRepository settingsRepository) {
        this.hostRepository = hostRepository;
        this.deviceRepository = deviceRepository;
        this.settingsRepository = settingsRepository;
    }

    public List<Host> findAllHosts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return hostRepository.findAll();
        } else {
            return hostRepository.search(stringFilter);
        }
    }

    public List<Host> findAllActiveHosts(String stringFilter) {
        return hostRepository.search(stringFilter, true);
    }

    public long countHosts() {
        return hostRepository.count();
    }

    public void deleteHost(Host host) {
        deviceRepository.search(
            "",
            host.getId().toString()
        ).forEach(
            deviceRepository::delete
        );
        hostRepository.delete(host);
    }

    public void deleteDevice(Device device) {
        deviceRepository.delete(device);
    }

    public void saveHost(Host host) {
        if (host == null) {
            log.info("Host is null. Are you sure you have connected your form to the application?");
            return;
        }
        hostRepository.save(host);
    }

    public List<Device> findAllDevices(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAll();
        } else {
            return deviceRepository.search(stringFilter);
        }
    }

    public List<Device> findAllDevices(String stringFilter, UUID id) {
        return deviceRepository.search(stringFilter, id.toString());
    }

    public List<Device> findAllDevices(String stringFilter, UUID id, Boolean isActiveHost) {
        return deviceRepository.search(stringFilter, id.toString(), isActiveHost);
    }

    public void saveSettings(Settings settings) {
        settingsRepository.save(settings);
    }

    public Settings getCurrentSettings() {
        return settingsRepository.count() > 0 ? settingsRepository.findAll().get(0) : null;
    }

    public void saveDevices(List<Device> devices) {
        deviceRepository.saveAll(devices);
    }

    public void saveDevice(Device device) {
        deviceRepository.save(device);
    }

    public Device getDeviceById(String id) {
        return id!=null
            ? deviceRepository.findById(UUID.fromString(id)).get()
            : null;
    }

    public Host getHostById(String id) {
        return id!=null
            ? hostRepository.findById(UUID.fromString(id)).get()
            : null;
    }
}