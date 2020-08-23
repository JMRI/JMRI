package jmri.server.json.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonServiceFactoryTestBase;

public class JsonTimeServiceFactoryTest extends JsonServiceFactoryTestBase<JsonTimeHttpService, JsonTimeSocketService> {

    @Override
    @Test
    public void testGetTypesV5() {
        assertThat(factory.getTypes(JSON.V5)).containsExactly(JSON.TIME);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = new JsonTimeServiceFactory();
    }
}
