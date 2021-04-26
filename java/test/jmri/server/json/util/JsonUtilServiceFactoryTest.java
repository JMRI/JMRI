package jmri.server.json.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonServiceFactoryTestBase;

/**
 * @author Randall Wood Copyright 2020
 */
public class JsonUtilServiceFactoryTest extends JsonServiceFactoryTestBase<JsonUtilHttpService, JsonUtilSocketService> {

    @Override
    @Test
    public void testGetTypesV5() {
        assertThat(factory.getTypes(JSON.V5)).containsExactly(JSON.GOODBYE, JSON.HELLO, JSON.METADATA, JSON.NETWORK_SERVICE, JSON.NETWORK_SERVICES, JSON.NODE, JSON.PANEL, JSON.PANELS, JSON.RAILROAD, JSON.SYSTEM_CONNECTION, JSON.SYSTEM_CONNECTIONS, JSON.CONFIG_PROFILE, JSON.CONFIG_PROFILES, JSON.VERSION);
    }

    @Override
    @Test
    public void testGetReceivedTypesV5() {
        assertThat(factory.getReceivedTypes(JSON.V5)).containsExactly(JSON.LOCALE, JSON.PING);
    }

    @Override
    @Test
    public void testGetSentTypesV5() {
        assertThat(factory.getSentTypes(JSON.V5)).containsExactly(JsonException.ERROR, JSON.LIST, JSON.PONG);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = new JsonUtilServiceFactory();
    }
}
