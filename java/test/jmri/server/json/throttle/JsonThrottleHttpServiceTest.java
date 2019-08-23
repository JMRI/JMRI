package jmri.server.json.throttle;

import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019	
 */
public class JsonThrottleHttpServiceTest extends JsonHttpServiceTestBase<JsonThrottleHttpService> {

    @Test
    public void testDoGet() {
        try {
            service.doGet(JsonThrottle.THROTTLE, "", mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Getting throttle is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        try {
            service.doPut(JsonThrottle.THROTTLE, "", mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Putting throttle is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPost() {
        try {
            service.doPost(JsonThrottle.THROTTLE, "", mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Posting throttle is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoList() {
        try {
            service.doGetList(JsonThrottle.THROTTLE, mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            assertEquals("Error message", "throttle cannot be listed.", ex.getMessage());
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonThrottleHttpService(mapper);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
