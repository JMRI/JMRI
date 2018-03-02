package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
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
                    JsonNode node = service.doSchema(type, true, Locale.ENGLISH);
                } catch (JsonException ex) {
                    if (ex.getCode() != 400) {
                        Assert.fail("Unexpected exception for type " + type + " from service " + service);
                    }
                    Assert.assertEquals("Only no server exception expected for type " + type + " from service " + service,
                            400,
                            ex.getCode());
                    Assert.assertEquals("Only no server exception expected for type " + type + " from service " + service,
                            "No messages from servers of type \"" + type + "\" are allowed.",
                            ex.getMessage());
                }
                try {
                    JsonNode node = service.doSchema(type, false, Locale.ENGLISH);
                } catch (JsonException ex) {
                    if (ex.getCode() != 400) {
                        Assert.fail("Unexpected exception for type " + type + " from service " + service);
                    }
                    Assert.assertEquals("Only no client exception expected for type " + type + " from service " + service,
                            400,
                            ex.getCode());
                    Assert.assertEquals("Only no client exception expected for type " + type + " from service " + service,
                            "" + type + "",
                            ex.getMessage());
                }
            });
        });
    }

}
