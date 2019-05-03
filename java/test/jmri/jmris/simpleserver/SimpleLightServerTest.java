package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleLightServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleLightServerTest extends jmri.jmris.AbstractLightServerTestBase {
        
    private StringBuilder sb = null;
    private java.io.DataOutputStream output = null;
    private java.io.DataInputStream input = null;

    @Test
    public void testConnectionCtor() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleLightServer a = new SimpleLightServer(jcs);
        Assert.assertNotNull(a);
    }

    // test sending a message.
    @Test
    public void testSendMessage() {
        SimpleLightServer a = (SimpleLightServer)ls;
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod = null;
        try {
            sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method sendMessage in SimpleLightServer class. ");
        }

        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
            sendMessageMethod.invoke(a, "Hello World");
            Assert.assertEquals("SendMessage Check", "Hello World", sb.toString());
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method sendMessage in SimpleLightServer class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
        }
    }

    // test sending a message.
    @Test
    public void testSendMessageWithConnection() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleLightServer a = new SimpleLightServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod = null;
        try {
            sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method sendMessage in SimpleLightServer class. ");
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
            sendMessageMethod.invoke(a, "Hello World");
            Assert.assertEquals("SendMessage Check", "Hello World", jcs.getOutput());
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method sendMessage in SimpleLightServer class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
        }
    }

    // override the default permissions.
    // test sending an error message.
    @Test
    public void testSendErrorStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.sendErrorStatus("IT1");
            Assert.assertEquals("sendErrorStatus check", "LIGHT ERROR\n", sb.toString());
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending Error Status");
        }
    }

    // test sending an ON status message.
    @Test
    public void CheckSendOnStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.sendStatus("IL1", jmri.Light.ON);
            Assert.assertEquals("sendErrorStatus check", "LIGHT IL1 ON\n", sb.toString());
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending ON Status");
        }
    }

    // test sending an OFF status message.
    @Test
    public void CheckSendOffStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.sendStatus("IL1", jmri.Light.OFF);
            Assert.assertEquals("sendErrorStatus check", "LIGHT IL1 OFF\n", sb.toString());
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending OFF Status");
        }
    }

    // test sending an ON status message.
    @Test
    public void CheckSendUnknownStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.sendStatus("IL1", 255);
            Assert.assertEquals("sendErrorStatus check", "LIGHT IL1 UNKNOWN\n", sb.toString());
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending UNKNOWN Status");
        }
    }

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.parseStatus("LIGHT IL1 ON\n");
            jmri.Light light = (jmri.InstanceManager.getDefault(jmri.LightManager.class)).getLight("IL1");
            Assert.assertEquals("Parse On Status Check",
                    jmri.Light.ON,
                    light.getState());
            // parsing the status also causes a message to return to the client.
            Assert.assertEquals("sendErrorStatus check", "LIGHT IL1 ON\n", sb.toString());
        } catch (jmri.JmriException | java.io.IOException jmrie) {
            Assert.fail("Exception retrieving Status");
        }
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.parseStatus("LIGHT IL1 OFF\n");
            jmri.Light light = (jmri.InstanceManager.getDefault(jmri.LightManager.class)).getLight("IL1");
            Assert.assertEquals("Parse OFF Status Check",
                    jmri.Light.OFF,
                    light.getState());
            // parsing the status also causes a message to return to the client.
            //Assert.assertEquals("parse OFF Status check","LIGHT IL1 OFF\n",sb.toString());
        } catch (jmri.JmriException | java.io.IOException jmrie) {
            Assert.fail("Exception retrieving Status");
        }
    }

    // test parsing an UNKNOWN status message.
    @Test
    public void testParseUnkownStatus() {
        SimpleLightServer a = (SimpleLightServer)ls;
        try {
            a.parseStatus("LIGHT IL1 UNKNOWN\n");
            // this currently causes no change of state, so we are just
            // checking to make sure there is no exception.
        } catch (jmri.JmriException | java.io.IOException jmrie) {
            Assert.fail("Exception retrieving Status");
        }
    }

    // The minimal setup for log4J
    @Before
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
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        input = new java.io.DataInputStream(System.in);
        ls = new SimpleLightServer(input, output);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
