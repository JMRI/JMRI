package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitUtil;
import org.junit.Assert;

/**
 * Common methods for JMRI JSON Service HTTP provider tests.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonHttpServiceTestBase {

    protected ObjectMapper mapper = null;
    protected Locale locale = Locale.ENGLISH;

    /**
     *
     * @throws Exception to allow overriding methods to throw any exception
     */
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        this.mapper = new ObjectMapper();
        // require valid inputs and outputs for tests by default
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateClientMessages(true);
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    /**
     *
     * @throws Exception to allow overriding methods to throw any exception
     */
    public void tearDown() throws Exception {
        this.mapper = null;
        JUnitUtil.tearDown();
    }

    /**
     * Validate a JsonNode produced by the JMRI JSON server against published
     * JMRI JSON service schema. Asserts a failure if the node is not schema
     * valid.
     *
     * @param node the node to validate
     */
    public final void validate(JsonNode node) {
        try {
            InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(node, true, locale);
        } catch (JsonException ex) {
            Assert.fail("Unable to validate schema.");
        }
    }

}
