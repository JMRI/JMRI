package jmri.server.json.throttle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonServiceFactoryTestBase;

/**
 * @author Randall Wood Copyright 2020
 */
public class JsonThrottleServiceFactoryTest extends JsonServiceFactoryTestBase<JsonThrottleHttpService, JsonThrottleSocketService> {

    @Override
    @Test
    public void testGetTypesV5() {
        assertThat(factory.getTypes(JSON.V5)).containsExactly(JsonThrottle.THROTTLE);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        factory = new JsonThrottleServiceFactory();
    }
}
