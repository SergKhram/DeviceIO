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
        };
    }

}
