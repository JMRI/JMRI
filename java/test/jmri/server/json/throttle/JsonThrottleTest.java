package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.JsonNode;

import jmri.InstanceManager;
import jmri.Throttle;
import jmri.ThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.roster.JsonRoster;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import java.io.DataOutputStream;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonThrottleTest {

    @Test
    public void testGetThrottle() throws java.io.IOException, jmri.server.json.JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);

        // get with tested long address
        JsonNode data = connection.getObjectMapper().createObjectNode().put(JSON.ADDRESS, 1234);
        JsonThrottle t = JsonThrottle.getThrottle("42", data, service, 42);
        Assert.assertNotNull("exists", t);

        // get with tested short address
        data = connection.getObjectMapper().createObjectNode().put(JSON.ADDRESS, 42);
        service = new JsonThrottleSocketService(connection);
        t = JsonThrottle.getThrottle("42", data, service, 42);
        Assert.assertNotNull("exists", t);

        // get with tested unusable address
        data = connection.getObjectMapper().createObjectNode().put(JSON.ADDRESS, 100);
        service = new JsonThrottleSocketService(connection);
        try {
            t = JsonThrottle.getThrottle("42", data, service, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "The address 100 is invalid.", ex.getMessage());
        }
        JUnitAppender.assertWarnMessage("Address \"100\" is not a valid address.");

        // get with non-existent roster entry
        data = connection.getObjectMapper().createObjectNode().put(JsonRoster.ROSTER_ENTRY, 100);
        service = new JsonThrottleSocketService(connection);
        try {
            t = JsonThrottle.getThrottle("42", data, service, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Not Found", 404, ex.getCode());
            Assert.assertEquals("Error message",
                    "Unable to create throttle for roster entry 100. Perhaps it does not exist?", ex.getMessage());
        }
        JUnitAppender.assertWarnMessage("Roster entry \"100\" does not exist.");
    }

    /**
     * Test pressing and releasing all function keys in a throttle.
     * 
     * @throws JsonException for any unanticipated exceptions
     */
    @Test
    public void testFunctionKeys() throws JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);

        // get with tested long address
        JsonNode data = connection.getObjectMapper().createObjectNode().put(JSON.ADDRESS, 1234);
        JsonThrottle jsonThrottle = JsonThrottle.getThrottle("42", data, service, 42);
        Assert.assertNotNull("JsonThrottle exists", jsonThrottle);
        Throttle throttle = jsonThrottle.getThrottle();
        Assert.assertNotNull("has Throttle", throttle);

        Assert.assertFalse(throttle.getF0());
        data = connection.getObjectMapper().createObjectNode().put("F0", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF0());
        Assert.assertFalse(throttle.getF1());
        data = connection.getObjectMapper().createObjectNode().put("F1", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF1());
        Assert.assertFalse(throttle.getF2());
        data = connection.getObjectMapper().createObjectNode().put("F2", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF2());
        Assert.assertFalse(throttle.getF3());
        data = connection.getObjectMapper().createObjectNode().put("F3", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF3());
        Assert.assertFalse(throttle.getF4());
        data = connection.getObjectMapper().createObjectNode().put("F4", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF4());
        Assert.assertFalse(throttle.getF5());
        data = connection.getObjectMapper().createObjectNode().put("F5", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF5());
        Assert.assertFalse(throttle.getF6());
        data = connection.getObjectMapper().createObjectNode().put("F6", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF6());
        Assert.assertFalse(throttle.getF7());
        data = connection.getObjectMapper().createObjectNode().put("F7", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF7());
        Assert.assertFalse(throttle.getF8());
        data = connection.getObjectMapper().createObjectNode().put("F8", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF8());
        Assert.assertFalse(throttle.getF9());
        data = connection.getObjectMapper().createObjectNode().put("F9", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF9());
        Assert.assertFalse(throttle.getF10());
        data = connection.getObjectMapper().createObjectNode().put("F10", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF10());
        Assert.assertFalse(throttle.getF11());
        data = connection.getObjectMapper().createObjectNode().put("F11", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF11());
        Assert.assertFalse(throttle.getF12());
        data = connection.getObjectMapper().createObjectNode().put("F12", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF12());
        Assert.assertFalse(throttle.getF13());
        data = connection.getObjectMapper().createObjectNode().put("F13", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF13());
        Assert.assertFalse(throttle.getF14());
        data = connection.getObjectMapper().createObjectNode().put("F14", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF14());
        Assert.assertFalse(throttle.getF15());
        data = connection.getObjectMapper().createObjectNode().put("F15", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF15());
        Assert.assertFalse(throttle.getF16());
        data = connection.getObjectMapper().createObjectNode().put("F16", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF16());
        Assert.assertFalse(throttle.getF17());
        data = connection.getObjectMapper().createObjectNode().put("F17", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF17());
        Assert.assertFalse(throttle.getF18());
        data = connection.getObjectMapper().createObjectNode().put("F18", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF18());
        Assert.assertFalse(throttle.getF19());
        data = connection.getObjectMapper().createObjectNode().put("F19", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF19());
        Assert.assertFalse(throttle.getF20());
        data = connection.getObjectMapper().createObjectNode().put("F20", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF20());
        Assert.assertFalse(throttle.getF21());
        data = connection.getObjectMapper().createObjectNode().put("F21", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF21());
        Assert.assertFalse(throttle.getF22());
        data = connection.getObjectMapper().createObjectNode().put("F22", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF22());
        Assert.assertFalse(throttle.getF23());
        data = connection.getObjectMapper().createObjectNode().put("F23", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF23());
        Assert.assertFalse(throttle.getF24());
        data = connection.getObjectMapper().createObjectNode().put("F24", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF24());
        Assert.assertFalse(throttle.getF25());
        data = connection.getObjectMapper().createObjectNode().put("F25", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF25());
        Assert.assertFalse(throttle.getF26());
        data = connection.getObjectMapper().createObjectNode().put("F26", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF26());
        Assert.assertFalse(throttle.getF27());
        data = connection.getObjectMapper().createObjectNode().put("F27", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF27());
        Assert.assertFalse(throttle.getF28());
        data = connection.getObjectMapper().createObjectNode().put("F28", true);
        jsonThrottle.onMessage(Locale.ENGLISH, data, service);
        Assert.assertTrue(throttle.getF28());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugCommandStation();
        InstanceManager.store(new TestThrottleManager(), ThrottleManager.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * DebugThrottleManager instance that requires that addresses 0-99 be short,
     * and that addresses greater than 127 be long.
     */
    private static class TestThrottleManager extends DebugThrottleManager {

        /**
         * {@inheritDoc}
         *
         * @return false if address is less than 127; true otherwise
         */
        @Override
        public boolean canBeLongAddress(int address) {
            return address > 127;
        }

        /**
         * {@inheritDoc}
         * 
         * @return false if address is greater than 100; true otherwise
         */
        @Override
        public boolean canBeShortAddress(int address) {
            return address < 100;
        }
    }
}
