package io.github.sergkhram.ui.views.list.forms;

import com.vaadin.flow.component.button.Button;
import io.github.sergkhram.ui.views.list.HostsListView;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;

import static io.github.sergkhram.Generator.generateRandomString;
import static io.github.sergkhram.utils.CustomAssertions.*;
import static io.github.sergkhram.utils.TestConst.mongoContainer;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {
        "grpc.server.port=-1"
    }
)
@Epic("DeviceIO")
@Feature("UI")
@Story("Host form view")
@DirtiesContext(classMode = BEFORE_CLASS)
@Testcontainers
public class HostFormTest {
    @Autowired
    HostsListView hostsListView;

    @Container
    public static MongoDBContainer container = new MongoDBContainer(mongoContainer);

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

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
