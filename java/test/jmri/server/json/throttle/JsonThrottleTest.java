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

import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotNull( t, "exists");

        // get with tested short address
        data = connection.getObjectMapper().createObjectNode().put(JSON.ADDRESS, 42);
        service = new JsonThrottleSocketService(connection);
        t = JsonThrottle.getThrottle("42", data, service, 42);
        assertNotNull( t, "exists");

        // get with tested unusable address
        JsonNode dataEx1 = connection.getObjectMapper().createObjectNode().put(JSON.ADDRESS, 100);
        JsonThrottleSocketService serviceEx1 = new JsonThrottleSocketService(connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            assertNotNull(JsonThrottle.getThrottle("42", dataEx1, serviceEx1, 42)),
            "Expected exception not thrown");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "The address 100 is invalid.", ex.getMessage(), "Error message");
        JUnitAppender.assertWarnMessage("Address \"100\" is not a valid address.");

        // get with non-existent roster entry
        JsonNode dataEx2 = connection.getObjectMapper().createObjectNode().put(JsonRoster.ROSTER_ENTRY, 100);
        JsonThrottleSocketService serviceEx2 = new JsonThrottleSocketService(connection);
        ex = assertThrows( JsonException.class, () ->
                JsonThrottle.getThrottle("42", dataEx2, serviceEx2, 42),
                "Expected exception not thrown");
        assertEquals( 404, ex.getCode(), "Error code is HTTP Not Found");
        assertEquals( "Unable to create throttle for roster entry 100. Perhaps it does not exist?",
            ex.getMessage(), "Error message");
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
        assertNotNull( jsonThrottle, "JsonThrottle exists");
        Throttle throttle = jsonThrottle.getThrottle();
        assertNotNull( throttle, "has Throttle");

        for (int i = 0; i<29; i++) { // Functions 0 - 28
            // System.out.println("testing " +i);
            assertFalse(throttle.getFunction(i));
            data = connection.getObjectMapper().createObjectNode().put("F" + i, true);
            jsonThrottle.onMessage(Locale.ENGLISH, data, service);
            assertTrue(throttle.getFunction(i));
        }

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugCommandStation();
        var memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        InstanceManager.store(new TestThrottleManager(memo), ThrottleManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * DebugThrottleManager instance that requires that addresses 0-99 be short,
     * and that addresses greater than 127 be long.
     */
    private static class TestThrottleManager extends DebugThrottleManager {

        private TestThrottleManager(jmri.SystemConnectionMemo memo) {
            super(memo);
        }

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
