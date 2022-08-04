package io.github.sergkhram.grpc;

import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.repository.HostRepository;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import io.github.sergkhram.proto.*;
import io.github.sergkhram.utils.Const;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.grpc.AllureGrpc;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static io.github.sergkhram.Generator.*;
import static io.github.sergkhram.grpc.converters.ProtoConverter.convertHostToHostProto;
import static io.github.sergkhram.grpc.converters.ProtoConverter.convertHostsToHostsProto;
import static io.github.sergkhram.utils.CustomAssertions.assertWithAllure;


@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "grpc.server.port=9091",
        "grpc.client.myClient.address=static://127.0.0.1:9091",
        "grpc.client.myClient.negotiationType=PLAINTEXT"
    }
)
@ImportAutoConfiguration({
    GrpcClientAutoConfiguration.class})
@DirtiesContext
@Epic("DeviceIO")
@Feature("gRPC")
@Story("Hosts")
public class HostsGrpcTest {

    @GrpcClient("myClient")
    private HostsServiceGrpc.HostsServiceBlockingStub hostService;

    @Autowired
    HostRepository hostRepository;

    @MockBean
    IdbManager idbManager;

    @MockBean
    AdbManager adbManager;

    @BeforeEach
    public void beforeTest() {
        hostRepository.deleteAll();
        hostService = hostService.withInterceptors(new AllureGrpc(), new LogGrpcInterceptor());
    }

    @Test
    @DirtiesContext
    @DisplayName("Check get hosts grpc request")
    public void checkHostsListRequest() {
        GetHostsListRequest request = GetHostsListRequest.newBuilder()
            .setStringFilter("")
            .build();
        GetHostsListResponse response = hostService.getHostsListRequest(request);
        Assertions.assertNotNull(response);
        assertWithAllure(
            GetHostsListResponse.getDefaultInstance().getHostsList(),
            response.getHostsList(),
            true
        );
        hostRepository.saveAll(generateHosts(100));
        List<Host> hosts = hostRepository.findAll();
        response = hostService.getHostsListRequest(request);
        assertWithAllure(
            convertHostsToHostsProto(hosts),
            response.getHostsList(),
            true
        );
    }

    @Test
    @DirtiesContext
    @DisplayName("Check get host info by id grpc request")
    public void checkHostInfoTest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        HostId request = HostId.newBuilder()
            .setId(host.getId().toString())
            .build();
        HostProto response = hostService.getHostRequest(request);
        assertWithAllure(
            convertHostToHostProto(host),
            response,
            true
        );
    }

    @Test
    @DisplayName("Check post host grpc request")
    public void checkCreateHostRequest() {
        Host host = generateHosts(1).get(0);
        HostProto response = hostService.postHostRequest(
            PostHostRequest.newBuilder()
                .setName(host.getName())
                .setAddress(host.getAddress())
                .build()
        );

        String id = response.getId();
        host.setId(UUID.fromString(id));
        assertWithAllure(
            convertHostToHostProto(host),
            response,
            true
        );
        response = hostService.getHostRequest(
            HostId.newBuilder().setId(id).build()
        );
        assertWithAllure(
            convertHostToHostProto(host),
            response,
            true
        );
    }

    @Test
    @DisplayName("Check update host grpc request")
    public void checkUpdateHostRequest() {
        Host host = generateHosts(1).get(0);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId().toString();
        host.setName(generateRandomString());
        host.setAddress(generateRandomString());
        host.setPort(generateRandomInt(0, 65535));

        HostProto response = hostService.updateHostRequest(
            UpdateHostRequest.newBuilder()
                .setId(id)
                .setName(host.getName())
                .setAddress(host.getAddress())
                .setPort(host.getPort())
                .build()
        );

        host.setId(UUID.fromString(id));
        assertWithAllure(
            convertHostToHostProto(host),
            response,
            true
        );

        response = hostService.getHostRequest(
            HostId.newBuilder().setId(id).build()
        );
        assertWithAllure(
            convertHostToHostProto(host),
            response,
            true
        );
    }

    @Test
    @DisplayName("Check delete host grpc request")
    public void checkDeleteHostRequest() {
        Host host = generateHosts(1).get(0);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId().toString();
        hostService.deleteHostRequest(HostId.newBuilder().setId(id).build());

        StatusRuntimeException e = Assertions.assertThrows(
            io.grpc.StatusRuntimeException.class,
            () -> hostService.getHostRequest(HostId.newBuilder().setId(id).build())
        );
        assertWithAllure(
            "UNKNOWN: No value present",
            e.getLocalizedMessage()
        );
    }

    @Test
    @DisplayName("Check connect host grpc request")
    public void checkConnectHostRequest() {
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress("localhost");
        host.setPort(65535);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId().toString();
        Mockito.doNothing().when(this.idbManager).connectToHost("localhost", 65535);
        Mockito.doNothing().when(this.adbManager).connectToHost("localhost", 65535);
        hostService.postHostConnectionRequest(HostId.newBuilder().setId(id).build());
    }

    @Test
    @DisplayName("Check disconnect host grpc request")
    public void checkDisconnectHostRequest() {
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress("localhost");
        host.setPort(65535);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId().toString();
        Mockito.doNothing().when(this.idbManager).disconnectHost("localhost", 65535);
        Mockito.doNothing().when(this.adbManager).disconnectHost("localhost", 65535);
        hostService.postHostDisconnectionRequest(HostId.newBuilder().setId(id).build());
    }

    @Test
    @DisplayName("Check update host state grpc request")
    public void checkUpdateHostStateRequest() {
        Host host = new Host();
        host.setName(generateRandomString());
        host.setAddress(Const.LOCAL_HOST);
        Host savedHost = hostRepository.save(host);
        String id = savedHost.getId().toString();
        hostService.getUpdateHostStateWithDeviceRemoval(
            UpdateHostStateRequest.newBuilder()
                .setId(id)
                .setDeleteDevices(false)
                .build()
        );
        HostProto response = hostService.getHostRequest(HostId.newBuilder().setId(id).build());
        assertWithAllure(
            true,
            response.getIsActive()
        );
    }
}
