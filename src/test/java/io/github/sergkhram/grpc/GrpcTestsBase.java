package io.github.sergkhram.grpc;

import io.github.sergkhram.data.repository.DeviceRepository;
import io.github.sergkhram.data.repository.HostRepository;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.github.sergkhram.utils.TestConst.mongoContainer;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

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
@DirtiesContext(classMode = BEFORE_CLASS)
@Testcontainers
public abstract class GrpcTestsBase {
    @Autowired
    HostRepository hostRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @MockBean
    IdbManager idbManager;

    @MockBean
    AdbManager adbManager;

    @Container
    public static MongoDBContainer container = new MongoDBContainer(mongoContainer);

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }
}
