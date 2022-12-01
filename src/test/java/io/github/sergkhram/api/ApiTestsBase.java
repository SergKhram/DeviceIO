package io.github.sergkhram.api;

import io.github.sergkhram.data.repository.DeviceRepository;
import io.github.sergkhram.data.repository.HostRepository;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
        "grpc.server.port=-1"
    }
)
@DirtiesContext(classMode = BEFORE_CLASS)
@Testcontainers
public abstract class ApiTestsBase {
    @Autowired
    HostRepository hostRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @MockBean
    IdbManager idbManager;

    @MockBean
    AdbManager adbManager;

    @Autowired
    private TestRestTemplate restTemplate;

    protected String getBaseUrl() {
        return restTemplate.getRootUri() + "/api";
    }

    @Container
    public static MongoDBContainer container = new MongoDBContainer(mongoContainer);

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }
}
