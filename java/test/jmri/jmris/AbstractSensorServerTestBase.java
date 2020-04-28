package jmri.jmris;

import jmri.InstanceManager;
import jmri.SensorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
        assertThat((InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1")).isNotNull();
    }

    // test sending an ACTIVE status message.
    @Test 
    public void CheckSendActiveStatus() throws java.io.IOException{
        ss.initSensor("IS1");
        ss.sendStatus("IS1",jmri.Sensor.ACTIVE);
        checkSensorActiveSent();
    }

    // test sending an INACTIVE status message.
    @Test
    public void CheckSendInActiveStatus() throws java.io.IOException {
        ss.initSensor("IS1");
        ss.sendStatus("IS1",jmri.Sensor.INACTIVE);
        checkSensorInActiveSent();
    }

    // test sending an UNKNOWN status message.
    @Test
    public void CheckSendUnkownStatus() throws java.io.IOException {
        ss.initSensor("IS1");
        ss.sendStatus("IS1",jmri.Sensor.UNKNOWN);
        checkSensorUnknownSent();
    }

    // test the property change sequence for an ACTIVE property change.
    @Test
    public void testPropertyChangeOnStatus() {
        Throwable thrown = catchThrowable( () -> {
            ss.initSensor("IS1");
            InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1").setState(jmri.Sensor.ACTIVE);
        });
        assertThat(thrown).withFailMessage("Exception setting Status").isNull();
        checkSensorActiveSent();
    }

    // test the property change sequence for an INACTIVE property change.
    @Test
    public void testPropertyChangeOffStatus() {
        Throwable thrown = catchThrowable( () -> {
            ss.initSensor("IS1");
            InstanceManager.getDefault(SensorManager.class)
                            .provideSensor("IS1").setState(jmri.Sensor.INACTIVE);
        });

        assertThat(thrown).withFailMessage("Exception setting Status").isNull();
        checkSensorInActiveSent();
    }

    /**
     * pre test setup.  Must setup SensorServer ss.
     */
    @BeforeEach
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
