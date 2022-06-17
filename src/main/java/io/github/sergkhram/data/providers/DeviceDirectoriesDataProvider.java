package io.github.sergkhram.data.providers;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import io.github.sergkhram.data.adb.AdbManager;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;

import java.util.List;
import java.util.stream.Stream;

public class DeviceDirectoriesDataProvider extends AbstractBackEndHierarchicalDataProvider<DeviceDirectoryElement, Void> {
    Device device;
    AdbManager adbManager;

    public DeviceDirectoriesDataProvider(Device device, AdbManager adbManager) {
        this.device = device;
        this.adbManager = adbManager;
    }

    @Override
    public int getChildCount(final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery) {
        DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent!=null
            ? getChildrenElements(parent).size()
            : adbManager.getListFiles(device, "/").size();
    }

    @Override
    public boolean hasChildren(final DeviceDirectoryElement deviceDirectoryElement) {
        return deviceDirectoryElement != null
            ? getChildrenElements(deviceDirectoryElement).size() > 0
            : adbManager.getListFiles(device, "/").size() > 0;
    }

    @Override
    protected Stream<DeviceDirectoryElement> fetchChildrenFromBackEnd(final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery) {
        final DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent!=null
            ? getChildrenElements(parent).stream()
            : adbManager.getListFiles(device, "/").stream();
    }

    private List<DeviceDirectoryElement> getChildrenElements(DeviceDirectoryElement deviceDirectoryElement) {
        String parentPath = deviceDirectoryElement.path.equals("/") ? "/" : deviceDirectoryElement.path + "/";
        return (deviceDirectoryElement.name.equals(".") || deviceDirectoryElement.name.equals(".."))
            ? List.of()
            : adbManager.getListFiles(device, parentPath + deviceDirectoryElement.name);
    }
}
