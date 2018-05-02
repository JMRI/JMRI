//SimpleSignalHeadServerTest.java
package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleSignalHeadServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleSignalHeadServerTest {

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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(jcs);
        Assert.assertNotNull(a);
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleSignalHeadServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",jcs.getOutput());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleSignalHeadServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleSignalHeadServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",sb.toString());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleSignalHeadServer class");
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.sendErrorStatus("IT1");
            Assert.assertEquals("sendErrorStatus check","SIGNALHEAD ERROR\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    // test intializing a SignalHead status message.
    @Test public void checkInitSignalHead() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        new SimpleSignalHeadServer(input, output);
        Assert.assertNotNull((jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1"));
    }

    // test sending DARK status message.
    @Test public void CheckSendDarkStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.sendStatus("IH1",jmri.SignalHead.DARK);
            Assert.assertEquals("sendStatus check","SIGNALHEAD IH1 DARK\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending DARK Status");
        }
    }

    // test sending an RED status message.
    @Test public void CheckSendRedStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.sendStatus("IH1",jmri.SignalHead.RED);
            Assert.assertEquals("sendStatus check","SIGNALHEAD IH1 RED\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending RED Status");
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.sendStatus("IH1",jmri.SignalHead.UNKNOWN);
            Assert.assertEquals("sendStatus check","SIGNALHEAD IH1 RED\n",sb.toString());
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending UNKNOWN Status");
        }
    }

    // test Parsing an DARK status message.
    @Test public void parseDarkStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.parseStatus("SIGNALHEAD IH1 DARK\n");
            jmri.SignalHead signalHead = (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1");
          Assert.assertEquals("Parse Active Status Check",
                       jmri.SignalHead.DARK,
                       signalHead.getAppearance());
            // parsing the status also causes a message to return to
            // the client.
            Assert.assertEquals("parse Dark check","SIGNALHEAD IH1 DARK\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing DARK Status");
        }
    }

    // test Parsing an RED status message.
    @Test public void parseRedStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.parseStatus("SIGNALHEAD IH1 RED\n");
            jmri.SignalHead signalHead = (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1");
          Assert.assertEquals("Parse Inactive Status Check",
                       jmri.SignalHead.RED,
                       signalHead.getAppearance());
            // parsing the status also causes a message to return to
            // the client.
            Assert.assertEquals("parse Inactive check","SIGNALHEAD IH1 RED\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing RED Status");
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.parseStatus("SIGNALHEAD IH1\n");
            // nothing has changed the Signal Head, so it should be DARK.
            Assert.assertEquals("parse blank check","SIGNALHEAD IH1 DARK\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing RED Status");
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
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        try {
            a.parseStatus("SIGNALHEAD IH1 UNKNOWN\n");
            // this isn't a known state, so it should be just like
            // blank.
            // nothing has changed the Signal Head, so it should be DARK.
            Assert.assertEquals("parse blank check","SIGNALHEAD IH1 DARK\n",sb.toString());
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception parsing UNKNOWN Status");
        }
    }

    // The minimal setup for log4J
    @Before public void setUp() throws Exception {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalSignalHeadManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH1","Head 1"));
    }

    @After public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
