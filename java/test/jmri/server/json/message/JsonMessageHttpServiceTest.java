package jmri.server.json.message;

import com.fasterxml.jackson.databind.node.NullNode;

import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonMessageHttpServiceTest extends JsonHttpServiceTestBase<JsonMessageHttpService> {

    @Test
    public void testDoGet() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(JsonMessage.MESSAGE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Getting message is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoPut() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPut(JsonMessage.MESSAGE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Putting message is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoPost() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doPost(JsonMessage.MESSAGE, "", mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Posting message is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    @Override
    public void testDoDelete() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doDelete(JsonMessage.MESSAGE, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 405, ex.getCode(), "Error code is HTTP Method Not Allowed");
        assertEquals( "Deleting message is not allowed.", ex.getMessage(), "Error message");
    }

    @Test
    public void testDoGetList() {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGetList(JsonMessage.MESSAGE, mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals( 400, ex.getCode(), "Error code is HTTP Bad Request");
        assertEquals( "message cannot be listed.", ex.getMessage(), "Error message");
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonMessageHttpService(mapper);
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
