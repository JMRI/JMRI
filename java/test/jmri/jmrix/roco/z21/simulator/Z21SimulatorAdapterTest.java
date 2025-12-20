package jmri.jmrix.roco.z21.simulator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Z21SimulatorAdapterTest.java
 * <p>
 * Test for the jmri.jmrix.roco.z21.simulator.z21SimulatorAdapter class
 *
 * @author Paul Bender Copyright (C) 2016
 */
@Timeout(10)
public class Z21SimulatorAdapterTest {

    private java.net.InetAddress host;
    private final static int PORT = 21105; // default port for Z21 connections.
    private Z21SimulatorAdapter a = null;

    @Test
    public void testCtor() {
        assertNotNull(a);
    }

    /*
     * Test that the Z21 simulator correctly sets up the network connection.
     */
    @Test
    @Disabled("test is currently too unreliable in CI environments.  The class under test frequently fails to bind to the port")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testConnection() {
        // connect the port
        assertDoesNotThrow( () ->
            a.connect() ,"Exception configuring server port");

        // and configure/start the simulator.
        a.configure();

        // try connecting to the port.
        try (DatagramSocket socket = new DatagramSocket()) {
            // create a datagram with the data from a valid message.
            // this is a request to get the serial number.
            byte data[] = {0x04, 0x00, 0x10, 0x00};
            DatagramPacket sendPacket = new DatagramPacket(data, 4, host, PORT);
            // and send it.

            assertDoesNotThrow( () ->
                a.getSocket().send(sendPacket),"IOException writing to network port");

            assertDoesNotThrow( () -> {
                byte buffer[] = new byte[100];
                DatagramPacket p = new DatagramPacket(buffer, 100, host, PORT);
                // set the timeout on the socket.
                try {
                    a.getSocket().setSoTimeout(5000); // 5 second timeout.
                } catch (java.net.SocketException timeoutse) {
                    // this is not a fatal error for this test, just
                    // an optimization in case something went wrong.
                }
                a.getSocket().receive(p);
                assertTrue( p.getLength() > 0, "received data from simulator");
            }, "Exception reading from network port");

        } catch (java.net.SocketException se) {
            assertNull(se, "Failure Creating Socket");
        }
    }

    @Test
    public void railComDataChangedReply() {
        cannedMessageCheck("getZ21RailComDataChangedReply", "04 00 88 00");
    }

    @Test
    public void hardwareVersionReply() {
        cannedMessageCheck("getHardwareVersionReply", "0C 00 1A 00 00 02 00 00 20 01 00 00");
    }

    @Test
    public void xPressNetUnknownCommandReply() {
        cannedMessageCheck("getXPressNetUnknownCommandReply", "07 00 40 00 61 82 E3");
    }

    @Test
    public void z21SerialNumberReply() {
        cannedMessageCheck("getZ21SerialNumberReply", "08 00 10 00 00 00 00 00");
    }

    @Test
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void Z21BroadCastFlagsReply() {
        cannedMessageCheck("getZ21BroadCastFlagsReply", "08 00 51 00 00 00 00 00");
    }

    private void cannedMessageCheck(String methodName, String expectedReply) {
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method method;
        try {
            method = a.getClass().getDeclaredMethod(methodName);
        } catch ( NoSuchMethodException nsm) {
            fail("Could not find method " + methodName + "in Z21SimulatorAdapter class");
            return;
        }

        // override the default permissions.
        assertNotNull(method);
        method.setAccessible(true);

        try {
            Z21Reply z = (Z21Reply) method.invoke(a);
            assertNotNull(z);
            assertEquals( expectedReply, z.toString(), methodName + " return value");
        } catch ( IllegalAccessException iae) {
            fail("Could not access method " + methodName + " in Z21SimulatorAdapter class", iae);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            fail(methodName + " executon failed reason: ", cause != null ? cause : ite);
        }
    }

    @Test
    public void testAddressedProgrammerManager() {
        // connect the port
        assertDoesNotThrow( () ->
            a.connect(), "Exception configuring server port");

        // and configure/start the simulator.
        a.configure();
        assertTrue(a.getSystemConnectionMemo().provides(jmri.AddressedProgrammerManager.class));
    }

    // verify there is a Reporter manager
    @Test
    public void testReporterManager() {
        assertTrue(a.getSystemConnectionMemo().provides(jmri.ReporterManager.class));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

        host = assertDoesNotThrow( () ->
            java.net.InetAddress.getLocalHost(), "Unable to create host localhost");
        // create a new simulator.
        a = new Z21SimulatorAdapter();
    }

    @AfterEach
    public void tearDown() {
        if (a.getSystemConnectionMemo() != null) {
            if (a.getSystemConnectionMemo().getTrafficController() != null) {
                a.getSystemConnectionMemo().getTrafficController().terminateThreads();
            }
            a.getSystemConnectionMemo().dispose();
        }
        a.terminateThread();
        JUnitUtil.waitThreadTerminated("z21.Z21TrafficController Transmit thread");
        JUnitUtil.waitThreadTerminated("z21.Z21XNetStreamPortController$1 Transmit thread");
        // suppress two timeout messages that occur
        JUnitAppender.suppressWarnMessageStartsWith("Timeout on reply to message:");
        JUnitAppender.suppressWarnMessageStartsWith("Timeout on reply to message:");
        a.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
