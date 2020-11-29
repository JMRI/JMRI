package jmri.jmris.simpleserver;

import jmri.Light;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
    public void testSendMessage() {
        SimpleLightServer a = (SimpleLightServer)ls;
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a, "Hello World");
        });
        assertThat(thrown).withFailMessage("Unablle to execute send message: {}").isNull();
        assertThat(sb.toString()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending a message.
    @Test
    public void testSendMessageWithConnection() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleLightServer a = new SimpleLightServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a, "Hello World");
        });
        assertThat(thrown).withFailMessage("Unablle to execute send message: {}").isNull();
        assertThat(jcs.getOutput()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // override the default permissions.
    // test sending an error message.
    @Test
    public void testSendErrorStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.sendErrorStatus("IT1"));
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT ERROR\n");
    }

    // test sending an ON status message.
    @Test
    public void CheckSendOnStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.sendStatus("IL1", jmri.Light.ON));
        assertThat(thrown).withFailMessage("Exception sending ON Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 ON\n");
    }

    // test sending an OFF status message.
    @Test
    public void CheckSendOffStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.sendStatus("IL1", jmri.Light.OFF));
        assertThat(thrown).withFailMessage("Exception sending OFF Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 OFF\n");
    }

    // test sending an ON status message.
    @Test
    public void CheckSendUnknownStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.sendStatus("IL1", 255));
        assertThat(thrown).withFailMessage("Exception sending UNKNOWN Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 UNKNOWN\n");
    }

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.parseStatus("LIGHT IL1 ON\n"));
        assertThat(thrown).withFailMessage("Exception retrieving Status").isNull();
        jmri.Light light = (jmri.InstanceManager.getDefault(jmri.LightManager.class)).getLight("IL1");
        assertThat(light.getState()).withFailMessage("Parse On Status Check").isEqualTo(jmri.Light.ON);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("LIGHT IL1 ON\n");
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() {
        Light light = (jmri.InstanceManager.getDefault(jmri.LightManager.class)).provideLight("IL1");
        light.setState(Light.ON);  // make sure the light is on before we parse the message.
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.parseStatus("LIGHT IL1 OFF\n"));
        assertThat(thrown).withFailMessage("Exception retrieving Status").isNull();
        assertThat(light.getState()).withFailMessage("Parse OFF Status Check").isEqualTo(jmri.Light.OFF);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("parse OFF Status check").isEqualTo("LIGHT IL1 OFF\n");
    }

    // test parsing an UNKNOWN status message.
    @Test
    public void testParseUnkownStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        Throwable thrown = catchThrowable( () -> a.parseStatus("LIGHT IL1 UNKNOWN\n"));
        // this currently causes no change of state, so we are just
        // checking to make sure there is no exception.
        assertThat(thrown).withFailMessage("Exception retrieving Status").isNull();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
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
