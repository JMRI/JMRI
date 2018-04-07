package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.server.json.JsonException;
import jmri.server.json.schema.JsonSchemaServiceCache;
import org.junit.Assert;

/**
 * Common methods for JMRI JSON Service HTTP provider tests.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonHttpServiceTestBase {

    /**
     * Validate a JsonNode produced by the JMRI JSON server against published
     * JMRI JSON service schema. Asserts a failure if the node is not schema
     * valid.
     *
     * @param node the node to validate
     */
    public final void validate(JsonNode node) {
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(node, true, Locale.ENGLISH);
        } catch (JsonException ex) {
            Assert.fail("Unable to validate schema.");
        }
    }

}
