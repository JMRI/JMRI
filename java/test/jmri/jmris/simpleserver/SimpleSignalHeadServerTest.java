package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleSignalHeadServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleSignalHeadServerTest {

    @Test
    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertThat(a).isNotNull();
    }

    @Test
    public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) {
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);        
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(jcs);
        assertThat(a).isNotNull();
    }

    // test sending a message.
    @Test
    public void testSendMessageWithConnection() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);        
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        assertDoesNotThrow(() -> {
            java.lang.reflect.Method sendMessageMethod;
            sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a,"Hello World");
        }, "failed to execute send message using refleciton");
        assertThat(jcs.getOutput()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending a message.
    @Test
    public void testSendMessage() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        assertDoesNotThrow(() -> {
           java.lang.reflect.Method sendMessageMethod;
           sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
           // override the default permissions.
            sendMessageMethod.setAccessible(true);
           sendMessageMethod.invoke(a,"Hello World");
         },"failed to execute send message using refleciton");
        assertThat(sb.toString()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending an error message.
    @Test
    public void testSendErrorStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {

                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () -> a.sendErrorStatus("IT1"),"failed to execute send error status");
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("SIGNALHEAD ERROR\n");
    }

    // test intializing a SignalHead status message.
    @Test
    public void testCheckInitSignalHead() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        Assertions.assertNotNull(new SimpleSignalHeadServer(input, output));
        assertThat(sb.toString()).withFailMessage("no status set for new signal head unless asked for").isEqualTo("");
    }

    // test sending DARK status message.
    @Test
    public void testCheckSendDarkStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () -> a.sendStatus("IH1",jmri.SignalHead.DARK),"Exception sending DARK Status");
        assertThat(sb.toString()).withFailMessage("sendStatus check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    // test sending an RED status message.
    @Test
    public void testCheckSendRedStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () -> a.sendStatus("IH1",jmri.SignalHead.RED),"Exception sending RED Status");
        assertThat(sb.toString()).withFailMessage("sendStatus check").isEqualTo("SIGNALHEAD IH1 RED\n");
    }

    // test sending an UNKNOWN status message.
    @Test
    public void testCheckSendUnkownStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () -> a.sendStatus("IH1",jmri.SignalHead.UNKNOWN),"Exception sending UNKNOWN Status");
        assertThat(sb.toString()).withFailMessage("sendStatus check").isEqualTo("SIGNALHEAD IH1 RED\n");
    }

    // test Parsing an DARK status message.
    @Test
    public void testParseDarkStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () -> a.parseStatus("SIGNALHEAD IH1 DARK\n"),"Exception parsing DARK Status");
        jmri.SignalHead signalHead = (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1");
        Assertions.assertNotNull(signalHead);
        assertThat(signalHead.getAppearance()).withFailMessage("Parse Active Status Check").isEqualTo(jmri.SignalHead.DARK);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("parse Dark check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    // test Parsing an RED status message.
    @Test
    public void testParseRedStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( ()->  a.parseStatus("SIGNALHEAD IH1 RED\n"),"Exception parsing RED Status");
        jmri.SignalHead signalHead = (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1");
        Assertions.assertNotNull(signalHead);
        assertThat(signalHead.getAppearance()).withFailMessage("Parse Inactive Status Check").isEqualTo(jmri.SignalHead.RED);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("parse Inactive check").isEqualTo("SIGNALHEAD IH1 RED\n");
    }

    // test Parsing an blank status message.
    @Test
    public void testParseBlankStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () -> a.parseStatus("SIGNALHEAD IH1\n"),"Exception parsing RED Status");
        // nothing has changed the Signal Head, so it should be DARK.
        assertThat(sb.toString()).withFailMessage("parse blank check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    // test Parsing an other status message.
    @Test
    public void testParseOtherStatus() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertDoesNotThrow( () ->  a.parseStatus("SIGNALHEAD IH1 UNKNOWN\n"),"Exception parsing UNKNOWN Status");
        // this isn't a known state, so it should be just like blank.
        // nothing has changed the Signal Head, so it should be DARK.
        assertThat(sb.toString()).withFailMessage("parse blank check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugThrottleManager();
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH1","Head 1"));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
