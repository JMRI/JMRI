package jmri.server.json.schema;

import java.util.Locale;
import java.util.Set;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceCacheTest {

    private final Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
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
        JSON.VERSIONS.forEach(version -> instance.getTypes(version).forEach((type) -> {
            Set<JsonHttpService> services = instance.getServices(type, version);
            if (!type.equals(JSON.HELLO)) {
                assertEquals( 1, services.size(), "Only single service for type");
            } else {
                assertEquals( 2, services.size(), "Only two services for type");
            }
            services.forEach((service) -> {
                testService(service, type);
            });
        }));
    }

    private void testService(JsonHttpService service, String type) {

        try {
            service.doSchema(type, true, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        } catch (JsonException ex) {
            Throwable cause = ex.getCause();
            assertEquals( 400, ex.getCode(),
                "Unexpected exception for type " + type + " from service " + service + "\n" +
                ex.getMessage() + (cause != null ? "\n" + cause.toString() : ""));
            assertEquals( "No messages from servers of type " + type + " are allowed.",
                ex.getMessage(),
                "Only no server exception expected for type " + type + " from service " + service);
        }

        try {
            service.doSchema(type, false, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        } catch (JsonException ex) {
            Throwable cause = ex.getCause();
            assertEquals( 400, ex.getCode(),
                "Unexpected exception for type " + type + " from service " + service + "\n" +
                    ex.getMessage() + (cause != null ? "\n" + cause.toString() : ""));
            assertEquals( "No messages from clients of type " + type + " are allowed.",
                ex.getMessage(),
                "Only no client exception expected for type " + type + " from service " + service);
        }

        // test that every service throws an expected exception
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doSchema("invalid-type", true, new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception for type \"invalid-type\" not thrown by " + service);
        Throwable cause = ex.getCause();
        assertEquals( 500, ex.getCode(),
            "Unexpected exception for type invalid-type from service " + service + "\n" +
                ex.getMessage() + (cause != null ? "\n" + cause.toString() : ""));
        assertEquals( "Unknown object type invalid-type was requested.",
            ex.getMessage(),
            "Only unknown type exception expected for type invalid-type from service " + service);

    }

}
