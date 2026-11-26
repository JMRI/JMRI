package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSignalGroupTest {

    @Test
    public void testCTor() {
        DefaultSignalGroup t = new DefaultSignalGroup("Signal Group Test");
        Assert.assertNotNull("exists",t);
    }

    private java.beans.PropertyChangeListener mastListener;

    /**
     * A group with one head that has a turnout and a sensor: shows On for the
     * mast aspect only as its AND/OR setting says. Before, a matching sensor
     * set the head On even with its turnout set the other way.
     */
    private boolean headOn(boolean and, int turnoutState, int sensorState) throws jmri.JmriException {
        jmri.SignalMast mast = org.mockito.Mockito.mock(jmri.SignalMast.class);
        org.mockito.Mockito.doAnswer(inv -> {
            mastListener = inv.getArgument(0);
            return null;
        }).when(mast).addPropertyChangeListener(org.mockito.ArgumentMatchers.any(java.beans.PropertyChangeListener.class));
        DefaultSignalGroup g = new DefaultSignalGroup("IG1", "group");
        g.setSignalMast(mast, "mast");
        g.addSignalMastAspect("Clear");
        jmri.SignalHead head = new VirtualSignalHead("IH1");
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(head);
        g.addSignalHead(head);
        g.setHeadOnState(head, jmri.SignalHead.YELLOW);
        g.setHeadOffState(head, jmri.SignalHead.DARK);
        jmri.Turnout t = jmri.InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        jmri.Sensor s = jmri.InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        g.setHeadAlignTurnout(head, t, jmri.Turnout.THROWN);
        g.setHeadAlignSensor(head, s, jmri.Sensor.ACTIVE);
        g.setSensorTurnoutOper(head, !and); // true is OR
        t.setCommandedState(turnoutState);
        s.setKnownState(sensorState);
        mastListener.propertyChange(new java.beans.PropertyChangeEvent(mast, "Aspect", "Stop", "Clear"));
        return head.getAppearance() == jmri.SignalHead.YELLOW;
    }

    @Test
    public void testAndNeedsTurnoutsAndSensors() throws jmri.JmriException {
        Assert.assertFalse("turnout set the other way", headOn(true, jmri.Turnout.CLOSED, jmri.Sensor.ACTIVE));
    }

    @Test
    public void testAndWithBothMatching() throws jmri.JmriException {
        Assert.assertTrue(headOn(true, jmri.Turnout.THROWN, jmri.Sensor.ACTIVE));
    }

    @Test
    public void testOrNeedsEither() throws jmri.JmriException {
        Assert.assertTrue("turnout set", headOn(false, jmri.Turnout.THROWN, jmri.Sensor.INACTIVE));
    }

    @Test
    public void testOrWithNeither() throws jmri.JmriException {
        Assert.assertFalse(headOn(false, jmri.Turnout.CLOSED, jmri.Sensor.INACTIVE));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(DefaultSignalGroupTest.class);

}
