package io.github.sergkhram.ui.views.list;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.logic.HostRequestsService;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.ui.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.sergkhram.ui.views.list.forms.HostForm;
import org.springframework.context.annotation.Scope;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Scope("prototype")
@PageTitle("Hosts | DeviceIO")
@Route(value = "", layout = MainLayout.class)
public final class HostsListView extends VerticalLayout {
    Grid<Host> grid = new Grid<>(Host.class);
    TextField filterText = new TextField();
    HostForm form;
    Button addHostButton = new Button("Add host");

    HostRequestsService hostRequestsService;
    DeviceRequestsService deviceRequestsService;

    public HostsListView(
        HostRequestsService hostRequestsService,
        DeviceRequestsService deviceRequestsService
    ) {
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

    private void configureForm() {
        form = new HostForm();
        form.setWidth("25em");
        form.addListener(HostForm.SaveEvent.class, this::saveHost);
        form.addListener(HostForm.DeleteEvent.class, this::deleteHost);
        form.addListener(HostForm.CloseEvent.class, e -> closeEditor());
    }

    private void saveHost(HostForm.SaveEvent saveEvent) {
        Host host = saveEvent.getHost();
        hostRequestsService.saveHost(host);
        updateList();
        hostRequestsService.connect(host);
        closeEditor();
    }

    private void deleteHost(HostForm.DeleteEvent deleteEvent) {
        Host host = deleteEvent.getHost();
        hostRequestsService.deleteHost(host);
        updateList();
        hostRequestsService.disconnect(host);
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
                    List<Device> dbListOfDevices = deviceRequestsService.getDBDevicesList(
                        "",
                        host.getId()
                    );
                    List<Device> currentListOfDevices = deviceRequestsService.getCurrentDevicesList(
                        host.getId()
                    );
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
        grid.setItems(hostRequestsService.getHostsList(filterText.getValue()));
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
        hostRequestsService.getHostsList("").parallelStream().forEach(
            this::updateHostState
        );
        updateList();
    }

    public void updateHostState(Host host) {
        deviceRequestsService.updateHostStateWithDeviceRemoval(host);
    }

    private void updateDeviceList(List<Device> dbListOfDevices, List<Device> currentListOfDevices) {
        dbListOfDevices.stream().filter(device ->
            !currentListOfDevices
                .stream()
                .map(Device::getSerial)
                .collect(Collectors.toList())
                .contains(device.getSerial())
        ).forEach(device ->
            deviceRequestsService.deleteDevice(device)
        );
        deviceRequestsService.saveDevices(
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
