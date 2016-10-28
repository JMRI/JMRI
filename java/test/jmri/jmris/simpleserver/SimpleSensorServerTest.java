//SimpleSensorServerTest.java
package jmri.jmris.simpleserver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleSensorServerTest {

    @Test public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        Assert.assertNotNull(a);
    }

    @Test public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleSensorServer a = new SimpleSensorServer(jcs);
        Assert.assertNotNull(a);
    }

    // test sending a message.
    @Test public void testSendMessage() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleSensorServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",sb.toString());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleSensorServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }

    // test sending a message.
    @Test public void testSendMessageWithConnection() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleSensorServer a = new SimpleSensorServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleSensorServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",jcs.getOutput());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleSensorServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }

    // test sending an error message.

    // test sending an error message.
    @Test public void testSendErrorStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {

                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        try {
            a.sendErrorStatus("IT1");
            Assert.assertEquals("sendErrorStatus check","SENSOR ERROR\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    // test intializing a Sensor status message.
    @Test public void checkInitSensor() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        a.initSensor("IS1");
        Assert.assertNotNull((jmri.InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1"));
    }

    // test sending an ACTIVE status message.
    @Test public void CheckSendActiveStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        a.initSensor("IS1");
        try {
            a.sendStatus("IS1",jmri.Sensor.ACTIVE);
            Assert.assertEquals("sendStatus check","SENSOR IS1 ACTIVE\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending ACTIVE Status");
        }
    }

    // test sending an INACTIVE status message.
    @Test public void CheckSendInActiveStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        a.initSensor("IS1");
        try {
            a.sendStatus("IS1",jmri.Sensor.ACTIVE);
            Assert.assertEquals("sendStatus check","SENSOR IS1 ACTIVE\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending ACTIVE Status");
        }
    }

    // test sending an UNKNOWN status message.
    @Test public void CheckSendUnkownStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        a.initSensor("IS1");
        try {
            a.sendStatus("IS1",jmri.Sensor.UNKNOWN);
            Assert.assertEquals("sendStatus check","SENSOR IS1 UNKNOWN\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending UNKNOWN Status");
        }
    }

    // test Parsing an ACTIVE status message.
    @Test public void parseActiveStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        try {
            a.parseStatus("SENSOR IS1 ACTIVE\n");
            jmri.Sensor sensor = (jmri.InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1");
          Assert.assertEquals("Parse Active Status Check",
                       jmri.Sensor.ACTIVE,
                       sensor.getState());
            // parsing the status also causes a message to return to
            // the client.
            Assert.assertEquals("parse Active check","SENSOR IS1 ACTIVE\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing ACTIVE Status");
        }
    }

    // test Parsing an INACTIVE status message.
    @Test public void parseInactiveStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        try {
            a.parseStatus("SENSOR IS1 INACTIVE\n");
            jmri.Sensor sensor = (jmri.InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1");
          Assert.assertEquals("Parse Inactive Status Check",
                       jmri.Sensor.INACTIVE,
                       sensor.getState());
            // parsing the status also causes a message to return to
            // the client.
            Assert.assertEquals("parse Inactive check","SENSOR IS1 INACTIVE\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing ACTIVE Status");
        }
    }

    // test Parsing an blank status message.
    @Test public void parseBlankStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        try {
            a.parseStatus("SENSOR IS1\n");
            // nothing has changed the sensor, so it should be unknown.
            Assert.assertEquals("parse blank check","SENSOR IS1 UNKNOWN\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing ACTIVE Status");
        }
    }

    // test Parsing an other status message.
    @Test public void parseOtherStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input, output);
        try {
            a.parseStatus("SENSOR IS1 UNKNOWN\n");
            // this isn't INACTIVE or ACTIVE, so it should be just like
            // blank.
            // nothing has changed the sensor, so it should be unknown.
            Assert.assertEquals("parse blank check","SENSOR IS1 UNKNOWN\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing ACTIVE Status");
        }
    }

    // The minimal setup for log4J
    @Before public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
    }

    @After public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
