package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender
 * @author Randall Wood Copyright 2018
 */
public class JsonPowerHttpServiceTest extends JsonHttpServiceTestBase {

    private final static Logger log = LoggerFactory.getLogger(JsonPowerHttpServiceTest.class);

    @Test
    public void testDoGet() throws JmriException {
        JsonPowerHttpService service = new JsonPowerHttpService(mapper);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        try {
            power.setPower(PowerManager.UNKNOWN);
            result = service.doGet(JsonPowerServiceFactory.POWER, "", mapper.createObjectNode(), locale);
            this.validate(result);
            Assert.assertNotNull(result);
            Assert.assertEquals(JsonPowerServiceFactory.POWER, result.path(JSON.TYPE).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            power.setPower(PowerManager.ON);
            result = service.doGet(JsonPowerServiceFactory.POWER, "", mapper.createObjectNode(), locale);
            this.validate(result);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            power.setPower(PowerManager.OFF);
            result = service.doGet(JsonPowerServiceFactory.POWER, "", mapper.createObjectNode(), locale);
            this.validate(result);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            log.error("Threw", ex);
            Assert.fail("Unexpected Exception");
        }
    }

    @Test
    public void testDoPost() throws JmriException {
        JsonPowerHttpService service = new JsonPowerHttpService(mapper);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        JsonNode message;
        try {
            power.setPower(PowerManager.UNKNOWN);
            message = mapper.createObjectNode().put(JSON.STATE, JSON.ON);
            result = service.doPost(JsonPowerServiceFactory.POWER, "", message, locale);
            this.validate(result);
            Assert.assertEquals(PowerManager.ON, power.getPower());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            message = mapper.createObjectNode().put(JSON.STATE, JSON.OFF);
            result = service.doPost(JsonPowerServiceFactory.POWER, "", message, locale);
            this.validate(result);
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            message = mapper.createObjectNode().put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonPowerServiceFactory.POWER, "", message, locale);
            this.validate(result);
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            message = mapper.createObjectNode().put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonPowerServiceFactory.POWER, "", message, locale);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            log.error("Threw", ex);
            Assert.fail("Unexpected Exception");
        }
    }

    @Test
    public void testDoPut() {
        try {
            (new JsonPowerHttpService(mapper)).doPut(JsonPowerServiceFactory.POWER, "", mapper.createObjectNode(), locale);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    @Test
    public void testDoGetList() {
        try {
            JsonNode result = (new JsonPowerHttpService(mapper)).doGetList(JsonPowerServiceFactory.POWER,
                    mapper.createObjectNode(), locale);
            this.validate(result);
            Assert.assertTrue(result.isArray());
            Assert.assertEquals(1, result.size());
        } catch (JsonException ex) {
            log.error("Threw", ex);
            Assert.fail("Unexpected Exception");
        }
    }

    @Test
    public void testDelete() {
        try {
            (new JsonPowerHttpService(mapper)).doDelete(JsonPowerServiceFactory.POWER, "", locale);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
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
