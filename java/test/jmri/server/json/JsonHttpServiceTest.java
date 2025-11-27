package jmri.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonHttpServiceTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getObjectMapper method, of class JsonHttpService.
     */
    @Test
    public void testGetObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals( mapper, (new JsonTestHttpService(mapper)).getObjectMapper(), "get object mapper");
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
        JsonException ex = assertThrows( JsonException.class,
            () -> instance.doSchema(JsonTestServiceFactory.TEST,
                true,
                "jmri/server/json/schema/not-jmri-client-schema",
                "jmri/server/json/schema/not-jmri-server-schema",
                0));
        assertEquals( 500, ex.getCode(), "Exception is coded 500");

        // Test that real schemas return correctly
        assertDoesNotThrow( () -> {
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
        },("Should not have thrown exception"));
        
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonHttpServiceTest.class);
}
