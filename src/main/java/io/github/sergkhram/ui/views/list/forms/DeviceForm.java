package io.github.sergkhram.ui.views.list.forms;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.ui.providers.IOSDeviceDirectoriesDataProvider;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.ui.providers.AndroidDeviceDirectoriesDataProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Comparator;

@Slf4j
public final class DeviceForm extends FormLayout {
    Binder<Device> binder = new Binder<>(Device.class);

    TextArea shellResult = new TextArea("Shell result");
    TextField shellRequest = new TextField("Type your shell request");
    VerticalLayout shellCmdLayout;
    TreeGrid<DeviceDirectoryElement> fileExplorerGrid = new TreeGrid<>();
    Grid<AppDescription> appsGrid = new Grid<>(AppDescription.class);
    VerticalLayout appsLayout;
    HierarchicalDataProvider dataProvider;
    Dialog downloadDialog = new Dialog();
    Anchor anchorElement;
    Text downloadDialogText = new Text("");
    Device device;
    File currentFile;
    ComboBox<IOSPackageType> iosPackageTypeComboBox;
    TextField iosBundle = new TextField("Type your bundle");
    Details iosExplorerTuner;

    public Tab fileExplorerTab;
    public Tab appsTab;
    public Tab executeShellTab;
    public VerticalLayout tabContent;
    private Div actionsTabsDiv;
    private Tabs tabs;

    public DeviceForm() {
        addClassName("device-form");
        prepareDialog();
        prepareIosFileExplorerTuner();
        prepareDeviceFileExplorer();
        prepareShellCmdLayout();
        prepareAppsGrid();
        add(
            prepareDeviceInfoForm(),
            prepareTabs()
        );
    }

