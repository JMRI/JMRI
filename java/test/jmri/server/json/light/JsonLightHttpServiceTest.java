package jmri.server.json.light;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import jmri.implementation.AbstractLight;

/**
 *
 * @author Paul Bender
 * @author Randall Wood Copyright 2018
 */
public class JsonLightHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Light, JsonLightHttpService> {

    @Test
    @Override
    public void testDoGet() throws JmriException, IOException, JsonException {
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = new AbstractLight("IL1") {
            // allow setting of "illegal" states for testing
            @Override
            public void setState(int newState) {
                if (newState == Light.ON && newState == Light.OFF) {
                    // if ON or OFF allow full transition to occur
                    super.setState(newState);
                } else {
                    // do the state change in the hardware
                    doNewState(mState, newState); // old state, new state
                    // change value and tell listeners
                    notifyStateChange(mState, newState);
                }
            }
        };
        manager.register(light1);
        JsonNode result = service.doGet(JsonLight.LIGHT, "IL1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JsonLight.LIGHT, result.path(JSON.TYPE).asText());
        assertEquals("IL1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        light1.setState(Light.ON);
        result = service.doGet(JsonLight.LIGHT, "IL1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        light1.setState(Light.OFF);
        result = service.doGet(JsonLight.LIGHT, "IL1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        light1.setState(Light.INCONSISTENT);
        result = service.doGet(JsonLight.LIGHT, "IL1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt());
        light1.setState(Light.UNKNOWN);
        result = service.doGet(JsonLight.LIGHT, "IL1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Test
    public void testDoPost() throws JmriException, IOException, JsonException {
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        // set off
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
        JsonNode result = service.doPost(JsonLight.LIGHT, "IL1", message, locale, 0);
        assertEquals(Light.OFF, light1.getState());
        validate(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set on
        message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
        result = service.doPost(JsonLight.LIGHT, "IL1", message, locale, 0);
        assertEquals(Light.ON, light1.getState());
        validate(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set unknown - remains on
        message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonLight.LIGHT, "IL1", message, locale, 0);
        assertEquals(Light.ON, light1.getState());
        validate(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // set invalid state
        message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // Invalid value
        try {
            service.doPost(JsonLight.LIGHT, "IL1", message, locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
        assertEquals(Light.ON, light1.getState());
    }

    @Test
    public void testDoPut() throws IOException, JsonException {
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        // add a light
        assertNull(manager.getLight("IL1"));
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, Light.OFF);
        JsonNode result = service.doPut(JsonLight.LIGHT, "IL1", message, locale, 0);
        validate(result);
        assertNotNull(manager.getLight("IL1"));
    }

    @Test
    public void testDoGetList() throws JsonException {
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        JsonNode result = service.doGetList(JsonLight.LIGHT, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(0, result.size());
        manager.provideLight("IL1");
        manager.provideLight("IL2");
        result = service.doGetList(JsonLight.LIGHT, NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(2, result.size());
        this.validate(result);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonLightHttpService(mapper);
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
