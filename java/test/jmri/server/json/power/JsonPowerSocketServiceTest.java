package jmri.server.json.power;

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
public class JsonPowerSocketServiceTest {

    private final static Logger log = LoggerFactory.getLogger(JsonPowerSocketServiceTest.class);

    @Test
    public void testPowerChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().readTree("{}");
            JsonPowerSocketService service = new JsonPowerSocketService(connection);
            PowerManager power = InstanceManager.getDefault(PowerManager.class);
            power.setPower(PowerManager.UNKNOWN);
            service.onMessage(JsonPowerServiceFactory.POWER, message, JSON.POST, Locale.ENGLISH);
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

    @Test
    public void testOnMessageChange() {
        try {
            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().readTree("{\"state\":2}"); // Power ON
            JsonPowerSocketService service = new JsonPowerSocketService(connection);
            PowerManager power = InstanceManager.getDefault(PowerManager.class);
            power.setPower(PowerManager.UNKNOWN);
            service.onMessage(JsonPowerServiceFactory.POWER, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.ON, power.getPower());
            message = connection.getObjectMapper().readTree("{\"state\":4}"); // Power OFF
            service.onMessage(JsonPowerServiceFactory.POWER, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.OFF, power.getPower());
            message = connection.getObjectMapper().readTree("{\"state\":0}"); // JSON Power UNKNOWN
            service.onMessage(JsonPowerServiceFactory.POWER, message, JSON.POST, Locale.ENGLISH);
            Assert.assertEquals(PowerManager.OFF, power.getPower()); // did not change
            message = connection.getObjectMapper().readTree("{\"state\":1}"); // JSON Invalid
            JsonException exception = null;
            try {
                service.onMessage(JsonPowerServiceFactory.POWER, message, JSON.POST, Locale.ENGLISH);
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

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugPowerManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
