package io.github.sergkhram.grpc;

import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.repository.HostRepository;
import io.github.sergkhram.proto.*;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static io.github.sergkhram.Generator.generateHosts;
import static io.github.sergkhram.grpc.converters.ProtoConverter.convertHostToHostProto;
import static io.github.sergkhram.grpc.converters.ProtoConverter.convertHostsToHostsProto;


@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
        "grpc.server.port=9091",
        "grpc.client.myClient.address=static://127.0.0.1:9091",
        "grpc.client.myClient.negotiationType=PLAINTEXT"
    }
)
@ImportAutoConfiguration({
    GrpcClientAutoConfiguration.class})
@DirtiesContext
public class HostsGrpcTest {

    @GrpcClient("myClient")
    private HostsServiceGrpc.HostsServiceBlockingStub hostService;

    @Autowired
    HostRepository hostRepository;

    @BeforeEach
    public void beforeTest() {
        hostRepository.deleteAll();
    }

    @Test
    @DirtiesContext
    public void checkHostsListRequest() {
        GetHostsListRequest request = GetHostsListRequest.newBuilder()
            .setStringFilter("")
            .build();
        GetHostsListResponse response = hostService.getHostsListRequest(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(
            GetHostsListResponse.getDefaultInstance().getHostsList(),
            response.getHostsList()
        );
        hostRepository.saveAll(generateHosts(100));
        List<Host> hosts = hostRepository.findAll();
        response = hostService.getHostsListRequest(request);
        Assertions.assertEquals(
            convertHostsToHostsProto(hosts),
            response.getHostsList()
        );
    }

    @Test
    @DirtiesContext
    public void checkHostInfoTest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        HostId request = HostId.newBuilder()
            .setId(host.getId().toString())
            .build();
        HostProto response = hostService.getHostRequest(request);
        Assertions.assertEquals(
            convertHostToHostProto(host),
            response
        );
    }
}
