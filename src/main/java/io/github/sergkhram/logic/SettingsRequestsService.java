package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.Settings;
import io.github.sergkhram.data.service.CrmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
