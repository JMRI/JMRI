package jmri.jmris.srcp;

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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
public class JmriSRCPSensorServerTest extends jmri.jmris.AbstractSensorServerTestBase {

    private Sensor sen;
    private ByteArrayOutputStream output;

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkErrorStatusSent(){
         assertThat(output.toString()).endsWith("499 ERROR unspecified error\n\r").withFailMessage("Active Message Sent");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorActiveSent(){
        assertThat(output.toString()).endsWith("100 INFO 0 FB 1 1\n\r").withFailMessage("Active Message Sent");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorInActiveSent(){
        assertThat(output.toString()).endsWith("100 INFO 0 FB 1 0\n\r").withFailMessage("Active Message Sent");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorUnknownSent(){
        assertThat(output.toString()).endsWith("411 ERROR unknown value\n\r").withFailMessage("Active Message Sent");

    }

    // test the property change sequence for an ACTIVE property change.
    @Override
    @Test
    public void testPropertyChangeOnStatus() {
        Throwable thrown = catchThrowable( () -> {
            ss.initSensor("IS1");
            sen.setState(Sensor.ACTIVE);
        });
        assertThat(thrown).withFailMessage("Exception setting Status" ).isNull();
        checkSensorActiveSent();
    }

    // test the property change sequence for an INACTIVE property change.
    @Override
    @Test
    public void testPropertyChangeOffStatus() {
        Throwable thrown = catchThrowable( () -> {
            ss.initSensor("IS1");
            sen.setState(Sensor.INACTIVE);
        });
        assertThat(thrown).withFailMessage("Exception setting Status").isNull();
        checkSensorInActiveSent();
    }

 // The minimal setup for log4J
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        instanceManagerDelegate = Mockito.mock(InstanceManagerDelegate.class);
        SensorManager sensorManager = Mockito.mock(SensorManager.class);
        Mockito.when(instanceManagerDelegate.getDefault(SensorManager.class)).thenReturn(sensorManager);
        Mockito.when(instanceManagerDelegate.getNullableDefault(SensorManager.class)).thenReturn(sensorManager);
        sen = new AbstractSensor("IS1") {

            @Override
            public void requestUpdateFromLayout() {
                // nothing to do
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
        };
        Mockito.when(sensorManager.provideSensor("IS1")).thenReturn(sen);
        Mockito.when(sensorManager.getSensor("IS1")).thenReturn(sen);

        SystemConnectionMemo memo = Mockito.mock(SystemConnectionMemo.class);
        Mockito.when(memo.getSystemPrefix()).thenReturn("I");
        Mockito.when(memo.get(SensorManager.class)).thenReturn(sensorManager);
        Mockito.when(instanceManagerDelegate.getList(SystemConnectionMemo.class)).thenReturn(Collections.singletonList(memo));

        output = new ByteArrayOutputStream();
        DataInputStream input = new java.io.DataInputStream(System.in);
        ss = new JmriSRCPSensorServer(input, output,instanceManagerDelegate);
    }

    @AfterEach
    public void tearDown() throws Exception {
        output = null;
        instanceManagerDelegate = null;
        sen.dispose();
        sen = null;
        ss.dispose();
        ss = null;
        JUnitUtil.tearDown();
    }

}
