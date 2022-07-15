package io.github.sergkhram.ui.views;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.github.sergkhram.ui.views.list.DevicesListView;
import io.github.sergkhram.ui.views.list.HostsListView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("DeviceIO");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
            new DrawerToggle(),
            logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.getThemeList().set("dark", true);
        header.addClassNames("py-0", "px-m");
        addToNavbar(header);

    }

    private void createDrawer() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        addToDrawer(tabs);
        setDrawerOpened(false);
    }

    private Component[] createMenuItems() {
        return new Tab[]{
            createTab("Hosts", HostsListView.class, VaadinIcon.DESKTOP.create()),
            createTab("Devices", DevicesListView.class, VaadinIcon.TABLET.create()),
            createTab("Settings", SettingsView.class, VaadinIcon.COG.create())
        };
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget, Icon icon) {
        final Tab tab = new Tab(icon);
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }
}
