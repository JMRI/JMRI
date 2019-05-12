package jmri.server.json.schema;

import java.util.Locale;
import java.util.Set;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceCacheTest {

    private Locale locale = Locale.ENGLISH;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Get every service and every schema made available by JSON service
     * factories.
     */
    @Test
    public void testSchemas() {
        JsonSchemaServiceCache instance = new JsonSchemaServiceCache();
        instance.getTypes().forEach((type) -> {
            Set<JsonHttpService> services = instance.getServices(type);
            if (!type.equals(JSON.HELLO)) {
                Assert.assertEquals("Only single service for type", 1, services.size());
            } else {
                Assert.assertEquals("Only two services for type", 2, services.size());
            }
            services.forEach((service) -> {
                try {
                    service.doSchema(type, true, locale, 0);
                } catch (JsonException ex) {
                    Throwable cause = ex.getCause();
                    Assert.assertEquals("Unexpected exception for type " + type + " from service " + service + "\n" + ex.getMessage()
                            + (cause != null ? "\n" + cause.toString() : ""),
                            400,
                            ex.getCode()
                    );
                    Assert.assertEquals("Only no server exception expected for type " + type + " from service " + service,
                            "No messages from servers of type " + type + " are allowed.",
                            ex.getMessage());
                }
                try {
                    service.doSchema(type, false, locale, 0);
                } catch (JsonException ex) {
                    Throwable cause = ex.getCause();
                    Assert.assertEquals("Unexpected exception for type " + type + " from service " + service + "\n" + ex.getMessage()
                            + (cause != null ? "\n" + cause.toString() : ""),
                            400,
                            ex.getCode());
                    Assert.assertEquals("Only no client exception expected for type " + type + " from service " + service,
                            "No messages from clients of type " + type + " are allowed.",
                            ex.getMessage());
                }
                // test that every service throws an expected exception
                try {
                    service.doSchema("invalid-type", true, locale, 0);
                    Assert.fail("Expected exception for type \"invalid-type\" not thrown by " + service);
                } catch (JsonException ex) {
                    Throwable cause = ex.getCause();
                    Assert.assertEquals("Unexpected exception for type invalid-type from service " + service + "\n" + ex.getMessage()
                            + (cause != null ? "\n" + cause.toString() : ""),
                            500,
                            ex.getCode());
                    Assert.assertEquals("Only unknown type exception expected for type invalid-type from service " + service,
                            "Unknown object type invalid-type was requested.",
                            ex.getMessage());
                }
            });
        });
    }

}
