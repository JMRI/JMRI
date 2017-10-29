package jmri.server.json.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonMessageSocketServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testOnMessage() {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        try {
            instance.onMessage(type, data, locale);
            Assert.assertNull(connection.getMessage());
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail("Unexpected exception thrown.");
        }
    }

    @Test
    public void testOnList() {
        String type = JSON.HELLO;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageSocketService instance = new JsonMessageSocketService(connection);
        try {
            instance.onList(type, data, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (IOException | JmriException ex) {
            Assert.fail("Unexpected exception thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        }
    }

}
