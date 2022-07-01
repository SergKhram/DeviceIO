package io.github.sergkhram.views.list;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.managers.Manager;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.managers.idb.IdbManager;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.sergkhram.views.list.forms.HostForm;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Scope("prototype")
@PageTitle("Hosts | DeviceIO")
@Route(value = "", layout = MainLayout.class)
public final class HostsListView extends VerticalLayout {
    Grid<Host> grid = new Grid<>(Host.class);
    TextField filterText = new TextField();
    HostForm form;
    CrmService service;
    List<Manager> managers;

    public HostsListView(CrmService service, AdbManager adbManager, IdbManager idbManager) {
        this.service = service;
        managers = List.of(adbManager, idbManager);
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

    private void configureForm() {
        form = new HostForm();
        form.setWidth("25em");
        form.addListener(HostForm.SaveEvent.class, this::saveHost);
        form.addListener(HostForm.DeleteEvent.class, this::deleteHost);
        form.addListener(HostForm.CloseEvent.class, e -> closeEditor());
    }

    private void saveHost(HostForm.SaveEvent saveEvent) {
        Host host = saveEvent.getHost();
        service.saveHost(host);
        updateList();
        managers
            .parallelStream()
            .forEach(
                it -> it.connectToHost(
                    host.getAddress(),
                    host.getPort()
                )
            );
//        adbManager.connectToHost(host.getAddress(), host.getPort());
//        idbManager.connectToHost(host.getAddress(), host.getPort());
        closeEditor();
    }

    private void deleteHost(HostForm.DeleteEvent deleteEvent) {
        Host host = deleteEvent.getHost();
        service.deleteHost(host);
        updateList();
        managers
            .parallelStream()
            .forEach(
                it -> it.disconnectHost(
                    host.getAddress(),
                    host.getPort()
                )
            );
//        adbManager.disconnectHost(host.getAddress(), host.getPort());
//        idbManager.disconnectHost(host.getAddress(), host.getPort());
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("name", "address", "port");
        grid.addColumn(Host::getIsActive).setHeader("Active").setComparator(
            Comparator.comparing(Host::getIsActive)
        );
        grid.addComponentColumn(host -> {
            Button connectButton = new Button("Connect");
            connectButton.setThemeName(Lumo.DARK);
            connectButton.addClickListener(
                click -> {
                    List<Device> dbListOfDevices = service.findAllDevices("", host.getId());
                    CopyOnWriteArrayList<Device> currentListOfDevices = new CopyOnWriteArrayList<>();
                    managers
                        .parallelStream()
                        .forEach(
                            it -> currentListOfDevices.addAll(it.getListOfDevices(host))
                        );

//                    List<Device> currentListOfAndroidDevices = adbManager.getListOfDevices(host);
//                    List<Device> currentListOfIOSDevices = idbManager.getListOfDevices(host);
//                    List<Device> currentListOfDevices = new ArrayList<>(currentListOfAndroidDevices);
//                    currentListOfDevices.addAll(currentListOfIOSDevices);
                    updateDeviceList(dbListOfDevices, currentListOfDevices);
                }
            );
            return connectButton;
        });
        grid.addComponentColumn(host -> {
            Button updateHostStateButton = new Button("Update state", new Icon(VaadinIcon.REFRESH));
            updateHostStateButton.setIconAfterText(false);
            updateHostStateButton.setThemeName(Lumo.DARK);
            updateHostStateButton.addClickListener(
                click -> {
                    updateHostState(host);
                    updateList();
                }
            );
            return updateHostStateButton;
        });
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event ->
            editHost(event.getValue()));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addHostButton = new Button("Add host");
        addHostButton.addClickListener(click -> addHost());
        Button updateHostsStateButton = new Button("Update the state of the hosts", new Icon(VaadinIcon.REFRESH));
        updateHostsStateButton.setIconAfterText(false);
        updateHostsStateButton.addClickListener(click -> updateHostsState());
        updateHostsStateButton.setThemeName(Lumo.DARK);

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addHostButton, updateHostsStateButton);

        toolbar.addClassName("toolbar");
        return toolbar;
    }

    public void editHost(Host host) {
        if (host == null) {
            closeEditor();
        } else {
            form.setHost(host);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void updateList() {
        grid.setItems(service.findAllHosts(filterText.getValue()));
    }

    private void closeEditor() {
        form.setHost(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    public void addHost() {
        grid.asSingleSelect().clear();
        editHost(new Host());
    }

    public void updateHostsState() {
        service.findAllHosts("").parallelStream().forEach(
            this::updateHostState
        );
        updateList();
    }

    public void updateHostState(Host host) {
        try {
            InetAddress address = InetAddress.getByName(host.getAddress());
            host.setIsActive(address.isReachable(5000));
            if(host.getIsActive()) {
                service.saveHost(host);
            } else {
                service.findAllDevices("", host.getId()).forEach(service::deleteDevice);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            service.findAllDevices("", host.getId()).forEach(service::deleteDevice);
        }
    }

    private void updateDeviceList(List<Device> dbListOfDevices, List<Device> currentListOfDevices) {
        dbListOfDevices.stream().filter(device ->
            !currentListOfDevices
                .stream()
                .map(Device::getSerial)
                .collect(Collectors.toList())
                .contains(device.getSerial())
        ).forEach(device ->
            service.deleteDevice(device)
        );
        service.saveDevices(
            currentListOfDevices.stream().filter(device ->
                !dbListOfDevices
                    .stream()
                    .map(Device::getSerial)
                    .collect(Collectors.toList())
                    .contains(device.getSerial())
            ).collect(Collectors.toList())
        );
    }
}
