package jmri.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonHttpServiceTest {

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
     * {@link JsonHttpService#doSchema(java.lang.String, boolean, java.lang.String, java.lang.String, int)}.
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
                    "jmri/server/json/schema/not-jmri-server-schema",
                    0);
            Assert.fail("Expected exception to be thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Exception is coded 500", 500, ex.getCode());
        }
        try {
            instance.doSchema(JsonTestServiceFactory.TEST,
                    false,
                    "jmri/server/json/schema/not-jmri-client-schema",
                    "jmri/server/json/schema/not-jmri-server-schema",
                    0);
            Assert.fail("Expected exception to be thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Exception is coded 500", 500, ex.getCode());
        }
        // Test that real schemas return correctly
        try {
            instance.doSchema(JsonTestServiceFactory.TEST,
                    true,
                    "jmri/server/json/schema/json-client.json",
                    "jmri/server/json/schema/json-server.json",
                    0);
            instance.doSchema(JsonTestServiceFactory.TEST,
                    false,
                    "jmri/server/json/schema/json-client.json",
                    "jmri/server/json/schema/json-server.json",
                    0);
        } catch (JsonException ex) {
            Assert.fail("Should not have thrown exception");
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonHttpServiceTest.class);
}
