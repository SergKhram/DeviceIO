package io.github.sergkhram.api;

import io.github.sergkhram.data.repository.HostRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
        "grpc.server.port=-1"
    }
)
@DirtiesContext
public abstract class ApiTestsBase {
    @Autowired
    HostRepository hostRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    protected String getBaseUrl() {
        return restTemplate.getRootUri() + "/api";
    }
}
