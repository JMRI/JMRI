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

    public static void testValidJmriJsonMessage(JsonNode message) throws IOException {
        URL resource = JsonHttpServiceTest.class.getClassLoader().getResource("jmri/server/json/schema/json-server.json");
        testSchemaValidJson(message, new ObjectMapper().readTree((resource)));
    }

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
