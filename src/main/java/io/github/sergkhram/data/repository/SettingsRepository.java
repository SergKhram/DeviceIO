package io.github.sergkhram.data.repository;

import io.github.sergkhram.data.entity.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SettingsRepository extends MongoRepository<Settings, String> {
}
