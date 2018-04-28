package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaHttpServiceTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of doGet method, of class JsonSchemaHttpService.
     *
     * @throws jmri.server.json.JsonException on unexpected exception
     */
    @Test
    public void testDoGet() throws JsonException {
        JsonSchemaHttpService instance = new JsonSchemaHttpService(new ObjectMapper());
        JsonNode result = instance.doGet(JSON.SCHEMA, JSON.JSON, Locale.ENGLISH);
        Assert.assertTrue("Is an array", result.isArray());
        Assert.assertEquals("Array has two elements", 2, result.size());
        Assert.assertTrue("1st element is JsonObject", result.get(0).isObject());
        Assert.assertTrue("2nd element is JsonObject", result.get(1).isObject());
        this.testIsSchema(result.get(0));
        this.testIsSchema(result.get(1));
        try {
            instance.doGet(JSON.JSON, JSON.JSON, Locale.ENGLISH);
            Assert.fail("Should have thrown exception");
        } catch (JsonException ex) {
            Assert.assertEquals("Exception code is 400", 400, ex.getCode());
            Assert.assertEquals("Error message", "Unknown object type json was requested.", ex.getMessage());
        }
    }

    private void testIsSchema(JsonNode root) {
        Assert.assertEquals("Is schema", JSON.SCHEMA, root.path(JSON.TYPE).asText());
        JsonNode data = root.path(JSON.DATA);
        Assert.assertEquals("Data has three properties", 3, data.size());
        Assert.assertTrue("Data has name property that is string", data.path(JSON.NAME).isTextual());
        Assert.assertTrue("Data has server property that is boolean", data.path(JSON.SERVER).isBoolean());
        Assert.assertTrue("Data has schema property that is object", data.path(JSON.SCHEMA).isObject());
    }
}
