package jmri.jmris.simpleserver;

import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleLightServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleLightServerTest extends jmri.jmris.AbstractLightServerTestBase {
        
    private StringBuilder sb = null;
    private java.io.DataOutputStream output = null;

    @Test
    public void testConnectionCtor() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleLightServer a = new SimpleLightServer(jcs);
        assertThat(a).isNotNull();
    }

    // test sending a message.
    @Test
    public void testSendMessage() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        // NOTE: this test uses reflection to test a private method.
        Assertions.assertDoesNotThrow( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a, "Hello World");
        },"Unable to execute send message");
        assertThat(sb.toString()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending a message.
    @Test
    public void testSendMessageWithConnection() throws Exception {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleLightServer a = new SimpleLightServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        Assertions.assertDoesNotThrow( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a, "Hello World");
        },"Unable to execute send message");
        assertThat(jcs.getOutput()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // override the default permissions.
    // test sending an error message.
    @Test
    public void testSendErrorStatus() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.sendErrorStatus("IT1"),"Exception sending Error Status");
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT ERROR\n");
    }

    // test sending an ON status message.
    @Test
    public void testCheckSendOnStatus() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.sendStatus("IL1", Light.ON),"Exception sending ON Status");
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 ON\n");
    }

    // test sending an OFF status message.
    @Test
    public void testCheckSendOffStatus() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.sendStatus("IL1", Light.OFF),"Exception sending OFF Status");
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 OFF\n");
    }

    // test sending an ON status message.
    @Test
    public void testCheckSendUnknownStatus() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.sendStatus("IL1", 255),"Exception sending UNKNOWN Status");
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 UNKNOWN\n");
    }

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.parseStatus("LIGHT IL1 ON\n"),"Exception retrieving Status");
        Light light = (jmri.InstanceManager.getDefault(LightManager.class)).getLight("IL1");
        Assertions.assertNotNull(light);
        assertThat(light.getState()).withFailMessage("Parse On Status Check").isEqualTo(Light.ON);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 ON\n");
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        Light light = (jmri.InstanceManager.getDefault(LightManager.class)).provideLight("IL1");
        light.setState(Light.ON);  // make sure the light is on before we parse the message.
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.parseStatus("LIGHT IL1 OFF\n"),"Exception retrieving Status");
        assertThat(light.getState()).withFailMessage("Parse OFF Status Check").isEqualTo(Light.OFF);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("parse OFF Status check").isEqualTo("LIGHT IL1 OFF\n");
    }

    // test parsing an UNKNOWN status message.
    @Test
    public void testParseUnkownStatus() throws Exception {
        SimpleLightServer a = (SimpleLightServer)ls;
        Assertions.assertDoesNotThrow( () -> a.parseStatus("LIGHT IL1 UNKNOWN\n"),"Exception retrieving Status");
        // this currently causes no change of state, so we are just
        // checking to make sure there is no exception.
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        sb = new StringBuilder();
        output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            // null output string drops characters
            // could be replaced by one that checks for specific outputs
            @Override
            public void write(int b) {
                sb.append((char) b);
            }
        });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ls = new SimpleLightServer(input, output);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
