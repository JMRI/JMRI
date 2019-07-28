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
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JsonNode result = service.doGet(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), locale, 42);
        this.validate(result);
        Assert.assertNotNull(result);
        Assert.assertEquals(JsonPowerServiceFactory.POWER, result.path(JSON.TYPE).asText());
        Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.ON);
        result = service.doGet(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), locale, 42);
        this.validate(result);
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        power.setPower(PowerManager.OFF);
        result = service.doGet(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), locale, 42);
        this.validate(result);
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Test
    public void testDoPost() throws JmriException, JsonException {
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        JsonNode message;
        power.setPower(PowerManager.UNKNOWN);
        message = mapper.createObjectNode().put(JSON.STATE, JSON.ON);
        result = service.doPost(JsonPowerServiceFactory.POWER, "", message, locale, 42);
        this.validate(result);
        Assert.assertEquals(PowerManager.ON, power.getPower());
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        message = mapper.createObjectNode().put(JSON.STATE, JSON.OFF);
        result = service.doPost(JsonPowerServiceFactory.POWER, "", message, locale, 42);
        this.validate(result);
        Assert.assertEquals(PowerManager.OFF, power.getPower());
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        message = mapper.createObjectNode().put(JSON.STATE, JSON.UNKNOWN);
        result = service.doPost(JsonPowerServiceFactory.POWER, "", message, locale, 42);
        this.validate(result);
        Assert.assertEquals(PowerManager.OFF, power.getPower());
        Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        message = mapper.createObjectNode().put(JSON.STATE, 42); // Invalid value
        JsonException exception = null;
        try {
            service.doPost(JsonPowerServiceFactory.POWER, "", message, locale, 42);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertEquals(PowerManager.OFF, power.getPower());
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
    }

    @Test
    public void testDoPut() {
        try {
            service.doPut(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
        }
    }

    @Test
    public void testDoGetList() throws JsonException {
        JsonNode result = service.doGetList(JsonPowerServiceFactory.POWER, NullNode.getInstance(), locale, 0);
        this.validate(result);
        Assert.assertTrue(result.isArray());
        Assert.assertEquals(1, result.size());
    }

    @Test
    @Override
    public void testDoDelete() {
        try {
            service.doDelete(JsonPowerServiceFactory.POWER, "", NullNode.getInstance(), locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
        }
    }

    @Before
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

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
