package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.Settings;
import io.github.sergkhram.data.service.CrmService;
import org.springframework.beans.factory.annotation.Autowired;

public class SettingsRequestsService {
    @Autowired
    CrmService crmService;

    public Settings getSettings() {
        return crmService.getCurrentSettings();
    }

    public void saveSettings(Settings settings) {
        crmService.saveSettings(settings);
    }
}
