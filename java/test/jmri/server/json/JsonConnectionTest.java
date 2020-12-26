package jmri.server.json;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jmri.InstanceManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonConnectionTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getObjectMapper method, of class JsonConnection. Verifies that
     * getObjectMapper() is not null.
     */
    @Test
    public void testGetObjectMapper() {
        JsonConnection instance = new JsonConnection((DataOutputStream) null);
        Assert.assertNotNull("ObjectMapper is created", instance.getObjectMapper());
    }

    /**
     * Test of sendMessage method, of class JsonConnection. Verifies that a
     * valid message is sent and that an error is sent for an invalid message
     * when validating messages.
     *
     * @throws java.io.IOException if unable to write to output stream
     */
    @Test
    public void testSendMessage_validating() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        JsonConnection instance = new JsonConnection(dos);
        String valid = "{\"type\":\"pong\"}";
        String invalid = "{\"type\":\"hello\"}"; // missing data portion
        String error = "{\"type\":\"error\",\"data\":{\"code\":500,\"message\":\"There was an error; see the JMRI application logs for details.\"}}";
        // test when validating messages
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
        // validate valid message
        instance.sendMessage(instance.getObjectMapper().readTree(valid), 0);
        Assert.assertEquals("Valid message is passed", valid, baos.toString(StandardCharsets.UTF_8.name()));
        baos.reset();
        // validate invalid message
        instance.sendMessage(instance.getObjectMapper().readTree(invalid), 0);
        Assert.assertNotEquals("Invalid message is not passed", invalid, baos.toString(StandardCharsets.UTF_8.name()));
        Assert.assertEquals("Invalid message is replaced with error", error, baos.toString(StandardCharsets.UTF_8.name()));
        baos.reset();
        // suppress warnings from validating invalid message (there are five)
        JUnitAppender.checkForMessageStartingWith("Errors validating");
        JUnitAppender.checkForMessageStartingWith("JSON Validation Error");
        JUnitAppender.checkForMessageStartingWith("JSON Validation Error");
        JUnitAppender.checkForMessageStartingWith("JSON Validation Error");
        JUnitAppender.checkForMessageStartingWith("JSON Validation Error");
    }

    /**
     * Test of sendMessage method, of class JsonConnection. Verifies that valid
     * and invalid messages are sent when not validating messages.
     *
     * @throws java.io.IOException if unable to write to output stream
     */
    @Test
    public void testSendMessage_nonValidating() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        JsonConnection instance = new JsonConnection(dos);
        String valid = "{\"type\":\"pong\"}";
        String invalid = "{\"type\":\"hello\"}"; // missing data portion
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(false);
        // pass valid message when not validating
        instance.sendMessage(instance.getObjectMapper().readTree(valid), 0);
        Assert.assertEquals("Valid message is passed", valid, baos.toString(StandardCharsets.UTF_8.name()));
        baos.reset();
        // pass invalid message when not validating
        instance.sendMessage(instance.getObjectMapper().readTree(invalid), 0);
        Assert.assertEquals("Invalid message is passed", invalid, baos.toString(StandardCharsets.UTF_8.name()));
        baos.reset();
    }
}
