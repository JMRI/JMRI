package jmri.jmris;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.AbstractSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
abstract public class AbstractSensorServerTestBase {

    protected AbstractSensorServer ss = null;

    @Test public void testCtor() {
        Assert.assertNotNull(ss);
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
        Assert.assertNotNull((jmri.InstanceManager.getDefault(jmri.SensorManager.class)).getSensor("IS1"));
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

    /**
     * pre test setup.  Must setup SensorServer ss.
     */
    @Before 
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
