package io.github.sergkhram.views.list;

import com.malinskiy.adam.request.device.DeviceState;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.data.providers.IOSDeviceDirectoriesDataProvider;
import io.github.sergkhram.data.service.DownloadService;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.logic.HostRequestsService;
import io.github.sergkhram.managers.Manager;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.managers.idb.IdbManager;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.views.MainLayout;
import io.github.sergkhram.views.list.forms.DeviceForm;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Scope;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sergkhram.utils.Const.IOS_OFFLINE_STATE;
import static io.github.sergkhram.utils.Utils.getManagerByType;

@org.springframework.stereotype.Component
@Scope("prototype")
@PageTitle("Devices | DeviceIO")
@Route(value = "devices", layout = MainLayout.class)
public final class DevicesListView extends VerticalLayout {
    ComboBox<Host> hosts = new ComboBox<>();
    Grid<Device> grid = new Grid<>(Device.class);
    TextField filterText = new TextField();
    DeviceForm form;
    DownloadService downloadService;

    HostRequestsService hostRequestsService;
    DeviceRequestsService deviceRequestsService;

    public DevicesListView(
        DownloadService downloadService,
        HostRequestsService hostRequestsService,
        DeviceRequestsService deviceRequestsService
    ) {
        this.downloadService = downloadService;
        this.deviceRequestsService = deviceRequestsService;
        this.hostRequestsService = hostRequestsService;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();
        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList(hosts.getValue()));

        hosts.setItems(hostRequestsService.getHostsList(""));
        hosts.setItemLabelGenerator(Host::getName);
        hosts.setClearButtonVisible(true);
        hosts.setHelperText("Choose the host to see the devices");
        hosts.addValueChangeListener(change -> updateList(change.getValue()));
        hosts.setPlaceholder("Hosts");

        Button updateDevicesStateButton = new Button("Update the state of the devices");
        updateDevicesStateButton.addClickListener(click -> updateDevicesState());
        updateDevicesStateButton.setThemeName(Lumo.DARK);

