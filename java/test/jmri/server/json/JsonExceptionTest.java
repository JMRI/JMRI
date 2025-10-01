package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for JsonException class.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Randall Wood Copyright 2018
 */
public class JsonExceptionTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testIntStringThrowableIntConstructor() {
        JsonException instance = new JsonException(1234, "bogus", new Throwable(), 5);
        assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        assertEquals( JsonException.ERROR, result.path(JSON.TYPE).asText(), "JSON type");
        assertEquals( 1234, data.path(JsonException.CODE).asInt(), "Error code");
        assertEquals( "bogus", data.path(JsonException.MESSAGE).asText(), "Error message");
        assertEquals( 5, result.path(JSON.ID).asInt(), "Message Id");
    }

    @Test
    public void testIntStringIntConstructor() {
        JsonException instance = new JsonException(1234, "bogus", 0);
        assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        assertEquals( JsonException.ERROR, result.path(JSON.TYPE).asText(), "JSON type");
        assertEquals( 1234, data.path(JsonException.CODE).asInt(), "Error code");
        assertEquals( "bogus", data.path(JsonException.MESSAGE).asText(), "Error message");
        assertTrue( result.path(JSON.ID).isMissingNode(), "Message Id");
    }

    /**
     * Test of getJsonMessage method, of class JsonException.
     */
    @Test
    public void testIntThrowableIntConstructor() {
        JsonException instance = new JsonException(1234, new Throwable("bogus"), 42);
        assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        assertEquals( JsonException.ERROR, result.path(JSON.TYPE).asText(), "JSON type");
        assertEquals( 1234, data.path(JsonException.CODE).asInt(), "Error code");
        assertEquals( "java.lang.Throwable: bogus", data.path(JsonException.MESSAGE).asText(), "Error message");
        assertEquals( 42, result.path(JSON.ID).asInt(), "Message Id");
    }

}
