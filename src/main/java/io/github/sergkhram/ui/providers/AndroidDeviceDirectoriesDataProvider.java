package io.github.sergkhram.ui.providers;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;

import java.util.List;
import java.util.stream.Stream;

public class AndroidDeviceDirectoriesDataProvider extends AbstractBackEndHierarchicalDataProvider<DeviceDirectoryElement, Void> {
    Device device;
    DeviceRequestsService deviceRequestsService;

    public AndroidDeviceDirectoriesDataProvider(Device device, DeviceRequestsService deviceRequestsService) {
        this.device = device;
        this.deviceRequestsService = deviceRequestsService;
    }

    @Override
    public int getChildCount(final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery) {
        DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent != null
            ? getChildrenElements(parent).size()
            : deviceRequestsService.getListFiles(device, "/", null).size();
    }

    @Override
    public boolean hasChildren(final DeviceDirectoryElement deviceDirectoryElement) {
        return deviceDirectoryElement != null
            ? getChildrenElements(deviceDirectoryElement).size() > 0
            : deviceRequestsService.getListFiles(device, "/", null).size() > 0;
    }

    @Override
    protected Stream<DeviceDirectoryElement> fetchChildrenFromBackEnd(
        final HierarchicalQuery<DeviceDirectoryElement, Void> hierarchicalQuery
    ) {
        final DeviceDirectoryElement parent = hierarchicalQuery.getParentOptional()
            .orElse(null);
        return parent != null
            ? getChildrenElements(parent).stream()
            : deviceRequestsService.getListFiles(device, "/", null).stream();
    }

    private List<DeviceDirectoryElement> getChildrenElements(DeviceDirectoryElement deviceDirectoryElement) {
        String parentPath = deviceDirectoryElement.path.equals("/") ? "/" : deviceDirectoryElement.path + "/";
        return (deviceDirectoryElement.name.equals(".") || deviceDirectoryElement.name.equals(".."))
            ? List.of()
            : deviceRequestsService.getListFiles(
                device, parentPath + deviceDirectoryElement.name, null
        );
    }
}
