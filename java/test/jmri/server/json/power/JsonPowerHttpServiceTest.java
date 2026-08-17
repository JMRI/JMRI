package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
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

    @Test
    public void testDoGetWithPrefix() throws JmriException, JsonException {
        // Capture original default PM before memo2 creation (store() makes new PM the default)
        PowerManager defaultPm = InstanceManager.getDefault(PowerManager.class);
        defaultPm.setPower(PowerManager.OFF);

        InternalSystemConnectionMemo memo2 = new InternalSystemConnectionMemo("J", "Juliet", false);
        PowerManager pm2 = memo2.get(PowerManager.class);
        assertNotNull(pm2);
        pm2.setPower(PowerManager.ON);

        // Restore original as default so no-prefix queries resolve to defaultPm
        InstanceManager.setDefault(PowerManager.class, defaultPm);

        // No prefix: uses default manager (OFF)
        JsonNode result = service.doGet(JsonPowerServiceFactory.POWER, "",
                mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        this.validate(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());

        // With prefix "J": uses memo2's manager (ON)
        ObjectNode dataWithPrefix = mapper.createObjectNode();
        dataWithPrefix.put(JSON.PREFIX, "J");
        result = service.doGet(JsonPowerServiceFactory.POWER, "", dataWithPrefix,
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        this.validate(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals("J", result.path(JSON.DATA).path(JSON.PREFIX).asText());

        // With unknown prefix: throws JsonException (HTTP 400)
        ObjectNode dataUnknown = mapper.createObjectNode();
        dataUnknown.put(JSON.PREFIX, "Z");
        JsonException ex = assertThrows(JsonException.class, () ->
                service.doGet(JsonPowerServiceFactory.POWER, "", dataUnknown,
                        new JsonRequest(locale, JSON.V5, JSON.GET, 0)));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
    }

    @Test
    public void testDoPostWithPrefix() throws JmriException, JsonException {
        // Capture original default PM before memo2 creation (store() makes new PM the default)
        PowerManager defaultPm = InstanceManager.getDefault(PowerManager.class);
        defaultPm.setPower(PowerManager.OFF);

        InternalSystemConnectionMemo memo2 = new InternalSystemConnectionMemo("J", "Juliet", false);
        PowerManager pm2 = memo2.get(PowerManager.class);
        assertNotNull(pm2);
        pm2.setPower(PowerManager.OFF);

        // Restore original as default so no-prefix queries resolve to defaultPm
        InstanceManager.setDefault(PowerManager.class, defaultPm);

        // Post ON to prefix "J": affects pm2, not default
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.PREFIX, "J");
        data.put(JSON.STATE, JSON.ON);
        JsonNode result = service.doPost(JsonPowerServiceFactory.POWER, "", data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 0));
        this.validate(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(PowerManager.ON, pm2.getPower());
        assertEquals(PowerManager.OFF, defaultPm.getPower());
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
