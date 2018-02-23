package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for JsonException class.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Randall Wood Copyright 2018
 */
public class JsonExceptionTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testIntStringThrowableConstructor() {
        JsonException instance = new JsonException(1234, "bogus", new Throwable());
        Assert.assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JSON type", JsonException.ERROR, result.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code", 1234, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Error message", "bogus", data.path(JsonException.MESSAGE).asText());
    }

    @Test
    public void testIntStringConstructor() {
        JsonException instance = new JsonException(1234, "bogus");
        Assert.assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JSON type", JsonException.ERROR, result.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code", 1234, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Error message", "bogus", data.path(JsonException.MESSAGE).asText());
    }

    /**
     * Test of getJsonMessage method, of class JsonException.
     */
    @Test
    public void testIntThrowableConstructor() {
        JsonException instance = new JsonException(1234, new Throwable("bogus"));
        Assert.assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JSON type", JsonException.ERROR, result.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code", 1234, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Error message", "java.lang.Throwable: bogus", data.path(JsonException.MESSAGE).asText());
    }

}
