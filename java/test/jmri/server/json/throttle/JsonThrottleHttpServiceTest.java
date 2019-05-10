package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonException;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019	
 */
public class JsonThrottleHttpServiceTest {

    @Test
    public void testDoGet() {
        ObjectMapper mapper = new ObjectMapper();
        JsonThrottleHttpService service = new JsonThrottleHttpService(mapper);
        try {
            service.doGet(JsonThrottle.THROTTLE, "", service.getObjectMapper().createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Getting throttle is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonThrottleHttpService service = new JsonThrottleHttpService(mapper);
        try {
            service.doPut(JsonThrottle.THROTTLE, "", mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Putting throttle is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPost() {
        ObjectMapper mapper = new ObjectMapper();
        JsonThrottleHttpService service = new JsonThrottleHttpService(mapper);
        try {
            service.doPost(JsonThrottle.THROTTLE, "", mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Posting throttle is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoList() {
        ObjectMapper mapper = new ObjectMapper();
        JsonThrottleHttpService service = new JsonThrottleHttpService(mapper);
        try {
            service.doGetList(JsonThrottle.THROTTLE, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "throttle cannot be listed.", ex.getMessage());
        }
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
