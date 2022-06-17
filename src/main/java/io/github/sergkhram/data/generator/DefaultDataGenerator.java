package io.github.sergkhram.data.generator;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.repository.DeviceRepository;
import io.github.sergkhram.data.repository.HostRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class DefaultDataGenerator {

    @Bean
    public CommandLineRunner loadData(HostRepository hostRepository, DeviceRepository deviceRepository) {

        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (hostRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }

            List<Host> hosts = List.of(
                new Host("Localhost", "127.0.0.1", null),
                new Host("Localhost2", "127.0.0.1", null),
                new Host("NotActive", "196.1.1.8", null),
                new Host("Active", "192.168.0.1", null)
            );
            hostRepository.saveAll(hosts);
            Device defaultDevice = new Device();
            defaultDevice.setName("default");
            defaultDevice.setHost(hosts.get(0));
            defaultDevice.setState("");
            Device defaultDevice2 = new Device();
            defaultDevice2.setName("default2");
            defaultDevice2.setHost(hosts.get(1));
            defaultDevice2.setState("");
            Device defaultDevice3 = new Device();
            defaultDevice3.setName("default3");
            defaultDevice3.setHost(hosts.get(2));
            defaultDevice3.setState("");
            List<Device> devices = List.of(
                defaultDevice,
                defaultDevice2,
                defaultDevice3
            );
            deviceRepository.saveAll(devices);
        };
    }

}
