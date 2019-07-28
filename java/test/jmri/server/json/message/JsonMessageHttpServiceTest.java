package jmri.server.json.message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.node.NullNode;

import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.util.JUnitUtil;

public class JsonMessageHttpServiceTest extends JsonHttpServiceTestBase<JsonMessageHttpService> {

    @Test
    public void testDoGet() {
        try {
            service.doGet(JsonMessage.MESSAGE, "", mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Getting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        try {
            service.doPut(JsonMessage.MESSAGE, "", mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Putting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPost() {
        try {
            service.doPost(JsonMessage.MESSAGE, "", mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Posting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    @Override
    public void testDoDelete() {
        try {
            service.doDelete(JsonMessage.MESSAGE, "", NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            assertEquals("Error message", "Deleting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        try {
            service.doGetList(JsonMessage.MESSAGE, mapper.createObjectNode(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            assertEquals("Error message", "message cannot be listed.", ex.getMessage());
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonMessageHttpService(mapper);
        JUnitUtil.resetProfileManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}