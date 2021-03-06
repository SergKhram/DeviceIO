package io.github.sergkhram.ui.views.list.forms;

import com.vaadin.flow.component.button.Button;
import io.github.sergkhram.ui.views.list.HostsListView;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;

import static io.github.sergkhram.Generator.generateRandomString;
import static io.github.sergkhram.utils.CustomAssertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {
        "grpc.server.port=-1"
    }
)
@Epic("DeviceIO")
@Feature("UI")
@Story("Host form view")
public class HostFormTest {
    @Autowired
    HostsListView hostsListView;

    @BeforeEach
    public void beforeTest() {
        HostForm form = getHostForm();
        form.setVisible(false);
        form.name.clear();
        form.address.clear();
        form.port.clear();
    }

    @Test
    public void checkFormActiveButtonsTest() {
        HostForm form = clickAddNewHost();
        assertTrueWithAllure(form.isVisible());
        assertTrueWithAllure(form.close.isEnabled());
        assertTrueWithAllure(form.delete.isEnabled());
        assertFalseWithAllure(form.save.isEnabled());
    }

    @Test
    public void checkFormCloseButtonTest() {
        HostForm form = clickAddNewHost();
        assertTrueWithAllure(form.isVisible());
        form.close.click();
        assertFalseWithAllure(form.isVisible());
    }

    @Test
    public void checkAddHostValidation() {
        HostForm form = clickAddNewHost();
        assertFalseWithAllure(form.save.isEnabled());
        form.name.setValue(generateRandomString());
        form.address.setValue(generateRandomString());
        form.port.setValue(100000);
        assertFalseWithAllure(form.save.isEnabled());
        form.port.setValue(500);
        assertTrueWithAllure(form.save.isEnabled());
        form.port.clear();
        assertTrueWithAllure(form.save.isEnabled());
    }

    @SneakyThrows
    private Button getAddHostButton() {
        Field addHostButtonField = HostsListView.class.getDeclaredField("addHostButton");
        addHostButtonField.setAccessible(true);
        return (Button) addHostButtonField.get(hostsListView);
    }

    @SneakyThrows
    private HostForm getHostForm() {
        Field hostForm = HostsListView.class.getDeclaredField("form");
        hostForm.setAccessible(true);
        return (HostForm) hostForm.get(hostsListView);
    }

    private HostForm clickAddNewHost() {
        Button addHostButton = getAddHostButton();
        addHostButton.click();
        return getHostForm();
    }
}
