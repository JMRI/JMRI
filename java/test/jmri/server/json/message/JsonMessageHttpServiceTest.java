package jmri.server.json.message;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;

public class JsonMessageHttpServiceTest {

    @Test
    public void testDoGet() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMessageHttpService service = new JsonMessageHttpService(mapper);
        try {
            service.doGet(JsonMessage.MESSAGE, "", mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Getting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPut() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMessageHttpService service = new JsonMessageHttpService(mapper);
        try {
            service.doPut(JsonMessage.MESSAGE, "", mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Putting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoPost() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMessageHttpService service = new JsonMessageHttpService(mapper);
        try {
            service.doPost(JsonMessage.MESSAGE, "", mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Posting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoDelete() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMessageHttpService service = new JsonMessageHttpService(mapper);
        try {
            service.doDelete(JsonMessage.MESSAGE, "", Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Method Not Allowed", 405, ex.getCode());
            Assert.assertEquals("Error message", "Deleting message is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testDoGetList() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMessageHttpService service = new JsonMessageHttpService(mapper);
        try {
            service.doGetList(JsonMessage.MESSAGE, mapper.createObjectNode(), Locale.ENGLISH);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP Bad Request", 400, ex.getCode());
            Assert.assertEquals("Error message", "message cannot be listed.", ex.getMessage());
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}