    private FormLayout prepareDeviceInfoForm() {
        TextField serial = new TextField("Device serial");
        TextField host = new TextField("Device host");
        TextField state = new TextField("Device state");
        TextField osVersion = new TextField("Device OS version");
        binder.forField(serial)
            .bind(Device::getSerial, null);
        binder.forField(host)
            .bind(deviceData -> deviceData.getHost().getAddress(), null);
        binder.forField(state)
            .bind(Device::getState, null);
        binder.forField(osVersion)
            .bind(Device::getOsVersion, null);

        FormLayout deviceInfoLayout = new FormLayout();
        deviceInfoLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("20em", 2)
        );
        deviceInfoLayout.add(
            serial,
            host,
            state,
            osVersion
        );
        deviceInfoLayout.getChildren().forEach(it -> ((TextField) it).setReadOnly(true));
        return deviceInfoLayout;
    }

    private void prepareDialog() {
        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickListener(
            click -> closeDialogAction()
        );

        downloadDialog.setHeaderTitle("Confirm downloading");
        setDownloadDialogText();
        downloadDialog.add(downloadDialogText);
        Button closeButton = new Button(
            new Icon("lumo", "cross"),
            click -> closeDialogAction()
        );
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        downloadDialog.getHeader().add(closeButton);
        downloadDialog.getFooter().add(cancel);
        downloadDialog.addDialogCloseActionListener(
            click -> closeDialogAction()
        );
    }

    private void closeDialogAction() {
        downloadDialog.close();
        if(anchorElement != null) {
            downloadDialog.getFooter().remove(anchorElement);
            anchorElement = null;
        }
        setDownloadDialogText();
        fireEvent(new DeleteFilesEvent(this, device, currentFile));
    }

    public void setAnchorElement(Anchor anchorElement) {
        this.anchorElement = anchorElement;
    }

    public void setDownloadDialogText(String text) {
        downloadDialogText.setText(text);
    }

    public void setDownloadDialogText() {
        setDownloadDialogText("Are you sure you want to download this file/directory?");
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    private Grid prepareDeviceFileExplorer() {
        fileExplorerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        fileExplorerGrid.addHierarchyColumn(DeviceDirectoryElement::getName)
            .setAutoWidth(true)
            .setFlexGrow(0)
            .setResizable(true)
            .setHeader("Name");
        fileExplorerGrid.addColumn(DeviceDirectoryElement::getSize)
            .setHeader("Size")
            .setAutoWidth(true)
            .setFlexGrow(0)
            .setTextAlign(ColumnTextAlign.END);
        GridContextMenu<DeviceDirectoryElement> contextMenu = fileExplorerGrid.addContextMenu();
        contextMenu.addItem(
            "Download", click -> {
                setDownloadDialogText();
                fireEvent(
                    new DownloadEvent(
                        this, device, click.getItem().get(), downloadDialog, iosPackageTypeComboBox.getValue()
                    )
                );
                downloadDialog.open();
            }
        );
        return fileExplorerGrid;
    }

    private Div prepareTabs() {
        fileExplorerTab = new Tab("File Explorer");
        appsTab = new Tab("Apps");
        executeShellTab = new Tab("Execute shell");

        executeShellTab.setEnabled(false);

        tabs = new Tabs(fileExplorerTab, appsTab, executeShellTab);
        tabs.addThemeVariants(TabsVariant.LUMO_CENTERED);
        tabs.addSelectedChangeListener(
            event -> fireEvent(
                new ReinitTabsEvent(
                    this,
                    device,
                    event.getSelectedTab(),
                    iosExplorerTuner,
                    fileExplorerGrid,
                    shellCmdLayout,
                    appsLayout
                )
            )
        );

        tabContent = new VerticalLayout();
        tabContent.setSpacing(false);
        setContent(tabs.getSelectedTab());
        actionsTabsDiv = new Div();
        actionsTabsDiv.add(
            tabs,
            tabContent
        );
        return actionsTabsDiv;
    }

    private void setContent(Tab tab) {
        clearTabs();
        if (tab.equals(fileExplorerTab)) {
            tabContent.add(
                iosExplorerTuner,
                fileExplorerGrid
            );
        } else if(tab.equals(appsTab)) {
            tabContent.add(

            );
        } else {
            tabContent.add(
                shellCmdLayout
            );
        }
    }

    public void clearTabs() {
        tabContent.removeAll();
        try {
            clearDeviceExplorer();
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
        }
        try {
            clearShellLayout();
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
        }
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
        fileExplorerGrid.setVisible(false);
        dataProvider = null;
        anchorElement = null;
    }

    public void setVisibleIOSLayoutForExplorer(boolean visible) {
        visible = visible && device.getIsActive();
        iosExplorerTuner.setVisible(visible);
    }

    public void setEnabledShellTab(boolean enabled) {
        executeShellTab.setEnabled(enabled);
        shellCmdLayout.setVisible(enabled);
    }

    public void setVisibleTabs(boolean visible) {
        visible = visible && device.getIsActive();
        actionsTabsDiv.setVisible(visible);
    }

    public void initDeviceExplorer(DeviceRequestsService deviceRequestsService) {
        dataProvider = device.getOsType().equals(OsType.ANDROID)
            ? new AndroidDeviceDirectoriesDataProvider(
                device,
                deviceRequestsService
            )
            : new IOSDeviceDirectoriesDataProvider(
                device,
                deviceRequestsService,
                "",
                iosPackageTypeComboBox.getValue()
            );
        fileExplorerGrid.setDataProvider(dataProvider);
        fileExplorerGrid.setVisible(true);
    }

    public void updateAppsGrid(DeviceRequestsService deviceRequestsService) {
        appsGrid.setItems(deviceRequestsService.getAppsList(device));
    }

    public void setDefaultTab() {
        tabs.setSelectedTab(fileExplorerTab);
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

    private Details prepareIosFileExplorerTuner() {
        Button executeButton = new Button("Open");
        executeButton.setThemeName(Lumo.DARK);
        executeButton.addClickListener(click ->
            fireEvent(
                new ReinitFileExplorerEvent(this, device, iosBundle.getValue(), iosPackageTypeComboBox.getValue())
            )
        );
        iosBundle.setClearButtonVisible(true);
        HorizontalLayout hl = new HorizontalLayout(iosBundle, executeButton);
        hl.setAlignItems(FlexComponent.Alignment.BASELINE);
        iosPackageTypeComboBox = new ComboBox<>("iOS Package Type");
        iosPackageTypeComboBox.setItems(IOSPackageType.values());
        iosPackageTypeComboBox.setItemLabelGenerator(IOSPackageType::name);
        iosPackageTypeComboBox.setValue(IOSPackageType.ROOT);
        HorizontalLayout summary = new HorizontalLayout();
        summary.setSpacing(false);
        summary.add(new Text("iOS Tuner"));
        FormLayout content = new FormLayout();
        content.add(
            iosPackageTypeComboBox,
            hl
        );
        iosExplorerTuner = new Details(summary, content);
        iosExplorerTuner.setVisible(false);
        return iosExplorerTuner;
    }

    private VerticalLayout prepareAppsGrid() {
        Button updateButton = new Button("Update", new Icon(VaadinIcon.REFRESH));
        updateButton.setIconAfterText(false);
        updateButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        updateButton.addClickListener(
            click -> {
                fireEvent(new UpdateAppsListEvent(this, device));
            }
        );
        appsGrid.setColumns("name", "appPackage", "path", "appState");
        appsGrid.addColumn(AppDescription::getIsActive).setHeader("Active").setComparator(
            Comparator.comparing(AppDescription::getIsActive)
        );
        appsGrid.getColumns().forEach(col -> {
                col.setAutoWidth(true);
                col.setFlexGrow(0);
                col.setResizable(true);
            }
        );
        appsLayout = new VerticalLayout(updateButton, appsGrid);
        appsLayout.addClassName("shellCmd");
        return appsLayout;
    }

    private void executeShellCmd() {
        fireEvent(new ExecuteShellEvent(this, device, shellRequest));
    }

    public void setNewDataProviderFileExplorer(HierarchicalDataProvider newDataProvider) {
        dataProvider = newDataProvider;
        fileExplorerGrid.setDataProvider(dataProvider);
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

    public static class DownloadEvent extends DeviceFormEvent {
        private DeviceDirectoryElement deviceDirectoryElement;
        private Dialog dialog;
        private IOSPackageType iosPackageType;

        DownloadEvent(
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

    public static class ReinitTabsEvent extends DeviceFormEvent {
        private final Tab currentTab;
        private final Details iosExplorerTuner;
        private final Grid fileExplorerGrid;
        private final VerticalLayout shellCmdLayout;
        private final VerticalLayout appsLayout;

        ReinitTabsEvent(
            DeviceForm source,
            Device device,
            Tab tab,
            Details iosExplorerTuner,
            Grid fileExplorerGrid,
            VerticalLayout shellCmdLayout,
            VerticalLayout appsLayout
        ) {
            super(source, device);
            this.currentTab = tab;
            this.fileExplorerGrid = fileExplorerGrid;
            this.iosExplorerTuner = iosExplorerTuner;
            this.shellCmdLayout = shellCmdLayout;
            this.appsLayout = appsLayout;
        }

        public Tab getCurrentTab() {
            return currentTab;
        }

        public Details getIosExplorerTuner() {
            return iosExplorerTuner;
        }


        public Grid getFileExplorerGrid() {
            return fileExplorerGrid;
        }

        public VerticalLayout getShellCmdLayout() {
            return shellCmdLayout;
        }

        public VerticalLayout getAppsLayout() {
            return appsLayout;
        }
    }

    public static class UpdateAppsListEvent extends DeviceFormEvent {

        protected UpdateAppsListEvent(DeviceForm source, Device device) {
            super(source, device);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
