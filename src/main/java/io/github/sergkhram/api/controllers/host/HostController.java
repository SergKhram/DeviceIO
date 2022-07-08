package io.github.sergkhram.api.controllers.host;

import io.github.sergkhram.api.logic.HostRequestsService;
import io.github.sergkhram.data.entity.Host;
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
public class HostController {
    HostRequestsService hostRequestsService;

    public HostController(HostRequestsService hostRequestsService) {
        this.hostRequestsService = hostRequestsService;
    }

    @GetMapping(path = "/host/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getHostRequest(
        @PathVariable("id") String id
    ) {
        try {
            Host host = hostRequestsService.getHostInfo(id);
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
            List<Host> hosts = hostRequestsService.getHostsList(stringFilter);
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
            Host savedHost = hostRequestsService.saveHost(host);
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
            Host savedHost = hostRequestsService.saveHost(host);
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
            Host host = hostRequestsService.getHostInfo(id);
            hostRequestsService.deleteHost(host);
            return ResponseEntity.accepted().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no host with id " + id);
        }
    }

    @PostMapping(path = "/host/{id}/connect")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postHostConnectionRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Host host = hostRequestsService.getHostInfo(id);
            hostRequestsService.connect(host);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/host/{id}/disconnect")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postHostDisconnectionRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Host host = hostRequestsService.getHostInfo(id);
            hostRequestsService.disconnect(host);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
