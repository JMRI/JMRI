package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.beans.PropertyChangeListener;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Abstract base class for Turnout tests in specific jmrix.* packages
 *
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 *
 * @author Bob Jacobsen
 */
public abstract class AbstractTurnoutTestBase {

    /**
     * Implementing classes must overload to load t with actual object; create scaffolds as needed
     */
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        t = null; // to save space, as JU4 doesn't garbage collect this object
        JUnitUtil.tearDown();
    }

    /**
     * @return number of listeners registered with the TrafficController by the object under test
     * util for test clean up
     */
    abstract public int numListeners();

    abstract public void checkThrownMsgSent() throws InterruptedException;

    abstract public void checkClosedMsgSent() throws InterruptedException;

    protected Turnout t = null; // holds object under test; set by setUp()

    protected boolean listenerResult = false;
    protected int listenStatus = Turnout.UNKNOWN;
    protected java.util.List<String> propChangeNames;

    public class Listen implements PropertyChangeListener {

        public Listen(){
            propChangeNames = new java.util.ArrayList<>();
        }
        
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
            propChangeNames.add(e.getPropertyName());
            if (e.getPropertyName().equals("KnownState")) {
                listenStatus = (Integer) e.getNewValue();
            }
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        // initial commanded state when created must be UNKNOWN
        assertEquals( Turnout.UNKNOWN, t.getCommandedState(), "initial commanded state 1");
        // initial known state when created must be UNKNOWN
        assertEquals( Turnout.UNKNOWN, t.getKnownState(), "initial known state");
        assertEquals( Turnout.UNKNOWN, t.getState(), "initial commanded state 2");
    }

    @Test
    public void testAddListener() {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        assertTrue( listenerResult, "listener invoked by setUserName");
        listenerResult = false;
        t.setCommandedState(Turnout.CLOSED);
        assertTrue( listenerResult, "listener invoked by setCommandedState");
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        assertFalse( listenerResult,
            "listener should not have heard message after removeListener");
    }

    @Test
    public void testDispose() {
        t.setCommandedState(Turnout.CLOSED); // in case registration with TrafficController is deferred to after first use
        t.dispose();
        assertEquals( 0, numListeners(), "controller listeners remaining");
    }

    @Test
    public void testRemoveListenerOnDispose() {
        int startListeners =  t.getNumPropertyChangeListeners();
        t.addPropertyChangeListener(new Listen());
        assertEquals( startListeners+1, t.getNumPropertyChangeListeners(),
            "controller listener added");
        t.dispose();
        assertTrue( t.getNumPropertyChangeListeners() < 1, "controller listeners remaining < 1");
    }

    @Test
    public void testCommandClosed() throws InterruptedException {
        t.setCommandedState(Turnout.CLOSED);
        // check
        assertEquals( Turnout.CLOSED, t.getCommandedState(), "commanded state 1");
        checkClosedMsgSent();
        ((AbstractTurnout)t).setKnownStateToCommanded();
        assertEquals( Turnout.CLOSED, t.getState(), "commanded state 2");
        assertEquals( "Closed", t.describeState(t.getState()), "commanded state 3");
    }

    @Test
    public void testCommandThrown() throws InterruptedException {
        t.setCommandedState(Turnout.THROWN);
        // check
        assertEquals( Turnout.THROWN, t.getCommandedState(), "commanded state 1");
        checkThrownMsgSent();
        ((AbstractTurnout)t).setKnownStateToCommanded();
        assertEquals( Turnout.THROWN, t.getState(), "commanded state 2");
        assertEquals( "Thrown", t.describeState(t.getState()), "commanded state 3");
    }

    private static class TestSensor extends AbstractSensor {
        boolean request = false;

        private TestSensor(String sysName, String userName){
            super(sysName, userName);
        }

        @Override
        public void requestUpdateFromLayout(){
            request = true;
        }

        boolean getRequest(){
            return request;
        }

        void resetRequest(){
            request=false;
        }
    }

    @Test
    public void testRequestUpdate() throws JmriException {
        TestSensor s1 = new TestSensor("IS1", "username1");
        TestSensor s2 = new TestSensor("IS2", "username2");
        InstanceManager.sensorManagerInstance().register(s1);
        InstanceManager.sensorManagerInstance().register(s2);

        t.provideFirstFeedbackSensor("IS1");
        t.setFeedbackMode(Turnout.ONESENSOR);

        t.requestUpdateFromLayout();
        assertTrue( s1.getRequest(), "update requested, one sensor");
        s1.resetRequest();

        t.provideSecondFeedbackSensor("IS2");
        t.setFeedbackMode(Turnout.TWOSENSOR);

        t.requestUpdateFromLayout();
        assertTrue( s1.getRequest(), "update requested, two sensor s1");
        assertTrue( s2.getRequest(), "update requested, two sensor s2");
    }

    @Test
    public void testGetAndSetInverted() {
        assumeTrue(t.canInvert(), "Turnout does not invert");
        t.addPropertyChangeListener(new Listen());
        assertFalse( t.getInverted(), "Default Inverted State");
        t.setInverted(true);
        assertTrue( t.getInverted(), "set Inverted");
        assertTrue( propChangeNames.contains("inverted"), "Inverted propertychange");

        t.addPropertyChangeListener(new Listen()); // reset PCLs

        t.setInverted(false);
        assertFalse( t.getInverted(), "Unset Inverted");
        assertTrue( propChangeNames.contains("inverted"), "Inverted propertychange");
            
        
    }

    @Test
    public void testInvertedCommandClosed() throws InterruptedException {
        assumeTrue(t.canInvert(), "Turnout does not invert");
        t.setInverted(true);
        t.setCommandedState(Turnout.CLOSED);
        // check
        assertEquals( Turnout.CLOSED, t.getCommandedState(), "commanded state 1");
        checkThrownMsgSent();
        ((AbstractTurnout) t).setKnownStateToCommanded();
        assertEquals( Turnout.CLOSED, t.getState(), "commanded state 2");
        assertEquals( "Closed", t.describeState(t.getState()), "commanded state 3");

    }

    @Test
    public void testInvertedCommandThrown() throws InterruptedException {
        assumeTrue(t.canInvert(), "Turnout does not invert");
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        // check
        assertEquals( Turnout.THROWN, t.getCommandedState(), "commanded state 1");
        checkClosedMsgSent();
        ((AbstractTurnout) t).setKnownStateToCommanded();
        assertEquals( Turnout.THROWN, t.getState(), "commanded state 2");
        assertEquals( "Thrown", t.describeState(t.getState()), "commanded state 3");

    }

    @Test
    public void testSetGetReportLocked() throws InterruptedException {
        assertTrue( t.getReportLocked(), "Turnout starts reporting locked attempted access");
        t.addPropertyChangeListener(new Listen()); // reset PCLs
        t.setReportLocked(true);
        assertFalse( propChangeNames.contains("reportlocked"),
            "SetGetReportLocked nochange");

        t.setReportLocked(false);
        assertFalse( t.getReportLocked(), "SetGetReportLocked sets false");
        assertTrue( propChangeNames.contains("reportlocked"),
            "SetGetReportLocked propertychange");

        t.addPropertyChangeListener(new Listen()); // reset PCLs
        t.setReportLocked(true);
        assertTrue( t.getReportLocked(), "SetGetReportLocked sets true");
        assertTrue( propChangeNames.contains("reportlocked"),
            "SetGetReportLocked propertychange");

    }

    @Test
    public void testSetFeedbackModePCL() throws InterruptedException {
        t.setFeedbackMode(Turnout.UNKNOWN);
        assertEquals( Turnout.UNKNOWN,t.getFeedbackMode(), "No feedback set at start");

        t.addPropertyChangeListener(new Listen());
        t.setFeedbackMode(Turnout.UNKNOWN);
        assertFalse( propChangeNames.contains("feedbackchange"),
            "setFeedbackMode propertychange");

        t.setFeedbackMode(Turnout.ONESENSOR);
        assertTrue( propChangeNames.contains("feedbackchange"),
            "setFeedbackMode propertychange");
    }

    @Test
    public void testSetDecoderNamePCL() throws InterruptedException {

        // assertNull( t.getDecoderName(), "No decoder set at start");
        // In AbstractTurnout this String defaults to PushbuttonPacket.unknown , ie "None"
        // which is different to the javadoc in Turnout which indicates should return null for unset.

        // so we set it manually here so starting this particular test from a known state.
        t.setDecoderName(null);
        assertNull( t.getDecoderName(), "No decoder set at start");

        t.addPropertyChangeListener(new Listen());
        t.setDecoderName(null);
        assertFalse( propChangeNames.contains("decoderNameChange"),
            "setDecoderName no change");

        t.setDecoderName("Test Name");
        assertTrue( propChangeNames.contains("decoderNameChange"),
            "SetDecoderName propertychange");
    }

    @Test
    public void testProvideFirstFeedbackSensor() throws jmri.JmriException {
        t.addPropertyChangeListener(new Listen());
        t.provideFirstFeedbackSensor("IS1");
        assertNotNull( t.getFirstSensor(), "first feedback sensor");
        assertTrue( propChangeNames.contains("turnoutFeedbackFirstSensorChange"),
            "1st feedback sensor propertychange");
    }

    @Test
    public void testProvideSecondFeedbackSensor() throws jmri.JmriException {
        t.addPropertyChangeListener(new Listen());
        t.provideSecondFeedbackSensor("IS2");
        assertNotNull( t.getSecondSensor(), "first feedback sensor");
        assertTrue( propChangeNames.contains("turnoutFeedbackSecondSensorChange"),
            "2nd feedback sensor propertychange");
    }

    @Test
    public void testOneSensorFeedback() throws jmri.JmriException {
        Sensor s1 = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        t.setFeedbackMode(Turnout.ONESENSOR); 
        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());
        t.provideFirstFeedbackSensor("IS1");
        s1.setKnownState(Sensor.INACTIVE);
        assertEquals( Turnout.CLOSED, t.getKnownState(),
            "known state for ONESENSOR feedback Inactive");
        assertEquals( Turnout.CLOSED,listenStatus,
            "listener notified of change for ONESENSOR feedback");
        s1.setKnownState(Sensor.ACTIVE);
        assertEquals( Turnout.THROWN, listenStatus,
            "listener notified of change for ONESENSOR feedback");
        assertEquals( Turnout.THROWN, t.getKnownState(),
            "known state for ONESENSOR feedback active");

        s1.setKnownState(Sensor.UNKNOWN);

        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "unknown state for ONESENSOR feedback, was " + t.describeState(t.getKnownState()));
        assertEquals( Turnout.INCONSISTENT, listenStatus,
            "listener notified of change for ONESENSOR feedback unknown");

        s1.setKnownState(Sensor.INACTIVE);
        assertEquals( Turnout.CLOSED, t.getKnownState(),
            "known state for ONESENSOR feedback Inactive");
        assertEquals( Turnout.CLOSED, listenStatus,
            "listener notified of change for ONESENSOR feedback reset");

        s1.setKnownState(Sensor.INCONSISTENT);
        assertEquals( Turnout.INCONSISTENT, listenStatus,
            "listener notified of change for ONESENSOR feedback INCONSISTENT");
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            "INCONSISTENT state for ONESENSOR feedback");

    }

    // Order of the 2 Sensor Conditions in same order as support page jmrit/beantable/TurnoutTable.shtml
    @Test
    public void testTwoSensorFeedback() throws jmri.JmriException {
        Sensor s1 = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        Sensor s2 = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2");
        t.provideFirstFeedbackSensor("IS1");
        t.provideSecondFeedbackSensor("IS2");
        t.setFeedbackMode(Turnout.TWOSENSOR); 
        assertEquals( Turnout.UNKNOWN, t.getKnownState(),
            "known state for TWOSENSOR feedback (UNKNOWN,UNKNOWN)");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.INACTIVE);

        JUnitUtil.waitFor( () -> t.getKnownState() != Turnout.UNKNOWN,"Turnout did not go Thrown");

        assertEquals( Turnout.THROWN, t.getKnownState(),
            "state changed by TWOSENSOR feedback (Active, Inactive)");
        assertEquals( Turnout.THROWN, listenStatus,
            "listener notified of change for TWOSENSOR feedback");


        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.ACTIVE);
        assertEquals( Turnout.CLOSED, t.getKnownState(),
            "state changed by TWOSENSOR feedback (Inactive, Active)");
        assertEquals( Turnout.CLOSED, listenStatus,
            "listener notified of change for TWOSENSOR feedback ");


        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INACTIVE);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            "known state for TWOSENSOR feedback (Inactive, Inactive)");


        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.UNKNOWN);
        assertEquals( Turnout.UNKNOWN, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (UNKNOWN, UNKNOWN), was "+ t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.ACTIVE);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (ACTIVE, ACTIVE) was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.INCONSISTENT);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (ACTIVE, INCONSISTENT), was "+ t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INCONSISTENT);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (INACTIVE, INCONSISTENT) was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.ACTIVE);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (INCONSISTENT, ACTIVE) was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.INACTIVE);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (INCONSISTENT, INACTIVE) was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.ACTIVE);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (UNKNOWN, ACTIVE), was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.INACTIVE);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (UNKNOWN, INACTIVE), was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.INCONSISTENT);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (UNKNOWN, INCONSISTENT), was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.UNKNOWN);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (INCONSISTENT, UNKNOWN), was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.UNKNOWN);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (ACTIVE, UNKNOWN), was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.UNKNOWN);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (INACTIVE, UNKNOWN), was " + t.describeState(t.getKnownState()));


        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.INCONSISTENT);
        assertEquals( Turnout.INCONSISTENT, t.getKnownState(),
            () -> "state changed by TWOSENSOR feedback (INCONSISTENT, INCONSISTENT), was " + t.describeState(t.getKnownState()));
    }

    @Test
    public void testDirectFeedback() throws InterruptedException, jmri.JmriException {

        // DIRECT mode is implemented in the AbstractTurnout class, so
        // it is possible on all systems.
        if (t.getFeedbackMode() != Turnout.DIRECT) {
            t.setFeedbackMode(Turnout.DIRECT);
        }
        assertEquals(Turnout.DIRECT, t.getFeedbackMode());

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        assertEquals(Turnout.THROWN, t.getState());
        assertEquals( Turnout.THROWN, listenStatus,
            "listener notified of change for DIRECT feedback");

        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        assertEquals(Turnout.CLOSED, t.getState());
        assertEquals( Turnout.CLOSED, listenStatus,
            "listener notified of change for DIRECT feedback");
    }

    @Test
    public void testGetBeanType(){
        assertEquals( t.getBeanType(), Bundle.getMessage("BeanNameTurnout"),
            "bean type");
    }

    @Test
    public void testIsCanFollow() {
        assertFalse( t.isCanFollow(), "Abstract method should always return false");
    }

    @Test
    public void testSetLeadingTurnout() {
        assertNull( t.getLeadingTurnout(), "Defaults to null");
        t.setLeadingTurnout(new AbstractTurnout("9999") {

            @Override
            protected void forwardCommandChangeToLayout(int s) {
                // nothing to do
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean locked) {
                // nothing to do
            }
        });
        assertNull( t.getLeadingTurnout(), "Did not change from null");
    }

}
