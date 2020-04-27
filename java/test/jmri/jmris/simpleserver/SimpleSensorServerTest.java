package jmri.jmris.simpleserver;

import jmri.*;
import jmri.implementation.AbstractSensor;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.PreferNumericComparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
        assertThat(a).isNotNull();
    }

    // test sending a message.
    @Test public void testSendMessage() {
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod=null;
            sendMessageMethod = ss.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(ss,"Hello World");
        });
        assertThat(thrown).withFailMessage("Exception thrown while invoking message using reflection: {}",thrown).isNull();
        assertThat(sb.toString()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
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
        Throwable thrown = catchThrowable( () -> {
           java.lang.reflect.Method sendMessageMethod=null;
           sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
           // override the default permissions.
           sendMessageMethod.setAccessible(true);
           sendMessageMethod.invoke(a,"Hello World");
        });
        assertThat(thrown).withFailMessage("Exception thrown while invoking message using reflection: {}",thrown).isNull();
        assertThat(jcs.getOutput()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
    }


    // test Parsing an ACTIVE status message.
    @Test 
    public void parseActiveStatus() throws Exception {
        ss.parseStatus("SENSOR IS1 ACTIVE\n");
        jmri.Sensor sensor = (InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1");
        assertThat(sensor.getState()).isEqualTo(jmri.Sensor.ACTIVE).withFailMessage("Parse Active Status Check");
        // parsing the status also causes a message to return to
        // the client.
        checkSensorActiveSent();
    }

    // test Parsing an INACTIVE status message.
    @Test 
    public void parseInactiveStatus() throws Exception {
         ss.parseStatus("SENSOR IS1 INACTIVE\n");
         jmri.Sensor sensor = (InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1");
         assertThat(sensor.getState()).isEqualTo(jmri.Sensor.INACTIVE).withFailMessage("Parse Inactive Status Check");
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
        assertThat(InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1")).isNotNull();
    }

    // test Parsing an blank status message.
    @Test
    public void parseBlankStatusWithOutNewLine() throws Exception {
        ss.parseStatus("SENSOR IS1");
        // nothing has changed the sensor, so it should be unknown.
        checkSensorUnknownSent();
        // verify the sensor exists, it should have been created with provideSensor.
        assertThat(InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1")).isNotNull();
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
         assertThat(sb.toString()).isEqualTo("SENSOR ERROR\n").withFailMessage("sendErrorStatus check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorActiveSent(){
         assertThat(sb.toString()).isEqualTo("SENSOR IS1 ACTIVE\n").withFailMessage("sendStatus check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorInActiveSent(){
         assertThat(sb.toString()).isEqualTo("SENSOR IS1 INACTIVE\n").withFailMessage("sendStatus check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorUnknownSent(){
         assertThat(sb.toString()).isEqualTo("SENSOR IS1 UNKNOWN\n").withFailMessage("sendStatus check");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalSensorManager();
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

    @AfterEach public void tearDown() throws Exception {
        ss.dispose();
        ss = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
