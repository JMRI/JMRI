package jmri.server.json.light;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.implementation.AbstractLight;

/**
 *
 * @author Paul Bender
 * @author Randall Wood Copyright 2018
 */
public class JsonLightHttpServiceTest extends JsonHttpServiceTestBase {

    @Test
    public void testDoGet() throws JmriException, IOException {
        JsonLightHttpService service = new JsonLightHttpService(mapper);
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
        JsonNode result;
        try {
            result = service.doGet(JsonLight.LIGHT, "IL1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JsonLight.LIGHT, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IL1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.ON);
            result = service.doGet(JsonLight.LIGHT, "IL1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.OFF);
            result = service.doGet(JsonLight.LIGHT, "IL1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.INCONSISTENT);
            result = service.doGet(JsonLight.LIGHT, "IL1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.INCONSISTENT, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.UNKNOWN);
            result = service.doGet(JsonLight.LIGHT, "IL1", service.getObjectMapper().createObjectNode(), locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPost() throws JmriException, IOException {
        JsonLightHttpService service = new JsonLightHttpService(mapper);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        try {
            // set off
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
            JsonNode result = service.doPost(JsonLight.LIGHT, "IL1", message, locale);
            Assert.assertEquals(Light.OFF, light1.getState());
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
            result = service.doPost(JsonLight.LIGHT, "IL1", message, locale);
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown - remains on
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonLight.LIGHT, "IL1", message, locale);
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonLight.LIGHT, "IL1", message, locale);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPut() throws IOException {
        JsonLightHttpService service = new JsonLightHttpService(mapper);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        try {
            // add a light
            Assert.assertNull(manager.getLight("IL1"));
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, Light.OFF);
            JsonNode result = service.doPut(JsonLight.LIGHT, "IL1", message, locale);
            Assert.assertNotNull(result);
            this.validate(result);
            Assert.assertNotNull(manager.getLight("IL1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            JsonLightHttpService service = new JsonLightHttpService(mapper);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            JsonNode result = service.doGetList(JsonLight.LIGHT, mapper.createObjectNode(), locale);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideLight("IL1");
            manager.provideLight("IL2");
            result = service.doGetList(JsonLight.LIGHT, mapper.createObjectNode(), locale);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
            this.validate(result);
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonLightHttpService(mapper)).doDelete(JsonLight.LIGHT, "", locale);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
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
