package jmri.server.json.turnout;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonServiceFactoryTestBase;

/**
 * @author Randall Wood Copyright 2020
 */
public class JsonTurnoutServiceFactoryTest extends JsonServiceFactoryTestBase<JsonTurnoutHttpService, JsonTurnoutSocketService> {

    @Override
    @Test
    public void testGetTypesV5() {
        assertThat(factory.getTypes(JSON.V5)).containsExactly(JsonTurnout.TURNOUT, JsonTurnout.TURNOUTS);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = new JsonTurnoutServiceFactory();
    }
}
