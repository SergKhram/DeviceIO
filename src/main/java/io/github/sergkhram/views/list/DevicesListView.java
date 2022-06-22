package io.github.sergkhram.views.list;

import com.malinskiy.adam.request.device.DeviceState;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.data.adb.AdbManager;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.views.MainLayout;
import io.github.sergkhram.views.list.forms.DeviceForm;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Scope;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.Comparator;
import java.util.Map;

@org.springframework.stereotype.Component
@Scope("prototype")
@PageTitle("Devices | STUDIO18")
@Route(value = "devices", layout = MainLayout.class)
public final class DevicesListView extends VerticalLayout {
    ComboBox<Host> hosts = new ComboBox<>();
    Grid<Device> grid = new Grid<>(Device.class);
    TextField filterText = new TextField();
    CrmService service;
    AdbManager adbManager;
    DeviceForm form;

    public DevicesListView(CrmService service, AdbManager adbManager) {
        this.service = service;
        this.adbManager = adbManager;
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
        filterText.setPlaceholder("Filter by device name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList(hosts.getValue()));

        hosts.setItems(service.findAllHosts(""));
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
        grid.setColumns("name");
        grid.addColumn(Device::getIsActive).setHeader("Active").setComparator(
            Comparator.comparing(Device::getIsActive)
        );
        grid.addColumn(device -> device.getHost().getName()).setHeader("Host").setComparator(
            Comparator.comparing(o -> o.getHost().getName())
        );
        grid.addComponentColumn(
            device -> {
                Button rebootButton = new Button("Reboot");
                rebootButton.setThemeName(Lumo.DARK);
                rebootButton.addClickListener(
                    click -> adbManager.rebootDevice(device)
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
            grid.setItems(service.findAllDevices(filterText.getValue(), host.getId()));
    }

    private void updateList() {
        grid.setItems(service.findAllDevices(filterText.getValue()));
    }

    private void updateDevicesState() {
        Map<String, String> devicesStates = adbManager.getDevicesStates();
        service.findAllDevices("").parallelStream().forEach(
            it -> {
                String currentState = devicesStates.getOrDefault(it.getName(), null);
                if(currentState == null || currentState.equals(DeviceState.OFFLINE.name())) {
                    it.setState(DeviceState.OFFLINE.name());
                    it.setIsActive(false);
                } else {
                    if(!it.getState().equals(currentState)) {
                        it.setState(currentState);
                        it.setIsActive(true);
                    }
                }
                service.saveDevice(it);
            }
        );
        updateList();
    }

    public void configureForm() {
        form = new DeviceForm();
        form.setWidth("35em");
        form.addListener(DeviceForm.ExecuteShellEvent.class, this::executeShell);
        form.addListener(DeviceForm.DownloadFileEvent.class, this::downloadFile);
        form.addListener(DeviceForm.DeleteFilesEvent.class, this::deleteFiles);
    }

    private void executeShell(DeviceForm.ExecuteShellEvent executeShellEvent) {
        form.clearShellResult();
        String result = adbManager.executeShell(executeShellEvent.getDevice(), form.getShellRequest());
        form.setShellResult(result);
    }

    private void downloadFile(DeviceForm.DownloadFileEvent downloadFileEvent) {
        DeviceDirectoryElement currentDeviceDirectoryElement = downloadFileEvent.deviceDirectoryElement;
        StreamResource resource = null;
        String error = "";
        if(!currentDeviceDirectoryElement.isDirectory) {
            try {
                File file = adbManager.downloadFile(
                    downloadFileEvent.getDevice(),
                    currentDeviceDirectoryElement,
                    "./target"
                );
                InputStream inputStream = new FileInputStream(
                    file);
                resource = new StreamResource(file.getName(), () -> inputStream);
                form.setCurrentFile(file);
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getLocalizedMessage();
            }
        } else {
            try {
                File directory = adbManager.downloadFolder(
                    downloadFileEvent.getDevice(),
                    currentDeviceDirectoryElement,
                    "./target"
                );
                if(directory.exists()) {
                    File zipFile = new File(directory + ".zip");
                    ZipUtil.pack(directory, zipFile);
                    InputStream inputStream = new FileInputStream(
                        zipFile);
                    resource = new StreamResource(zipFile.getName(), () -> inputStream);
                    FileUtils.deleteDirectory(directory);
                    form.setCurrentFile(zipFile);
                } else {
                    error = "Empty directory!";
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getLocalizedMessage();
            }
        }
        if(error.isEmpty()) {
            Anchor link = new Anchor();
            link.setHref(resource);
            link.getElement().setAttribute("download", true);
            Button downloadButton = new Button(
                VaadinIcon.DOWNLOAD.create(),
                click -> click.getSource().setEnabled(false)
            );
            link.add(downloadButton);
            downloadFileEvent.dialog.getFooter().add(link);
            form.setAnchorElement(link);
        } else {
            form.setDialogText(error);
        }
    }

    private void deleteFiles(DeviceForm.DeleteFilesEvent deleteFilesEvent) {
        File currentFile = deleteFilesEvent.currentFile;
        if(currentFile.exists()) {
            FileUtils.deleteQuietly(currentFile);
        }
    }

    private void closeEditor() {
        form.setDevice(null);
        form.setVisible(false);
        form.clearShellRequest();
        form.clearShellResult();
        form.clearDeviceExplorer();
        removeClassName("device-interact");
    }

    public void openDevice(Device device) {
        if (device == null) {
            closeEditor();
        } else {
            form.setDevice(device);
            form.setVisible(true);
            form.initDeviceExplorer(adbManager);
            addClassName("device-interact");
        }
    }
}
