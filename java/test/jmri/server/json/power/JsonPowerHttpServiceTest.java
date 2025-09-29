package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender
 * @author Randall Wood Copyright 2018, 2019
 */
public class JsonPowerHttpServiceTest extends JsonHttpServiceTestBase<JsonPowerHttpService> {

    @Test
    public void testDoGet() throws JmriException, JsonException {
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        power.setPower(PowerManager.UNKNOWN);
        JsonNode result = service.doGet(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        this.validate(result);
        assertNotNull(result);
        assertEquals(JsonPowerServiceFactory.POWER, result.path(JSON.TYPE).asText());
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.ON);
        result = service.doGet(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        this.validate(result);
        assertNotNull(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.OFF);
        result = service.doGet(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        this.validate(result);
        assertNotNull(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        JsonNode message;
        power.setPower(PowerManager.UNKNOWN);
        message = mapper.createObjectNode().put(JSON.STATE, JSON.ON);
        result = service.doPost(JsonPowerServiceFactory.POWER, "", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        this.validate(result);
        assertEquals(PowerManager.ON, power.getPower());
        assertNotNull(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        message = mapper.createObjectNode().put(JSON.STATE, JSON.OFF);
        result = service.doPost(JsonPowerServiceFactory.POWER, "", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        this.validate(result);
        assertEquals(PowerManager.OFF, power.getPower());
        assertNotNull(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        message = mapper.createObjectNode().put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonPowerServiceFactory.POWER, "", message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        this.validate(result);
        assertEquals(PowerManager.OFF, power.getPower());
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        JsonNode messageEx = mapper.createObjectNode().put(JSON.STATE, 42); // Invalid value
        JsonException exception = assertThrows( JsonException.class, () ->
            service.doPost(JsonPowerServiceFactory.POWER, "", messageEx,
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(PowerManager.OFF, power.getPower());
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
    }

    @Test
    public void testDoPut() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
    }

    @Test
    public void testDoGetList() throws JsonException {
        JsonNode result = service.doGetList(JsonPowerServiceFactory.POWER, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        this.validate(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
    }

    @Test
    @Override
    public void testDoDelete() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonPowerHttpService(mapper);
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugPowerManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
