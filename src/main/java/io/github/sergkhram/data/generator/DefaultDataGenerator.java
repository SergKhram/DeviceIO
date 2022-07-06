package io.github.sergkhram.data.generator;

import io.github.sergkhram.data.repository.DeviceRepository;
import io.github.sergkhram.data.repository.HostRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
@Slf4j
public class DefaultDataGenerator {

    @Bean
    public CommandLineRunner loadData(HostRepository hostRepository, DeviceRepository deviceRepository) {

        return args -> {
            if (hostRepository.count() != 0L) {
                log.info("Using existing database");
                return;
            }

//            List<Host> hosts = List.of(
//                new Host("Localhost", "127.0.0.1", null),
//                new Host("Localhost2", "127.0.0.1", null),
//                new Host("NotActive", "196.1.1.8", null),
//                new Host("Active", "192.168.0.1", null)
//            );
//            hostRepository.saveAll(hosts);
//            Device defaultDevice = new Device();
//            defaultDevice.setSerial("default");
//            defaultDevice.setHost(hosts.get(0));
//            defaultDevice.setState("");
//            defaultDevice.setDeviceType(DeviceType.ANDROID);
//            Device defaultDevice2 = new Device();
//            defaultDevice2.setSerial("default2");
//            defaultDevice2.setHost(hosts.get(1));
//            defaultDevice2.setState("");
//            defaultDevice2.setDeviceType(DeviceType.ANDROID);
//            Device defaultDevice3 = new Device();
//            defaultDevice3.setSerial("default3");
//            defaultDevice3.setHost(hosts.get(2));
//            defaultDevice3.setState("");
//            defaultDevice3.setDeviceType(DeviceType.ANDROID);
//            List<Device> devices = List.of(
//                defaultDevice,
//                defaultDevice2,
//                defaultDevice3
//            );
//            deviceRepository.saveAll(devices);
        };
    }

}
