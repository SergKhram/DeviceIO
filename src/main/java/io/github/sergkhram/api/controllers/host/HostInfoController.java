package io.github.sergkhram.api.controllers.host;

import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.service.CrmService;
import kotlin.jvm.Throws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.github.sergkhram.data.converters.Converters.convertModelToJsonNode;

@RestController
@RequestMapping("/api")
@Slf4j
public class HostInfoController {
    CrmService service;

    public HostInfoController(CrmService service) {
        this.service = service;
    }

    @GetMapping(path = "/host/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getHostInfoRequest(
        @PathVariable("id") String id
    ) {
        try {
            Host host = getHostInfo(id);
            return ResponseEntity.ok().body(convertModelToJsonNode(host));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no host with id " + id);
        }
    }

    @GetMapping(path = "/hosts")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getHostsListRequest(
        @RequestParam(value = "stringFilter", required = false) String stringFilter
    ) {
        try {
            List<Host> hosts = getHostsList(stringFilter);
            return ResponseEntity.ok().body(convertModelToJsonNode(hosts));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/host")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postHostRequest(
        @RequestBody Host host
    ) {
        try {
            Host savedHost = saveHost(host);
            return ResponseEntity.ok().body(convertModelToJsonNode(savedHost));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(path = "/host/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> updateHostRequest(
        @PathVariable(value = "id") String id,
        @RequestBody Host host
    ) {
        try {
            host.setId(UUID.fromString(id));
            Host savedHost = saveHost(host);
            return ResponseEntity.ok().body(convertModelToJsonNode(savedHost));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(path = "/host/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> deleteHostRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Host host = getHostInfo(id);
            deleteHost(host);
            return ResponseEntity.accepted().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no host with id " + id);
        }
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
}
