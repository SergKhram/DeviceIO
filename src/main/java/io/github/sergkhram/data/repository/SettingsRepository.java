package io.github.sergkhram.data.repository;

import io.github.sergkhram.data.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettingsRepository extends JpaRepository<Settings, UUID> {
}
