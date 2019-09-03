package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleSensorServerTest extends jmri.jmris.AbstractSensorServerTestBase {

    private StringBuilder sb = null;

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
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = ss.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleSensorServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(ss,"Hello World");
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


    // test Parsing an ACTIVE status message.
    @Test 
    public void parseActiveStatus() throws Exception {
        ss.parseStatus("SENSOR IS1 ACTIVE\n");
        jmri.Sensor sensor = (jmri.InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1");
        Assert.assertEquals("Parse Active Status Check",
                       jmri.Sensor.ACTIVE,
                       sensor.getState());
        // parsing the status also causes a message to return to
        // the client.
        checkSensorActiveSent();
    }

    // test Parsing an INACTIVE status message.
    @Test 
    public void parseInactiveStatus() throws Exception {
         ss.parseStatus("SENSOR IS1 INACTIVE\n");
         jmri.Sensor sensor = (jmri.InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1");
         Assert.assertEquals("Parse Inactive Status Check",
                       jmri.Sensor.INACTIVE,
                       sensor.getState());
         // parsing the status also causes a message to return to
         // the client.
         checkSensorInActiveSent();
    }

    // test Parsing an blank status message.
    @Test 
    public void parseBlankStatus() throws Exception {
        ss.parseStatus("SENSOR IS1\n");
        // nothing has changed the sensor, so it should be unknown.
        checkSensorUnknownSent();
        // verify the sensor exists, it should have been created with provideSensor.
        Assert.assertNotNull(jmri.InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1"));
    }

    // test Parsing an other status message.
    @Test 
    public void parseOtherStatus() throws Exception {
        ss.parseStatus("SENSOR IS1 UNKNOWN\n");
        // this isn't INACTIVE or ACTIVE, so it should be just like blank.
        // nothing has changed the sensor, so it should be unknown.
        checkSensorUnknownSent();
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkErrorStatusSent(){
         Assert.assertEquals("sendErrorStatus check","SENSOR ERROR\n",sb.toString());
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorActiveSent(){
         Assert.assertEquals("sendStatus check","SENSOR IS1 ACTIVE\n",sb.toString());
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorInActiveSent(){
         Assert.assertEquals("sendStatus check","SENSOR IS1 INACTIVE\n",sb.toString());
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorUnknownSent(){
         Assert.assertEquals("sendStatus check","SENSOR IS1 UNKNOWN\n",sb.toString());
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
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ss = new SimpleSensorServer(input, output);
    }

    @After public void tearDown() throws Exception {
        ss.dispose();
        ss = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
