package io.github.sergkhram.views.list.forms;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.data.adb.AdbManager;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.providers.DeviceDirectoriesDataProvider;
import java.io.File;

public class DeviceForm extends FormLayout {
    Binder<Device> binder = new Binder<>(Device.class);

    TextField name = new TextField("Device name");
    TextField host = new TextField("Device host");
    TextField state = new TextField("Device state");
    TextArea shellResult = new TextArea("Shell result");
    TextField shellRequest = new TextField("Type your shell request");
    TreeGrid<DeviceDirectoryElement> treeGrid = new TreeGrid<>();
    HierarchicalDataProvider<DeviceDirectoryElement, Void> dataProvider;
    Dialog dialog = new Dialog();
    HorizontalLayout deviceFileExplorer;
    Anchor anchorElement;
    Text dialogText = new Text("");
    private Device device;
    File currentFile;

    public DeviceForm() {
        addClassName("device-form");
        binder.forField(name)
            .bind(Device::getName, null);
        binder.forField(host)
            .bind(deviceData -> deviceData.getHost().getAddress(), null);
        binder.forField(state)
            .bind(Device::getState, null);
        name.setReadOnly(true);
        host.setReadOnly(true);
        state.setReadOnly(true);
        shellResult.setReadOnly(true);
        shellResult.setVisible(false);

        prepareDialog();

        add(name,
            host,
            state,
            prepareShellCmdLayout(),
            prepareDeviceFileExplorer()
        );
    }

    private void prepareDialog() {
        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickListener(
            click -> closeDialogAction()
        );

        dialog.setHeaderTitle("Confirm downloading");
        setDialogText();
        dialog.add(dialogText);
        Button closeButton = new Button(
            new Icon("lumo", "cross"),
            click -> closeDialogAction()
        );
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeButton);
        dialog.getFooter().add(cancel);
    }

    private void closeDialogAction() {
        dialog.close();
        dialog.getFooter().remove(anchorElement);
        anchorElement = null;
        setDialogText();
        fireEvent(new DeleteFilesEvent(this, device, currentFile));
    }

    public void setAnchorElement(Anchor anchorElement) {
        this.anchorElement = anchorElement;
    }

    public void setDialogText(String text) {
        dialogText.setText(text);
    }

    public void setDialogText() {
        setDialogText("Are you sure you want to download this file/directory?");
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    private VerticalLayout prepareDeviceFileExplorer() {
        treeGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        treeGrid.addHierarchyColumn(DeviceDirectoryElement::getName)
            .setHeader("Name");
        treeGrid.addColumn(DeviceDirectoryElement::getSize)
            .setHeader("Size");
        GridContextMenu<DeviceDirectoryElement> menu = treeGrid.addContextMenu();
        menu.addItem(
            "Download", click -> {
                setDialogText();
                fireEvent(new DownloadFileEvent(this, device, click.getItem().get(), dialog));
                dialog.open();
            }
        );

        H3 gridTitle = new H3("File explorer");
        gridTitle.getStyle().set("margin", "0");
        deviceFileExplorer = new HorizontalLayout(gridTitle);
        deviceFileExplorer.setAlignItems(FlexComponent.Alignment.BASELINE);
        return new VerticalLayout(
            deviceFileExplorer,
            treeGrid
        );
    }

    public void setDevice(Device device) {
        this.device = device;
        binder.readBean(device);
    }

    public void setShellResult(String result) {
        if(!result.isEmpty()) {
            shellResult.setValue(result);
            shellResult.setVisible(true);
            shellResult.setMaxHeight("150px");
            shellResult.setWidthFull();
        }
    }

    public String getShellRequest() {
        return shellRequest.getValue();
    }

    public void clearShellRequest() {
        shellRequest.clear();
    }

    public void clearShellResult() {
        shellResult.clear();
        shellResult.setVisible(false);
    }

    public void clearDeviceExplorer() {
        treeGrid.setVisible(false);
        dataProvider = null;
        deviceFileExplorer.setVisible(false);
        anchorElement = null;
    }

    public void initDeviceExplorer(AdbManager adbManager) {
        if(device.getIsActive()) {
            dataProvider = new DeviceDirectoriesDataProvider(device, adbManager);
            treeGrid.setDataProvider(dataProvider);
            treeGrid.setVisible(true);
            deviceFileExplorer.setVisible(true);
        }
    }

    private VerticalLayout prepareShellCmdLayout() {
        Button executeButton = new Button("Execute");
        executeButton.setThemeName(Lumo.DARK);
        executeButton.addClickListener(click -> executeShellCmd());
        shellRequest.setClearButtonVisible(true);
        HorizontalLayout shellEnterLayout = new HorizontalLayout(shellRequest, executeButton);
        shellEnterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        VerticalLayout shellCmdLayout = new VerticalLayout(shellEnterLayout, shellResult);
        shellCmdLayout.addClassName("shellCmd");
        return shellCmdLayout;
    }

    private void executeShellCmd() {
        fireEvent(new ExecuteShellEvent(this, device));
    }

    public static abstract class DeviceFormEvent extends ComponentEvent<DeviceForm> {
        private Device device;

        protected DeviceFormEvent(DeviceForm source, Device device) {
            super(source, false);
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }
    }

    public static class ExecuteShellEvent extends DeviceFormEvent {
        ExecuteShellEvent(DeviceForm source, Device device) {
            super(source, device);
        }
    }

    public static class DownloadFileEvent extends DeviceFormEvent {
        public DeviceDirectoryElement deviceDirectoryElement;
        public Dialog dialog;

        DownloadFileEvent(DeviceForm source, Device device, DeviceDirectoryElement deviceDirectoryElement, Dialog dialog) {
            super(source, device);
            this.deviceDirectoryElement = deviceDirectoryElement;
            this.dialog = dialog;
        }
    }

    public static class DeleteFilesEvent extends DeviceFormEvent {
        public File currentFile;

        DeleteFilesEvent(DeviceForm source, Device device, File currentFile) {
            super(source, device);
            this.currentFile = currentFile;
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
