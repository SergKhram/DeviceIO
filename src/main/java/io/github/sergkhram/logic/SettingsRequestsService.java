package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.Settings;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.managers.adb.AdbManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class SettingsRequestsService {
    @Autowired
    CrmService crmService;

    @Autowired
    AdbManager adbManager;

    public Settings getSettings() {
        return crmService.getCurrentSettings();
    }

    public void saveSettings(Settings settings) {
        if(
            settings.getAndroidHomePath()!=null
                && !settings.getAndroidHomePath().isEmpty()
                && !settings.getAndroidHomePath().equals(
                    Objects.requireNonNullElseGet(getSettings(), Settings::new)
                        .getAndroidHomePath()==null ? "" : getSettings().getAndroidHomePath()
                )
        ) adbManager.reinitializeAdb(settings.getAndroidHomePath());
        crmService.saveSettings(settings);
    }
}
