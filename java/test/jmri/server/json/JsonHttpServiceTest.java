package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonHttpServiceTest {

    private final static Logger log = LoggerFactory.getLogger(JsonHttpServiceTest.class);

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getObjectMapper method, of class JsonHttpService.
     */
    @Test
    public void testGetObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        Assert.assertEquals("get object mapper", mapper, (new JsonTestHttpService(mapper)).getObjectMapper());
    }

    /**
     * Test
     * {@link JsonHttpService#doSchema(java.lang.String, boolean, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testDoSchema4Param() {
        JsonTestHttpService instance = new JsonTestHttpService(new ObjectMapper());
        // Ensure a JsonException with code 500 is thrown if the schema
        // resource is invalid
        try {
            instance.doSchema(JsonTestServiceFactory.TEST,
                    true,
                    "jmri/server/json/schema/not-jmri-client-schema",
                    "jmri/server/json/schema/not-jmri-server-schema");
            Assert.fail("Expected exception to be thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Exception is coded 500", 500, ex.getCode());
        }
        try {
            instance.doSchema(JsonTestServiceFactory.TEST,
                    false,
                    "jmri/server/json/schema/not-jmri-client-schema",
                    "jmri/server/json/schema/not-jmri-server-schema");
            Assert.fail("Expected exception to be thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Exception is coded 500", 500, ex.getCode());
        }
        // Test that real schemas return correctly
        try {
            instance.doSchema(JsonTestServiceFactory.TEST,
                    true,
                    "jmri/server/json/schema/json-client.json",
                    "jmri/server/json/schema/json-server.json");
            instance.doSchema(JsonTestServiceFactory.TEST,
                    false,
                    "jmri/server/json/schema/json-client.json",
                    "jmri/server/json/schema/json-server.json");
        } catch (JsonException ex) {
            Assert.fail("Should not have thrown exception");
        }
    }

    /**
     * Test that a JMRI JSON message is valid per the JMRI JSON schema.
     *
     * @param message the message to test
     * @throws IOException if unable to read the schema
     */
    public static void testValidJmriJsonMessage(JsonNode message) throws IOException {
        URL resource = JsonHttpServiceTest.class.getClassLoader().getResource("jmri/server/json/schema/json-server.json");
        testSchemaValidJson(message, new ObjectMapper().readTree((resource)));
    }

    /**
     * Test that a node is a valid per the given schema.
     *
     * @param node   the node to test
     * @param schema the schema to test with
     */
    public static void testSchemaValidJson(JsonNode node, JsonNode schema) {
        Set<ValidationMessage> errors = JsonSchemaFactory.getInstance().getSchema(schema).validate(node);
        if (!errors.isEmpty()) {
            log.warn("Errors validating {}", node);
            errors.forEach((error) -> {
                log.warn("JSON Validation Error: {}\n\t{}\n\t{}\n\t{}", error.getCode(), error.getMessage(), error.getPath(), error.getType());
            });
        }
        Assert.assertTrue("No errors expected", errors.isEmpty());
    }
}
