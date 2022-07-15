package io.github.sergkhram.ui.providers;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.logic.DeviceRequestsService;

import java.util.List;
import java.util.stream.Stream;

public class IOSDeviceDirectoriesDataProvider extends AbstractBackEndHierarchicalDataProvider<DeviceDirectoryElement, Void> {
    Device device;
    DeviceRequestsService deviceRequestsService;
    String bundle;
    IOSPackageType iosPackageType;

    public IOSDeviceDirectoriesDataProvider(
        Device device, DeviceRequestsService deviceRequestsService, String bundle, IOSPackageType iosPackageType
    ) {
        this.device = device;
        this.deviceRequestsService = deviceRequestsService;
        this.bundle = bundle;
        this.iosPackageType = iosPackageType;
    }

    @Override
    public int getChildCount(final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery) {
        DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent != null
            ? getChildrenElements(parent).size()
            : deviceRequestsService.getListFiles(device, bundle, iosPackageType).size();
    }

    @Override
    public boolean hasChildren(final DeviceDirectoryElement deviceDirectoryElement) {
        boolean hasChildren;
        if (deviceDirectoryElement != null) {
            hasChildren = getChildrenElements(deviceDirectoryElement).size() > 0;
            if (hasChildren) deviceDirectoryElement.isDirectory = true;
        } else {
            hasChildren = deviceRequestsService.getListFiles(device, bundle, iosPackageType).size() > 0;
        }
        return hasChildren;
    }

    @Override
    protected Stream<DeviceDirectoryElement> fetchChildrenFromBackEnd(
        final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery
    ) {
        final DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent != null
            ? getChildrenElements(parent).stream()
            : deviceRequestsService.getListFiles(device, bundle, iosPackageType).stream();
    }

    private List<DeviceDirectoryElement> getChildrenElements(DeviceDirectoryElement deviceDirectoryElement) {
        String parentPath = deviceDirectoryElement.path + "/";
        return deviceRequestsService.getListFiles(
            device, parentPath + deviceDirectoryElement.name, iosPackageType
        );
    }
}
