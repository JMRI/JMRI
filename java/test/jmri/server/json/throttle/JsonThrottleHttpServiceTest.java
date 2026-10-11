package jmri.server.json.throttle;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
public class JsonThrottleHttpServiceTest extends JsonHttpServiceTestBase<JsonThrottleHttpService> {

    @Test
    public void testDoGet() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JsonThrottle.THROTTLE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Getting throttle is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoPut() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonThrottle.THROTTLE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Putting throttle is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoPost() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonThrottle.THROTTLE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Posting throttle is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoList() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGetList(JsonThrottle.THROTTLE, mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "throttle cannot be listed.", ex.getMessage(), "Error message");
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonThrottleHttpService(mapper);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
