package io.github.sergkhram.data.providers;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;

import java.util.List;
import java.util.stream.Stream;

public class IOSDeviceDirectoriesDataProvider extends AbstractBackEndHierarchicalDataProvider<DeviceDirectoryElement, Void> {
    Device device;
    IdbManager idbManager;
    String bundle;
    IOSPackageType iosPackageType;

    public IOSDeviceDirectoriesDataProvider(
        Device device, IdbManager idbManager, String bundle, IOSPackageType iosPackageType
    ) {
        this.device = device;
        this.idbManager = idbManager;
        this.bundle = bundle;
        this.iosPackageType = iosPackageType;
    }

    @Override
    public int getChildCount(final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery) {
        DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent!=null
            ? getChildrenElements(parent).size()
            : idbManager.getListFiles(device, bundle, iosPackageType).size();
    }

    @Override
    public boolean hasChildren(final DeviceDirectoryElement deviceDirectoryElement) {
        return deviceDirectoryElement != null
            ? getChildrenElements(deviceDirectoryElement).size() > 0
            : idbManager.getListFiles(device, bundle, iosPackageType).size() > 0;
    }

    @Override
    protected Stream<DeviceDirectoryElement> fetchChildrenFromBackEnd(
        final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery
    ) {
        final DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent!=null
            ? getChildrenElements(parent).stream()
            : idbManager.getListFiles(device, bundle, iosPackageType).stream();
    }

    private List<DeviceDirectoryElement> getChildrenElements(DeviceDirectoryElement deviceDirectoryElement) {
        String parentPath = deviceDirectoryElement.path + "/";
        return idbManager.getListFiles(device, parentPath + deviceDirectoryElement.name, iosPackageType);
    }
}
