//SimpleLightServerTest.java
package jmri.jmris.simpleserver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleLightServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleLightServerTest {

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
        SimpleLightServer a = new SimpleLightServer(input, output);
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
        SimpleLightServer a = new SimpleLightServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleLightServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",sb.toString());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleLightServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }

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
        SimpleLightServer a = new SimpleLightServer(input, output);
        try {
            a.sendErrorStatus("IT1");
            Assert.assertEquals("sendErrorStatus check","LIGHT ERROR\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    // test sending an ON status message.
    @Test public void CheckSendOnStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleLightServer a = new SimpleLightServer(input, output);
        try {
            a.sendStatus("IL1",jmri.Light.ON);
            Assert.assertEquals("sendErrorStatus check","LIGHT IL1 ON\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending ON Status");
        }
    }

    // test sending an OFF status message.
    @Test public void CheckSendOffStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleLightServer a = new SimpleLightServer(input, output);
        try {
            a.sendStatus("IL1",jmri.Light.OFF);
            Assert.assertEquals("sendErrorStatus check","LIGHT IL1 OFF\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending OFF Status");
        }
    }

    // test sending an ON status message.
    @Test public void CheckSendUnknownStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleLightServer a = new SimpleLightServer(input, output);
        try {
            a.sendStatus("IL1",255);
            Assert.assertEquals("sendErrorStatus check","LIGHT IL1 UNKNOWN\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending UNKNOWN Status");
        }
    }

    // test parsing an ON status message.
    @Test public void testParseOnStatus() {
       StringBuilder sb = new StringBuilder();
       java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimpleLightServer a = new SimpleLightServer(input,output);
       try {
          a.parseStatus("LIGHT IL1 ON\n");
          jmri.Light light = (jmri.InstanceManager.getDefault(jmri.LightManager.class)).getLight("IL1");
          Assert.assertEquals("Parse On Status Check",
                       jmri.Light.ON,
                       light.getState());
          // parsing the status also causes a message to return to the client. 
          Assert.assertEquals("sendErrorStatus check","LIGHT IL1 ON\n",sb.toString());
       } catch(jmri.JmriException|java.io.IOException jmrie){
         Assert.fail("Exception retrieving Status");
       }
    }

    // test parsing an OFF status message.
    @Test public void testParseOffStatus() {
       StringBuilder sb = new StringBuilder();
       java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               // null output string drops characters
               // could be replaced by one that checks for specific outputs
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimpleLightServer a = new SimpleLightServer(input,output);
       try {
          a.parseStatus("LIGHT IL1 OFF\n");
          jmri.Light light = (jmri.InstanceManager.getDefault(jmri.LightManager.class)).getLight("IL1");
          Assert.assertEquals("Parse OFF Status Check",
                       jmri.Light.OFF,
                       light.getState());
          // parsing the status also causes a message to return to the client. 
          //Assert.assertEquals("parse OFF Status check","LIGHT IL1 OFF\n",sb.toString());
       } catch(jmri.JmriException|java.io.IOException jmrie){
         Assert.fail("Exception retrieving Status");
       }
    }

    // test parsing an UNKNOWN status message.
    @Test public void testParseUnkownStatus() {
       StringBuilder sb = new StringBuilder();
       java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               // null output string drops characters
               // could be replaced by one that checks for specific outputs
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimpleLightServer a = new SimpleLightServer(input,output);
       try {
          a.parseStatus("LIGHT IL1 UNKNOWN\n");
          // this currently causes no change of state, so we are just 
          // checking to make sure there is no exception.
       } catch(jmri.JmriException|java.io.IOException jmrie){
         Assert.fail("Exception retrieving Status");
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
