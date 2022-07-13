package io.github.sergkhram.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.data.entity.Settings;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.utils.Const;
import org.springframework.context.annotation.Scope;

import java.util.Objects;


@org.springframework.stereotype.Component
@Scope("prototype")
@PageTitle("Settings | DeviceIO")
@Route(value = "settings", layout = MainLayout.class)
public final class SettingsView extends VerticalLayout {
    TextField androidHomePath = new TextField("ANDROID_HOME path");
    IntegerField adbTimeout = new IntegerField("ADB Timeout(ms)");
    TextField downloadPath = new TextField("Download path");
    CrmService service;
    Button save = new Button("Save");
    Binder<Settings> binder = new BeanValidationBinder<>(Settings.class);

    public SettingsView(CrmService service) {
        this.service = service;
        binder.bindInstanceFields(this);
        addClassName("settings-view");
        setSizeFull();
        save.setThemeName(Lumo.DARK);
        save.addClickListener(click -> saveSettings());
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        add(getContent(),save);
        updateSettings();
    }

    private Component getContent() {
        FormLayout content = new FormLayout();
        content.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("20em", 2)
        );
        content.add(
            androidHomePath,
            adbTimeout,
            downloadPath
        );
        return content;
    }

    private void updateSettings() {
        Settings currentSettings = service.getCurrentSettings();
        if(currentSettings!=null) {
            binder.readBean(currentSettings);
        } else {
            adbTimeout.setValue(Const.TIMEOUT);
            downloadPath.setValue(Const.DEFAULT_DOWNLOAD_PATH);
        }
    }

    private void saveSettings() {
        Settings newSettings = Objects.requireNonNullElseGet(service.getCurrentSettings(), Settings::new);
        try {
            binder.writeBean(newSettings);
            service.saveSettings(newSettings);
            Integer currentAdbTimeout = newSettings.getAdbTimeout();
            if(currentAdbTimeout != null) {
                Const.TIMEOUT = currentAdbTimeout;
            }
            String currentDownloadPath = newSettings.getDownloadPath();
            if(currentDownloadPath != null && !currentDownloadPath.isEmpty()) {
                Const.DEFAULT_DOWNLOAD_PATH = currentDownloadPath;
            }
        } catch (ValidationException v) {
            v.printStackTrace();
        }
    }
}
