package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.managers.Manager;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class HostRequestsService {
    @Autowired
    CrmService crmService;
    List<Manager> managers;

    public HostRequestsService(
        AdbManager adbManager,
        IdbManager idbManager
    ) {
        managers = List.of(adbManager, idbManager);
    }

    public Host getHostInfo(String id)
        throws NoSuchElementException, IllegalArgumentException
    {
        return crmService.getHostById(id);
    }

    public List<Host> getHostsList(String stringFilter)
        throws NoSuchElementException, IllegalArgumentException
    {
        if(stringFilter == null) stringFilter = "";
        return crmService.findAllHosts(stringFilter);
    }

    public Host saveHost(Host host) {
        crmService.saveHost(host);
        return crmService.findAllHosts(host.getName()).get(0);
    }

    public void deleteHost(Host host) {
        crmService.deleteHost(host);
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

    public Host updateHostState(Host host) throws IOException {
        InetAddress address = InetAddress.getByName(host.getAddress());
        Boolean savedHostState = host.getIsActive();
        Boolean currentHostState = address.isReachable(5000);
        if(!savedHostState.equals(currentHostState)) {
            host.setIsActive(currentHostState);
            saveHost(host);
        }
        return host;
    }
}
