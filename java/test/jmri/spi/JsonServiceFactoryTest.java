package jmri.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.DataOutputStream;
import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonSocketService;
import jmri.util.JUnitUtil;

/**
 * Test that JsonServiceFactory classes adhere to contract.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonServiceFactoryTest {

    /**
     * Test that every published service factory creates valid objects.
     */
    @Test
    public void testJsonServiceFactories() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        @SuppressWarnings("rawtypes")
        ServiceLoader<JsonServiceFactory> loader = ServiceLoader.load(JsonServiceFactory.class);
        assertThat(loader.iterator().hasNext()).isTrue();
        loader.forEach((factory) -> {
            JSON.VERSIONS.forEach(version -> {
                // verify factory is well behaved
                JsonSocketService<?> socket = factory.getSocketService(connection, version);
                JsonHttpService http = factory.getHttpService(connection.getObjectMapper(), version);
                assertThat(socket).isNotNull();
                assertThat(http).isNotNull();
                assertThat(factory.getTypes(version)).isNotNull();
                assertThat(factory.getSentTypes(version)).isNotNull();
                assertThat(factory.getReceivedTypes(version)).isNotNull();
                // verify socket service constructors are populating finals correctly
                assertThat(socket.getConnection()).isEqualTo(connection);
                assertThat(socket.getHttpService()).isExactlyInstanceOf(http.getClass());
                // verify HTTP service constructors are populating finals correctly
                assertThat(http.getObjectMapper()).isEqualTo(connection.getObjectMapper());
            });
        });
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
