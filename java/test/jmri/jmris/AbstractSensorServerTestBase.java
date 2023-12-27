package jmri.jmris;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.AbstractSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
abstract public class AbstractSensorServerTestBase {

    protected AbstractSensorServer ss = null;
    
    @Test public void testCtor() {
        assertThat(ss).isNotNull();
    }

    // test sending an error message.
    @Test 
    public void testSendErrorStatus() throws java.io.IOException {
        ss.sendErrorStatus("IT1");
        checkErrorStatusSent();
    }

    // test intializing a Sensor status message.
    @Test 
    public void checkInitSensor() {
        ss.initSensor("IS1");
        assertThat((InstanceManager.getDefault(SensorManager.class)).getSensor("IS1")).isNotNull();
    }

    // test sending an ACTIVE status message.
    @Test 
    public void testCheckSendActiveStatus() throws java.io.IOException{
        ss.initSensor("IS1");
        ss.sendStatus("IS1",Sensor.ACTIVE);
        checkSensorActiveSent();
    }

    // test sending an INACTIVE status message.
    @Test
    public void testCheckSendInActiveStatus() throws java.io.IOException {
        ss.initSensor("IS1");
        ss.sendStatus("IS1",Sensor.INACTIVE);
        checkSensorInActiveSent();
    }

    // test sending an UNKNOWN status message.
    @Test
    public void testCheckSendUnkownStatus() throws java.io.IOException {
        ss.initSensor("IS1");
        ss.sendStatus("IS1",Sensor.UNKNOWN);
        checkSensorUnknownSent();
    }

    // test the property change sequence for an ACTIVE property change.
    @Test
    public void testPropertyChangeOnStatus() throws Exception {
        Assertions.assertDoesNotThrow( () -> {
            ss.initSensor("IS1");
            InstanceManager.getDefault(SensorManager.class).provideSensor("IS1").setState(Sensor.ACTIVE);
        }, ("Exception setting Status"));
        checkSensorActiveSent();
    }

    // test the property change sequence for an INACTIVE property change.
    @Test
    public void testPropertyChangeOffStatus() throws Exception {
        Assertions.assertDoesNotThrow( () -> {
            ss.initSensor("IS1");
            InstanceManager.getDefault(SensorManager.class)
                            .provideSensor("IS1").setState(Sensor.INACTIVE);
        }, "Exception setting Status");
        checkSensorInActiveSent();
    }

    /**
     * pre test setup.  Must setup SensorServer ss.
     */
    abstract public void setUp(); 

    /**
     * check that an error status message was sent by the server
     */
    abstract public void checkErrorStatusSent();

    /**
     * check that an active status message was sent by the server
     */
    abstract public void checkSensorActiveSent();

    /**
     * check that an inactive status message was sent by the server
     */
    abstract public void checkSensorInActiveSent();

    /**
     * check that an unknown status message was sent by the server
     */
    abstract public void checkSensorUnknownSent();
}
