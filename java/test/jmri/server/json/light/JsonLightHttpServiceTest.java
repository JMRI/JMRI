package jmri.server.json.light;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonLightHttpServiceTest {

    @Test
    public void testDoGet() throws JmriException, IOException {
        JsonLightHttpService service = new JsonLightHttpService(new ObjectMapper());
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        JsonNode result;
        JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        try {
            result = service.doGet(JsonLight.LIGHT, "IL1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JsonLight.LIGHT, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IL1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.ON);
            result = service.doGet(JsonLight.LIGHT, "IL1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            light1.setState(Light.OFF);
            result = service.doGet(JsonLight.LIGHT, "IL1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPost() throws JmriException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonLightHttpService service = new JsonLightHttpService(mapper);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        Light light1 = manager.provideLight("IL1");
        JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        try {
            // set off
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.OFF);
            JsonNode result = service.doPost(JsonLight.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertEquals(Light.OFF, light1.getState());
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.ON);
            result = service.doPost(JsonLight.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown - remains on
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonLight.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertEquals(Light.ON, light1.getState());
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonLight.LIGHT, "IL1", message, Locale.ENGLISH);
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
        ObjectMapper mapper = new ObjectMapper();
        JsonLightHttpService service = new JsonLightHttpService(mapper);
        LightManager manager = InstanceManager.getDefault(LightManager.class);
        JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        try {
            // add a light
            Assert.assertNull(manager.getLight("IL1"));
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IL1").put(JSON.STATE, Light.OFF);
            JsonNode result = service.doPut(JsonLight.LIGHT, "IL1", message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertNotNull(manager.getLight("IL1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonLightHttpService service = new JsonLightHttpService(mapper);
            LightManager manager = InstanceManager.getDefault(LightManager.class);
            JsonNode result = service.doGetList(JsonLight.LIGHT, Locale.ENGLISH);
            JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
            Assert.assertNotNull(result);
            Assert.assertEquals(0, result.size());
            manager.provideLight("IL1");
            manager.provideLight("IL2");
            result = service.doGetList(JsonLight.LIGHT, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonLightHttpService(new ObjectMapper())).doDelete(JsonLight.LIGHT, "", Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
