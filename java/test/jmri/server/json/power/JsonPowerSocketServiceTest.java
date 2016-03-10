package jmri.server.json.power;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.swingui.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonPowerSocketServiceTest extends TestCase {

    private final static Logger log = LoggerFactory.getLogger(JsonPowerSocketServiceTest.class);

    public void testCtorSuccess() {
        JsonPowerSocketService service = new JsonPowerSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testPowerChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().readTree("{}");
            JsonPowerSocketService service = new JsonPowerSocketService(connection);
            PowerManager power = InstanceManager.getDefault(PowerManager.class);
            power.setPower(PowerManager.UNKNOWN);
            service.onMessage(JsonPowerServiceFactory.POWER, message, Locale.ENGLISH);
            // TODO: test that service is listener in PowerManager
            Assert.assertEquals(JSON.UNKNOWN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            power.setPower(PowerManager.ON);
            Assert.assertEquals(JSON.ON, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            power.setPower(PowerManager.OFF);
            Assert.assertEquals(JSON.OFF, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt());
            service.onClose();
            // TODO: test that service is no longer a listener in PowerManager
        } catch (IOException | JmriException | JsonException ex) {
            log.error("testPowerChange threw", ex);
            Assert.fail("Unexpected Exception");
        }
    }

    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().readTree("{\"state\":2}"); // Power ON
            JsonPowerSocketService service = new JsonPowerSocketService(connection);
            PowerManager power = InstanceManager.getDefault(PowerManager.class);
            power.setPower(PowerManager.UNKNOWN);
            service.onMessage(JsonPowerServiceFactory.POWER, message, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.ON, power.getPower());
            message = connection.getObjectMapper().readTree("{\"state\":4}"); // Power OFF
            service.onMessage(JsonPowerServiceFactory.POWER, message, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            message = connection.getObjectMapper().readTree("{\"state\":0}"); // JSON Power UNKNOWN
            service.onMessage(JsonPowerServiceFactory.POWER, message, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.OFF, power.getPower()); // did not change
            message = connection.getObjectMapper().readTree("{\"state\":1}"); // JSON Invalid
            JsonException exception = null;
            try {
                service.onMessage(JsonPowerServiceFactory.POWER, message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(PowerManager.OFF, power.getPower()); // did not change
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (IOException | JmriException | JsonException ex) {
            log.error("testOnMessageChange threw", ex);
            Assert.fail("Unexpected Exception");
        }
    }

    // from here down is testing infrastructure
    public JsonPowerSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonPowerSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonPowerSocketServiceTest.class);

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
