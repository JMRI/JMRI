package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.*;
import jmri.jmrit.ctc.setup.CreateTestObjects;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.openide.util.Exceptions;

/*
* Tests for the NBHSensor Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class NBHSensorTest {

    private PropertyChangeListener _testListener = null;

    @Test
    public void testGetsAndSets() {
        CreateTestObjects.createSensor("IS91", "IS 91");
        CreateTestObjects.createSensor("IS92", "IS 92");
        CreateTestObjects.createSensor("IS93", "IS 93");

        // Use NB constructor
        Sensor sensor91 = InstanceManager.getDefault(SensorManager.class).getSensor("IS91");
        Assert.assertNotNull(sensor91);
        NamedBeanHandle<Sensor> nbSensor91 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor91.getUserName(), sensor91);
        Assert.assertNotNull(nbSensor91);
        NBHSensor sensor = new NBHSensor(nbSensor91);
        Assert.assertNotNull(sensor);

        // Use regular constructor with optional true
        NBHSensor sensor92 = new NBHSensor("Module", "UserId", "Parameter", "IS92", true);
        Assert.assertNotNull(sensor92);

        // Use regular constructor with optional false
        NBHSensor sensor93 = new NBHSensor("Module", "UserId", "Parameter", "IS93", true);
        Assert.assertNotNull(sensor93);
        realBean(sensor93);

        // Use regular constructor with optional false and invalid
        NBHSensor sensor94 = new NBHSensor("Module", "UserId", "Parameter", "IS94", true);
        Assert.assertNotNull(sensor94);
        nullBean(sensor94);

        JUnitAppender.suppressErrorMessage("Module, UserIdParameter, Sensor does not exist: IS94");
    }
    
    @Test
    public void testHandleNameModification() {
        
/*  Next, test in NBHSensor the ability to dynmaically change the underlying sensor used
    WITHOUT affecting registered PropertyChangeListeners....
*/
//  Create and initialize standard JMRI sensors:
        InstanceManager.getDefault(SensorManager.class).newSensor("IS:FLEETING", "FLEETING");   // Create it if it doesn't exist.
        Sensor sensorFleeting2 = InstanceManager.getDefault(SensorManager.class).newSensor("IS:FLEETING2", "FLEETING2");
        try { sensorFleeting2.setKnownState(Sensor.INACTIVE); } catch (JmriException ex) {} // Shouldn't throw, since it's a standard JMRI object.

//  Our initial NBHSensor, associate with "IS:FLEETING":
        NBHSensor sensorFleetingNBH = new NBHSensor("Module", "UserId", "Parameter", "FLEETING", false);
        Assert.assertEquals(0, sensorFleetingNBH.testingGetCountOfPropertyChangeListenersRegistered());     // Verify nothing registered yet.

//  Setup for the test:        
        PropertyChangeListener propertyChangeListener;
        AtomicInteger booleanContainer = new AtomicInteger(0);
        sensorFleetingNBH.setKnownState(Sensor.INACTIVE);
        
        sensorFleetingNBH.addPropertyChangeListener(propertyChangeListener = (PropertyChangeEvent e) -> { booleanContainer.incrementAndGet(); });
        Assert.assertEquals(1, sensorFleetingNBH.testingGetCountOfPropertyChangeListenersRegistered());
        sensorFleetingNBH.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals(1, booleanContainer.get());     // Make sure it works so far.
        
//  Simulate the user changing the sensor contained in the NBHSensor to something else:        
        sensorFleetingNBH.setHandleName("FLEETING2");
        Assert.assertEquals(1, sensorFleetingNBH.testingGetCountOfPropertyChangeListenersRegistered()); // We BETTER still be registered!

// Simulate as if SOMETHING OTHER THAN OUR CODE changed the state of sensor FLEETING2:
        try { sensorFleeting2.setKnownState(Sensor.ACTIVE); } catch (JmriException ex) {}   // Shouldn't throw, since it's a standard JMRI object.
        
//  Did our PropertyChangeEvent happen?
//  This is what all this led up to, the REAL test!:        
        Assert.assertEquals(2, booleanContainer.get());
        
//  Clean up, and make sure our bookkeeping worked fine:
        sensorFleetingNBH.removePropertyChangeListener(propertyChangeListener);
        Assert.assertEquals(0, sensorFleetingNBH.testingGetCountOfPropertyChangeListenersRegistered());
    }
    
    public void nullBean(NBHSensor sensor) {
        Sensor sbean = sensor.getBean();
        Assert.assertNull(sbean);
        boolean match = sensor.matchSensor(sbean);
        Assert.assertFalse(match);

        sensor.setKnownState(Sensor.ACTIVE);
        int known = sensor.getKnownState();
        Assert.assertEquals(Sensor.INACTIVE, known);

        sensor.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        sensor.removePropertyChangeListener(_testListener);
    }

    public void realBean(NBHSensor sensor) {
        Sensor sbean = sensor.getBean();
        Assert.assertNotNull(sbean);
        boolean match = sensor.matchSensor(sbean);
        Assert.assertTrue(match);

        sensor.setKnownState(Sensor.ACTIVE);
        int known = sensor.getKnownState();
        Assert.assertEquals(Sensor.ACTIVE, known);

        sensor.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        sensor.removePropertyChangeListener(_testListener);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        // stop any BlockBossLogic threads created
        JUnitUtil.clearBlockBossLogic();

        jmri.util.JUnitUtil.tearDown();
    }
}
