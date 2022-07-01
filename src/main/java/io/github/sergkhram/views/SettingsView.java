package io.github.sergkhram.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.entity.Settings;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.utils.Const;
import io.github.sergkhram.views.list.forms.HostForm;
import org.springframework.context.annotation.Scope;

import java.util.Objects;


@org.springframework.stereotype.Component
@Scope("prototype")
@PageTitle("Settings | DeviceIO")
@Route(value = "settings", layout = MainLayout.class)
public final class SettingsView extends VerticalLayout {
    TextField androidHomePath = new TextField("ANDROID_HOME path");
    IntegerField adbTimeout = new IntegerField("ADB Timeout(ms)");
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
        content.add(androidHomePath);
        content.add(adbTimeout);
        return content;
    }

    private void updateSettings() {
        Settings currentSettings = service.getCurrentSettings();
        if(currentSettings!=null) {
            binder.readBean(currentSettings);
        } else {
            adbTimeout.setValue(Const.TIMEOUT);
        }
    }

    private void saveSettings() {
        Settings newSettings = Objects.requireNonNullElseGet(service.getCurrentSettings(), Settings::new);
        try {
            binder.writeBean(newSettings);
            service.settingsRepository.save(newSettings);
            Integer currentAdbTimeout = newSettings.getAdbTimeout();
            if(currentAdbTimeout != null) {
                Const.TIMEOUT = currentAdbTimeout;
            }
        } catch (ValidationException v) {
            v.printStackTrace();
        }
    }
}