        HorizontalLayout toolbar = new HorizontalLayout(filterText, hosts, updateDevicesStateButton);

        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassNames("devices-grid");
        grid.setSizeFull();
        grid.setColumns("serial");
        grid.addColumn(Device::getName).setHeader("Name").setComparator(
            Comparator.comparing(Device::getName)
        );
        grid.addColumn(Device::getIsActive).setHeader("Active").setComparator(
            Comparator.comparing(Device::getIsActive)
        );
        grid.addColumn(device -> device.getHost().getName()).setHeader("Host").setComparator(
            Comparator.comparing(o -> o.getHost().getName())
        );
        grid.addComponentColumn(
            device -> {
                Image image;
                switch (device.getOsType()) {
                    case ANDROID:
                        image = new Image("images/android.png", "Android");
                        break;
                    case IOS:
                        image = new Image("images/appleinc.png", "Apple");
                        break;
                    default:
                        image = new Image();
                        break;
                }
                return image;
            }
        ).setHeader("Type").setComparator(
            Comparator.comparing(Device::getOsType)
        );
        grid.addComponentColumn(
            device -> {
                Button rebootButton = new Button("Reboot");
                rebootButton.setThemeName(Lumo.DARK);
                rebootButton.addClickListener(
                    click -> {
                        deviceRequestsService.reboot(device);
                    }
                );
                return rebootButton;
            }
        );
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event ->
            {
                form.clearDeviceExplorer();
                openDevice(event.getValue());
            }
        );

    }

    private void updateList(Host host) {
        if (host == null)
            updateList();
        else
            grid.setItems(deviceRequestsService.getDBDevicesList(filterText.getValue(), host.getId()));
    }

    private void updateList() {
        grid.setItems(deviceRequestsService.getDBDevicesList(filterText.getValue(), ""));
    }

    private void updateDevicesState() {
        Map<String, String> devicesStates = deviceRequestsService.getDevicesStates();
        deviceRequestsService.getDBDevicesList("", "").parallelStream().forEach(
            it -> {
                String currentState = devicesStates.getOrDefault(it.getSerial(), null);
                if (
                    currentState == null
                        || currentState.equals(DeviceState.OFFLINE.name())
                        || currentState.equalsIgnoreCase(IOS_OFFLINE_STATE)
                ) {
                    it.setState(
                        currentState != null ? currentState : DeviceState.OFFLINE.name()
                    );
                    it.setIsActive(false);
                } else {
                    if (!it.getState().equals(currentState)) {
                        it.setState(currentState);
                        it.setIsActive(true);
                    }
                }
                deviceRequestsService.saveDevice(it);
            }
        );
        updateList();
    }

    public void configureForm() {
        form = new DeviceForm();
        form.setWidth("35em");
        form.addListener(DeviceForm.ExecuteShellEvent.class, this::executeShell);
        form.addListener(DeviceForm.DownloadEvent.class, this::download);
        form.addListener(DeviceForm.DeleteFilesEvent.class, this::deleteFiles);
        form.addListener(DeviceForm.ReinitFileExplorerEvent.class, this::reinitExplorer);
    }

    private void executeShell(DeviceForm.ExecuteShellEvent executeShellEvent) {
        form.clearShellResult();
        String result = deviceRequestsService.executeShell(
            executeShellEvent.getDevice(),
            executeShellEvent.getShellRequestValue()
        );
        form.setShellResult(result);
    }

    private void download(DeviceForm.DownloadEvent downloadEvent) {
        DeviceDirectoryElement currentDeviceDirectoryElement = downloadEvent.getDeviceDirectoryElement();
        StreamResource resource = null;
        String error = "";

        if (currentDeviceDirectoryElement.isDirectory != null && currentDeviceDirectoryElement.isDirectory) {
            try {
                DownloadService.DownloadData data = downloadService.downloadFolder(downloadEvent);
                if (data.error != null) {
                    error = data.error;
                } else {
                    resource = data.resource;
                    form.setCurrentFile(data.file);
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getLocalizedMessage();
            }
        } else {
            try {
                DownloadService.DownloadData data = downloadService.downloadFile(downloadEvent);
                resource = data.resource;
                form.setCurrentFile(data.file);
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getLocalizedMessage();
            }
        }
        if (error.isEmpty()) {
            Anchor link = prepareAnchor(resource, downloadEvent.getDialog());
            form.setAnchorElement(link);
        } else {
            form.setDownloadDialogText(error);
        }
    }

    private Anchor prepareAnchor(StreamResource resource, Dialog dialog) {
        Anchor link = new Anchor();
        link.setHref(resource);
        link.getElement().setAttribute("download", true);
        Button downloadButton = new Button(
            VaadinIcon.DOWNLOAD.create(),
            click -> click.getSource().setEnabled(false)
        );
        link.add(downloadButton);
        dialog.getFooter().add(link);
        return link;
    }

    private void deleteFiles(DeviceForm.DeleteFilesEvent deleteFilesEvent) {
        File currentFile = deleteFilesEvent.currentFile;
        if (currentFile != null && currentFile.exists()) {
            FileUtils.deleteQuietly(currentFile);
        }
    }

    private void reinitExplorer(DeviceForm.ReinitFileExplorerEvent reinitFileExplorerEvent) {
        form.setNewDataProviderFileExplorer(
            new IOSDeviceDirectoriesDataProvider(
                reinitFileExplorerEvent.getDevice(),
                deviceRequestsService,
                reinitFileExplorerEvent.getBundle(),
                reinitFileExplorerEvent.getIosPackageType()
            )
        );
    }

    private void closeEditor() {
        form.setDevice(null);
        form.setVisible(false);
        form.clearShellLayout();
        form.clearDeviceExplorer();
        removeClassName("device-interact");
    }

    public void openDevice(Device device) {
        if (device == null) {
            closeEditor();
        } else {
            form.setDevice(device);
            form.setVisible(true);
            if (device.getOsType().equals(OsType.IOS)) {
                form.setVisibleIOSLayoutForExplorer(true);
                form.setVisibleShellLayout(false);
            } else {
                form.setVisibleIOSLayoutForExplorer(false);
                form.setVisibleShellLayout(true);
            }
            form.initDeviceExplorer(deviceRequestsService);
            addClassName("device-interact");
        }
    }
}
