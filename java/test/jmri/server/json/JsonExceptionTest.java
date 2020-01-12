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
    public void testIntStringThrowableIntConstructor() {
        JsonException instance = new JsonException(1234, "bogus", new Throwable(), 5);
        Assert.assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JSON type", JsonException.ERROR, result.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code", 1234, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Error message", "bogus", data.path(JsonException.MESSAGE).asText());
        Assert.assertEquals("Message Id", 5, result.path(JSON.ID).asInt());
    }

    @Test
    public void testIntStringIntConstructor() {
        JsonException instance = new JsonException(1234, "bogus", 0);
        Assert.assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JSON type", JsonException.ERROR, result.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code", 1234, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Error message", "bogus", data.path(JsonException.MESSAGE).asText());
        Assert.assertTrue("Message Id", result.path(JSON.ID).isMissingNode());
    }

    /**
     * Test of getJsonMessage method, of class JsonException.
     */
    @Test
    public void testIntThrowableIntConstructor() {
        JsonException instance = new JsonException(1234, new Throwable("bogus"), 42);
        Assert.assertEquals(1234, instance.getCode());
        JsonNode result = instance.getJsonMessage();
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JSON type", JsonException.ERROR, result.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code", 1234, data.path(JsonException.CODE).asInt());
        Assert.assertEquals("Error message", "java.lang.Throwable: bogus", data.path(JsonException.MESSAGE).asText());
        Assert.assertEquals("Message Id", 42, result.path(JSON.ID).asInt());
    }

}
