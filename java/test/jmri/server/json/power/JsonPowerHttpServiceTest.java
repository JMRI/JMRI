package jmri.server.json.power;

import apps.tests.Log4JFixture;
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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonPowerHttpServiceTest extends TestCase {

    private final static Logger log = LoggerFactory.getLogger(JsonPowerHttpServiceTest.class);

    public void testCtorSuccess() {
        JsonPowerHttpService service = new JsonPowerHttpService(new ObjectMapper());
        Assert.assertNotNull(service);
    }

    public void testDoGet() throws JmriException {
        JsonPowerHttpService service = new JsonPowerHttpService(new ObjectMapper());
        PowerManager power = InstanceManager.getDefault(PowerManager.class);
        JsonNode result;
        try {
            power.setPower(PowerManager.UNKNOWN);
            result = service.doGet(JsonPowerServiceFactory.POWER, null, Locale.ENGLISH);
            Assert.assertNotNull(result);
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

    public void testDoPut() {
        try {
            (new JsonPowerHttpService(new ObjectMapper())).doPut(JsonPowerServiceFactory.POWER, null, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    public void testDoGetList() {
        try {
            (new JsonPowerHttpService(new ObjectMapper())).doGetList(JsonPowerServiceFactory.POWER, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    public void testDelete() {
        try {
            (new JsonPowerHttpService(new ObjectMapper())).doDelete(JsonPowerServiceFactory.POWER, null, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
            return;
        }
        Assert.fail("Did not throw expected error.");
    }
    
    // from here down is testing infrastructure
    public JsonPowerHttpServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonPowerHttpServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonPowerHttpServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugPowerManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
