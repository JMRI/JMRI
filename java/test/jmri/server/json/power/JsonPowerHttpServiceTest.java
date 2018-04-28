package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
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
 * @author Randall Wood
 */
public class JsonPowerHttpServiceTest {

    private final static Logger log = LoggerFactory.getLogger(JsonPowerHttpServiceTest.class);

    @Test
    public void testCtorSuccess() {
        JsonPowerHttpService service = new JsonPowerHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    @Test
    public void testDoGet() throws JmriException {
        JsonPowerHttpService service = new JsonPowerHttpService(new ObjectMapper());
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        try {
            power.setPower(PowerManager.UNKNOWN);
            result = service.doGet(JsonPowerServiceFactory.POWER, null, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JsonPowerServiceFactory.POWER, result.path(JSON.TYPE).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            power.setPower(PowerManager.ON);
            result = service.doGet(JsonPowerServiceFactory.POWER, null, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            power.setPower(PowerManager.OFF);
            result = service.doGet(JsonPowerServiceFactory.POWER, null, Locale.ENGLISH);
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            log.error("Threw", ex);
            Assert.fail("Unexpected Exception");
        }
    }

    @Test
    public void testDoPost() throws JmriException {
        ObjectMapper mapper = new ObjectMapper();
        JsonPowerHttpService service = new JsonPowerHttpService(mapper);
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        JsonNode message;
        try {
            power.setPower(PowerManager.UNKNOWN);
            message = mapper.createObjectNode().put(JSON.STATE, JSON.ON);
            result = service.doPost(JsonPowerServiceFactory.POWER, null, message, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.ON, power.getPower());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
            message = mapper.createObjectNode().put(JSON.STATE, JSON.OFF);
            result = service.doPost(JsonPowerServiceFactory.POWER, null, message, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            Assert.assertNotNull(result);
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            message = mapper.createObjectNode().put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonPowerServiceFactory.POWER, null, message, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
            message = mapper.createObjectNode().put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonPowerServiceFactory.POWER, null, message, Locale.ENGLISH);
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
            (new JsonPowerHttpService(new ObjectMapper())).doPut(JsonPowerServiceFactory.POWER, null, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    @Test
    public void testDoGetList() {
        try {
            JsonNode result = (new JsonPowerHttpService(new ObjectMapper())).doGetList(JsonPowerServiceFactory.POWER, Locale.ENGLISH);
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
            (new JsonPowerHttpService(new ObjectMapper())).doDelete(JsonPowerServiceFactory.POWER, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugPowerManager();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
