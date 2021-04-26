package jmri.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyChangeListener;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
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

    static protected boolean listenerResult = false;
    static protected int listenStatus = Turnout.UNKNOWN;
    static protected java.util.List<String> propChangeNames;

    public static class Listen implements PropertyChangeListener {

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
        Assert.assertEquals("initial commanded state 1", Turnout.UNKNOWN, t.getCommandedState());
        // initial known state when created must be UNKNOWN
        Assert.assertEquals("initial known state", Turnout.UNKNOWN, t.getKnownState());
        Assert.assertEquals("initial commanded state 2", Turnout.UNKNOWN, t.getState());
    }

    @Test
    public void testAddListener() {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener invoked by setUserName", listenerResult);
        listenerResult = false;
        t.setCommandedState(Turnout.CLOSED);
        Assert.assertTrue("listener invoked by setCommandedState", listenerResult);
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertFalse("listener should not have heard message after removeListener",
                listenerResult);
    }

    @Test
    public void testDispose() {
        t.setCommandedState(Turnout.CLOSED); // in case registration with TrafficController is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    @Test
    public void testRemoveListenerOnDispose() {
        int startListeners =  t.getNumPropertyChangeListeners();
        t.addPropertyChangeListener(new Listen());
        Assert.assertEquals("controller listener added", startListeners+1, t.getNumPropertyChangeListeners());
        t.dispose();
        Assert.assertTrue("controller listeners remaining < 1", t.getNumPropertyChangeListeners() < 1);
    }

    @Test
    public void testCommandClosed() throws InterruptedException {
        t.setCommandedState(Turnout.CLOSED);
        // check
        Assert.assertEquals("commanded state 1", Turnout.CLOSED, t.getCommandedState());
        checkClosedMsgSent();
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.CLOSED, t.getState());
        Assert.assertEquals("commanded state 3", "Closed", t.describeState(t.getState()));
    }

    @Test
    public void testCommandThrown() throws InterruptedException {
        t.setCommandedState(Turnout.THROWN);
        // check
        Assert.assertEquals("commanded state 1", Turnout.THROWN, t.getCommandedState());
        checkThrownMsgSent();
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.THROWN, t.getState());
        Assert.assertEquals("commanded state 3", "Thrown", t.describeState(t.getState()));
    }

    static class TestSensor extends AbstractSensor {
            public boolean request = false;

            public TestSensor(String sysName, String userName){
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
        Assert.assertTrue("update requested, one sensor", s1.getRequest());
        s1.resetRequest();

        t.provideSecondFeedbackSensor("IS2");
        t.setFeedbackMode(Turnout.TWOSENSOR);

        t.requestUpdateFromLayout();
        Assert.assertTrue("update requested, two sensor s1", s1.getRequest());
        Assert.assertTrue("update requested, two sensor s2", s2.getRequest());
    }

    @Test
    public void testGetAndSetInverted() {
        if (t.canInvert()) {
            t.addPropertyChangeListener(new Listen());
            Assert.assertFalse("Default Inverted State", t.getInverted());
            t.setInverted(true);
            Assert.assertTrue("set Inverted", t.getInverted());
            Assert.assertTrue("Inverted propertychange",propChangeNames.contains("inverted"));
            
            t.addPropertyChangeListener(new Listen()); // reset PCLs
            
            t.setInverted(false);
            Assert.assertFalse("Unset Inverted", t.getInverted());
            Assert.assertTrue("Inverted propertychange",propChangeNames.contains("inverted"));
            
        }
    }

    @Test
    public void testInvertedCommandClosed() throws InterruptedException {
        if (t.canInvert()) {
            //Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
            t.setInverted(true);
            t.setCommandedState(Turnout.CLOSED);
            // check
            Assert.assertEquals("commanded state 1", Turnout.CLOSED, t.getCommandedState());
            checkThrownMsgSent();
            ((AbstractTurnout) t).setKnownStateToCommanded();
            Assert.assertEquals("commanded state 2", Turnout.CLOSED, t.getState());
            Assert.assertEquals("commanded state 3", "Closed", t.describeState(t.getState()));
        }
    }

    @Test
    public void testInvertedCommandThrown() throws InterruptedException {
        if (t.canInvert()) {
            //Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
            t.setInverted(true);
            t.setCommandedState(Turnout.THROWN);
            // check
            Assert.assertEquals("commanded state 1", Turnout.THROWN, t.getCommandedState());
            checkClosedMsgSent();
            ((AbstractTurnout) t).setKnownStateToCommanded();
            Assert.assertEquals("commanded state 2", Turnout.THROWN, t.getState());
            Assert.assertEquals("commanded state 3", "Thrown", t.describeState(t.getState()));
        }
    }

    @Test
    public void testSetGetReportLocked() throws InterruptedException {
        Assert.assertTrue("Turnout starts reporting locked attempted access",t.getReportLocked());
        t.addPropertyChangeListener(new Listen()); // reset PCLs
        t.setReportLocked(true);
        Assert.assertFalse("SetGetReportLocked nochange",
            propChangeNames.contains("reportlocked"));
        
        t.setReportLocked(false);
        Assert.assertFalse("SetGetReportLocked sets false",t.getReportLocked());
        Assert.assertTrue("SetGetReportLocked propertychange",
            propChangeNames.contains("reportlocked"));
        
        t.addPropertyChangeListener(new Listen()); // reset PCLs
        t.setReportLocked(true);
        Assert.assertTrue("SetGetReportLocked sets true",t.getReportLocked());
        Assert.assertTrue("SetGetReportLocked propertychange",
            propChangeNames.contains("reportlocked"));
        
    }
    
    @Test
    public void testSetFeedbackModePCL() throws InterruptedException {
        t.setFeedbackMode(Turnout.UNKNOWN);
        Assert.assertEquals("No feedback set at start",Turnout.UNKNOWN,t.getFeedbackMode());
        
        t.addPropertyChangeListener(new Listen());
        t.setFeedbackMode(Turnout.UNKNOWN);
        Assert.assertFalse("setFeedbackMode propertychange",
            propChangeNames.contains("feedbackchange"));
        
        t.setFeedbackMode(Turnout.ONESENSOR);
        Assert.assertTrue("setFeedbackMode propertychange",
            propChangeNames.contains("feedbackchange"));
    }
    
    @Test
    public void testSetDecoderNamePCL() throws InterruptedException {
        
        // Assert.assertEquals("No decoder set at start",null,t.getDecoderName());
        // In AbstractTurnout this String defaults to PushbuttonPacket.unknown , ie "None"
        // which is different to the javadoc in Turnout which indicates should return null for unset.
        
        // so we set it manually here so starting this particular test from a known state.
        t.setDecoderName(null);
        Assert.assertEquals("No decoder set at start",null,t.getDecoderName());
        
        t.addPropertyChangeListener(new Listen());
        t.setDecoderName(null);
        Assert.assertFalse("setDecoderName no change",
            propChangeNames.contains("decoderNameChange"));
        
        t.setDecoderName("Test Name");
        Assert.assertTrue("SetDecoderName propertychange",
            propChangeNames.contains("decoderNameChange"));
    }
    
    @Test
    public void testProvideFirstFeedbackSensor() throws jmri.JmriException {
        t.addPropertyChangeListener(new Listen());
        t.provideFirstFeedbackSensor("IS1");
        Assert.assertNotNull("first feedback sensor", t.getFirstSensor());
        Assert.assertTrue("1st feedback sensor propertychange",
            propChangeNames.contains("turnoutFeedbackFirstSensorChange"));
    }

    @Test
    public void testProvideSecondFeedbackSensor() throws jmri.JmriException {
        t.addPropertyChangeListener(new Listen());
        t.provideSecondFeedbackSensor("IS2");
        Assert.assertNotNull("first feedback sensor", t.getSecondSensor());
        Assert.assertTrue("2nd feedback sensor propertychange",
            propChangeNames.contains("turnoutFeedbackSecondSensorChange"));
    }

    @Test
    public void testOneSensorFeedback() throws jmri.JmriException {
        Sensor s1 = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        t.setFeedbackMode(Turnout.ONESENSOR); 
        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());
        t.provideFirstFeedbackSensor("IS1");
        s1.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("known state for ONESENSOR feedback Inactive", Turnout.CLOSED, t.getKnownState());
        Assert.assertEquals("listener notified of change for ONESENSOR feedback", Turnout.CLOSED,listenStatus);
        s1.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("listener notified of change for ONESENSOR feedback", Turnout.THROWN,listenStatus);
        Assert.assertEquals("known state for ONESENSOR feedback active", Turnout.THROWN, t.getKnownState());
        
        s1.setKnownState(Sensor.UNKNOWN);
        
        Assert.assertEquals("unknown state for ONESENSOR feedback ", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        Assert.assertEquals("listener notified of change for ONESENSOR feedback unknown", Turnout.INCONSISTENT,listenStatus);
        
        s1.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("known state for ONESENSOR feedback Inactive", Turnout.CLOSED, t.getKnownState());
        Assert.assertEquals("listener notified of change for ONESENSOR feedback reset", Turnout.CLOSED,listenStatus);
        
        s1.setKnownState(Sensor.INCONSISTENT);
        Assert.assertEquals("listener notified of change for ONESENSOR feedback INCONSISTENT", Turnout.INCONSISTENT,listenStatus);
        Assert.assertEquals("INCONSISTENT state for ONESENSOR feedback", Turnout.INCONSISTENT, t.getKnownState());
        
    }

    // Order of the 2 Sensor Conditions in same order as support page jmrit/beantable/TurnoutTable.shtml
    @Test
    public void testTwoSensorFeedback() throws jmri.JmriException {
        Sensor s1 = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        Sensor s2 = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2");
        t.provideFirstFeedbackSensor("IS1");
        t.provideSecondFeedbackSensor("IS2");
        t.setFeedbackMode(Turnout.TWOSENSOR); 
        Assert.assertEquals("known state for TWOSENSOR feedback (UNKNOWN,UNKNOWN)", Turnout.UNKNOWN, t.getKnownState());

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.INACTIVE);

        JUnitUtil.waitFor( () -> t.getKnownState() != Turnout.UNKNOWN);

        Assert.assertEquals("state changed by TWOSENSOR feedback (Active, Inactive)", Turnout.THROWN, t.getKnownState());
        Assert.assertEquals("listener notified of change for TWOSENSOR feedback", Turnout.THROWN,listenStatus);


        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (Inactive, Active)", Turnout.CLOSED, t.getKnownState());
        Assert.assertEquals("listener notified of change for TWOSENSOR feedback ", Turnout.CLOSED,listenStatus);

        
        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("known state for TWOSENSOR feedback (Inactive, Inactive)", Turnout.INCONSISTENT, t.getKnownState());

        
        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.UNKNOWN);
        Assert.assertEquals("state changed by TWOSENSOR feedback (UNKNOWN, UNKNOWN)", t.describeState(Turnout.UNKNOWN), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (ACTIVE, ACTIVE)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.INCONSISTENT);
        Assert.assertEquals("state changed by TWOSENSOR feedback (ACTIVE, INCONSISTENT)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INCONSISTENT);
        Assert.assertEquals("state changed by TWOSENSOR feedback (INACTIVE, INCONSISTENT)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));

        
        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (INCONSISTENT, ACTIVE)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (INCONSISTENT, INACTIVE)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (UNKNOWN, INACTIVE)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (UNKNOWN, INACTIVE)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.UNKNOWN);
        s2.setKnownState(Sensor.INCONSISTENT);
        Assert.assertEquals("state changed by TWOSENSOR feedback (UNKNOWN, INCONSISTENT)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.UNKNOWN);
        Assert.assertEquals("state changed by TWOSENSOR feedback (INCONSISTENT, UNKNOWN)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.UNKNOWN);
        Assert.assertEquals("state changed by TWOSENSOR feedback (ACTIVE, UNKNOWN)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.UNKNOWN);
        Assert.assertEquals("state changed by TWOSENSOR feedback (INACTIVE, UNKNOWN)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
        
        
        s1.setKnownState(Sensor.INCONSISTENT);
        s2.setKnownState(Sensor.INCONSISTENT);
        Assert.assertEquals("state changed by TWOSENSOR feedback (INCONSISTENT, INCONSISTENT)", t.describeState(Turnout.INCONSISTENT), t.describeState(t.getKnownState()));
    }

    @Test
    public void testDirectFeedback() throws Exception {

        // DIRECT mode is implemented in the AbstractTurnout class, so
        // it is possible on all systems.
        if (t.getFeedbackMode() != Turnout.DIRECT) {
            t.setFeedbackMode(Turnout.DIRECT);
        }
        Assert.assertEquals(Turnout.DIRECT, t.getFeedbackMode());

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        Assert.assertEquals(Turnout.THROWN, t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.THROWN, listenStatus);

        t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        Assert.assertEquals(Turnout.CLOSED, t.getState());
        Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.CLOSED, listenStatus);
    }

    @Test
    public void testGetBeanType(){
         Assert.assertEquals("bean type", t.getBeanType(), Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testIsCanFollow() {
        assertThat(t.isCanFollow()).as("Abstract method should always return false").isFalse();
    }

    @Test
    public void testSetLeadingTurnout() {
        assertThat(t.getLeadingTurnout()).as("Defaults to null").isNull();
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
        assertThat(t.getLeadingTurnout()).as("Did not change from null").isNull();
    }

}
