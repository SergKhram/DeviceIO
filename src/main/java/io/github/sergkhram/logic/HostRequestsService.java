package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.managers.Manager;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HostRequestsService {
    CrmService service;
    List<Manager> managers;

    public HostRequestsService(
        CrmService service,
        AdbManager adbManager,
        IdbManager idbManager
    ) {
        this.service = service;
        managers = List.of(adbManager, idbManager);
    }

    public Host getHostInfo(String id)
        throws NoSuchElementException, IllegalArgumentException
    {
        return service.getHostById(id);
    }

    public List<Host> getHostsList(String stringFilter)
        throws NoSuchElementException, IllegalArgumentException
    {
        if(stringFilter == null) stringFilter = "";
        return service.findAllHosts(stringFilter);
    }

    public Host saveHost(Host host) {
        service.saveHost(host);
        return service.findAllHosts(host.getName()).get(0);
    }

    public void deleteHost(Host host) {
        service.deleteHost(host);
    }

    public void connect(Host host) {
        managers
            .parallelStream()
            .forEach(
                it -> it.connectToHost(
                    host.getAddress(),
                    host.getPort()
                )
            );
    }

    public void disconnect(Host host) {
        managers
            .parallelStream()
            .forEach(
                it -> it.disconnectHost(
                    host.getAddress(),
                    host.getPort()
                )
            );
    }
}
