package io.github.sergkhram.views.list.forms;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.providers.IOSDeviceDirectoriesDataProvider;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.providers.AndroidDeviceDirectoriesDataProvider;
import io.github.sergkhram.managers.idb.IdbManager;

import java.io.File;

public final class DeviceForm extends FormLayout {
    Binder<Device> binder = new Binder<>(Device.class);

    TextField serial = new TextField("Device serial");
    TextField host = new TextField("Device host");
    TextField state = new TextField("Device state");
    TextArea shellResult = new TextArea("Shell result");
    TextField shellRequest = new TextField("Type your shell request");
    VerticalLayout shellCmdLayout;
    TreeGrid<DeviceDirectoryElement> treeGrid = new TreeGrid<>();
    HierarchicalDataProvider dataProvider;
    Dialog dialog = new Dialog();
    HorizontalLayout deviceFileExplorer;
    Anchor anchorElement;
    Text dialogText = new Text("");
    Device device;
    File currentFile;
    ComboBox<IOSPackageType> iosPackageTypeComboBox;
    TextField bundle = new TextField("Type your bundle");
    VerticalLayout iosLayoutForExplorer;

    public DeviceForm() {
        addClassName("device-form");
        binder.forField(serial)
            .bind(Device::getSerial, null);
        binder.forField(host)
            .bind(deviceData -> deviceData.getHost().getAddress(), null);
        binder.forField(state)
            .bind(Device::getState, null);
        serial.setReadOnly(true);
        host.setReadOnly(true);
        state.setReadOnly(true);

        prepareDialog();

        add(serial,
            host,
            state,
            prepareShellCmdLayout(),
            prepareIosFileExplorerHelper(),
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
                fireEvent(
                    new DownloadFileEvent(
                        this, device, click.getItem().get(), dialog, iosPackageTypeComboBox.getValue()
                    )
                );
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

    public void clearShellLayout() {
        shellRequest.clear();
        clearShellResult();
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

    public void setVisibleShellLayout(boolean visible) {
        visible = visible && device.getIsActive();
        shellCmdLayout.setVisible(visible);
    }

    public void setVisibleIOSLayoutForExplorer(boolean visible) {
        visible = visible && device.getIsActive();
        iosLayoutForExplorer.setVisible(visible);
    }

    public void initDeviceExplorer(AdbManager adbManager, IdbManager idbManager) {
        if(device.getIsActive()) {
            dataProvider = device.getDeviceType().equals(DeviceType.ANDROID)
                ? new AndroidDeviceDirectoriesDataProvider(device, adbManager)
                : new IOSDeviceDirectoriesDataProvider(
                    device, idbManager, "", iosPackageTypeComboBox.getValue()
                );
            treeGrid.setDataProvider(dataProvider);
            treeGrid.setVisible(true);
            deviceFileExplorer.setVisible(true);
        }
    }

    private VerticalLayout prepareShellCmdLayout() {
        shellResult.setReadOnly(true);
        shellResult.setVisible(false);
        Button executeButton = new Button("Execute");
        executeButton.setThemeName(Lumo.DARK);
        executeButton.addClickListener(click -> executeShellCmd());
        shellRequest.setClearButtonVisible(true);
        HorizontalLayout shellEnterLayout = new HorizontalLayout(shellRequest, executeButton);
        shellEnterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        shellCmdLayout = new VerticalLayout(shellEnterLayout, shellResult);
        shellCmdLayout.addClassName("shellCmd");
        return shellCmdLayout;
    }

    private VerticalLayout prepareIosFileExplorerHelper() {
        Button executeButton = new Button("Open");
        executeButton.setThemeName(Lumo.DARK);
        executeButton.addClickListener(click ->
            fireEvent(
                new ReinitFileExplorerEvent(this, device, bundle.getValue(), iosPackageTypeComboBox.getValue())
            )
        );
        bundle.setClearButtonVisible(true);
        HorizontalLayout hl = new HorizontalLayout(bundle, executeButton);
        hl.setAlignItems(FlexComponent.Alignment.BASELINE);
        iosPackageTypeComboBox = new ComboBox<>("iOS Package Type");
        iosPackageTypeComboBox.setItems(IOSPackageType.values());
        iosPackageTypeComboBox.setItemLabelGenerator(IOSPackageType::name);
        iosPackageTypeComboBox.setValue(IOSPackageType.ROOT);
        iosLayoutForExplorer = new VerticalLayout(
            iosPackageTypeComboBox,
            hl
        );
        iosLayoutForExplorer.setVisible(false);
        return iosLayoutForExplorer;
    }

    private void executeShellCmd() {
        fireEvent(new ExecuteShellEvent(this, device, shellRequest));
    }

    public void setNewDataProviderFileExplorer(HierarchicalDataProvider newDataProvider) {
        dataProvider = newDataProvider;
        treeGrid.setDataProvider(dataProvider);
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
        private final TextField shellRequest;

        ExecuteShellEvent(DeviceForm source, Device device, TextField shellRequest) {
            super(source, device);
            this.shellRequest = shellRequest;
        }

        public String getShellRequestValue() {
            return shellRequest.getValue();
        }
    }

    public static class DownloadFileEvent extends DeviceFormEvent {
        private DeviceDirectoryElement deviceDirectoryElement;
        private Dialog dialog;
        private IOSPackageType iosPackageType;

        DownloadFileEvent(
            DeviceForm source,
            Device device,
            DeviceDirectoryElement deviceDirectoryElement,
            Dialog dialog,
            IOSPackageType iosPackageType
        ) {
            super(source, device);
            this.deviceDirectoryElement = deviceDirectoryElement;
            this.dialog = dialog;
            this.iosPackageType = iosPackageType;
        }

        public DeviceDirectoryElement getDeviceDirectoryElement() {
            return deviceDirectoryElement;
        }

        public Dialog getDialog() {
            return dialog;
        }

        public IOSPackageType getIosPackageType() {
            return iosPackageType;
        }
    }

    public static class DeleteFilesEvent extends DeviceFormEvent {
        public File currentFile;

        DeleteFilesEvent(DeviceForm source, Device device, File currentFile) {
            super(source, device);
            this.currentFile = currentFile;
        }
    }

    public static class ReinitFileExplorerEvent extends DeviceFormEvent {
        private final String bundle;
        private final IOSPackageType iosPackage;

        ReinitFileExplorerEvent(DeviceForm source, Device device, String bundle, IOSPackageType iosPackage) {
            super(source, device);
            this.bundle = bundle;
            this.iosPackage = iosPackage;
        }

        public String getBundle() {
            return bundle;
        }

        public IOSPackageType getIosPackageType() {
            return iosPackage;
        }
    }


    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
