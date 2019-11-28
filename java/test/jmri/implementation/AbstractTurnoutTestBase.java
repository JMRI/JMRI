package jmri.implementation;

import java.beans.PropertyChangeListener;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import jmri.Turnout;
import jmri.Sensor;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base class for Turnout tests in specific jmrix.* packages
 *
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 *
 * @author	Bob Jacobsen
 */
public abstract class AbstractTurnoutTestBase {

    // implementing classes must provide these abstract members:
    @Before
    abstract public void setUp();    	// load t with actual object; create scaffolds as needed

    /** 
     * @return number of listeners registered with the TrafficController by the object under test
     */
    abstract public int numListeners();

    abstract public void checkThrownMsgSent() throws InterruptedException;

    abstract public void checkClosedMsgSent() throws InterruptedException;

    protected Turnout t = null;	// holds object under test; set by setUp()

    static protected boolean listenerResult = false;
    static protected int listenStatus = Turnout.UNKNOWN;

    public class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
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
        t.setCommandedState(Turnout.CLOSED);  	// in case registration with TrafficController is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    @Test
    public void testCommandClosed() throws InterruptedException {
        t.setCommandedState(Turnout.CLOSED);
        // check
        Assert.assertEquals("commanded state 1", Turnout.CLOSED, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.CLOSED, t.getState());
        Assert.assertEquals("commanded state 3", "Closed", t.describeState(t.getState()));
        checkClosedMsgSent();
    }

    @Test
    public void testCommandThrown() throws InterruptedException {
        t.setCommandedState(Turnout.THROWN);
        // check
        Assert.assertEquals("commanded state 1", Turnout.THROWN, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.THROWN, t.getState());
        Assert.assertEquals("commanded state 3", "Thrown", t.describeState(t.getState()));
        checkThrownMsgSent();
    }

    class TestSensor extends AbstractSensor {
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
    public void testGetAndSetInverted(){
        Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
        Assert.assertFalse("Default Inverted State", t.getInverted());
        t.setInverted(true);
        Assert.assertTrue("set Inverted", t.getInverted());
    }

    @Test
    public void testInvertedCommandClosed() throws InterruptedException {
        Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
        t.setInverted(true);
        t.setCommandedState(Turnout.CLOSED);
        // check
        Assert.assertEquals("commanded state 1", Turnout.CLOSED, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.CLOSED, t.getState());
        Assert.assertEquals("commanded state 3", "Closed", t.describeState(t.getState()));
        checkThrownMsgSent();
    }

    @Test
    public void testInvertedCommandThrown() throws InterruptedException {
        Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        // check
        Assert.assertEquals("commanded state 1", Turnout.THROWN, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.THROWN, t.getState());
        Assert.assertEquals("commanded state 3", "Thrown", t.describeState(t.getState()));
        checkClosedMsgSent();
    }

    @Test
    public void testProvideFirstFeedbackSensor() throws jmri.JmriException {
        t.provideFirstFeedbackSensor("IS1");
        Assert.assertNotNull("first feedback sensor", t.getFirstSensor());
    }

    @Test
    public void testProvideSecondFeedbackSensor() throws jmri.JmriException {
        t.provideSecondFeedbackSensor("IS2");
        Assert.assertNotNull("first feedback sensor", t.getSecondSensor());
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
    }

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

        JUnitUtil.waitFor( () -> {
            return t.getKnownState() != Turnout.UNKNOWN;
        });

        Assert.assertEquals("state changed by TWOSENSOR feedback (Active, Inactive)", Turnout.THROWN, t.getKnownState());

        Assert.assertEquals("listener notified of change for TWOSENSOR feedback", Turnout.THROWN,listenStatus);

        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("known state for TWOSENSOR feedback (Inactive, Inactive)", Turnout.INCONSISTENT, t.getKnownState());

        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (Inactive, Active)", Turnout.CLOSED, t.getKnownState());

        Assert.assertEquals("listener notified of change for TWOSENSOR feedback ", Turnout.CLOSED,listenStatus);

        s1.setKnownState(Sensor.ACTIVE);
        s2.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("state changed by TWOSENSOR feedback (Active, Active)", Turnout.INCONSISTENT, t.getKnownState());
    }

    @Test 
    public void testDirectFeedback() throws Exception {

        // DIRECT mode is implemented in the AbstractTurnout class, so
	// it is possible on all systems.
	if(t.getFeedbackMode() != Turnout.DIRECT){
		t.setFeedbackMode(Turnout.DIRECT);
	}
        Assert.assertEquals(Turnout.DIRECT, t.getFeedbackMode());

	listenStatus = Turnout.UNKNOWN;
	t.addPropertyChangeListener(new Listen());
        
        // Check that state changes appropriately
        t.setCommandedState(Turnout.THROWN);
        checkThrownMsgSent();
        Assert.assertEquals(t.getState(), Turnout.THROWN);
	Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.THROWN,listenStatus);

	t.setCommandedState(Turnout.CLOSED);
        checkClosedMsgSent();
        Assert.assertEquals(t.getState(), Turnout.CLOSED);
	Assert.assertEquals("listener notified of change for DIRECT feedback", Turnout.CLOSED,listenStatus);
    }

    @Test
    public void testGetBeanType(){
         Assert.assertEquals("bean type", t.getBeanType(), Bundle.getMessage("BeanNameTurnout"));
    }

}
