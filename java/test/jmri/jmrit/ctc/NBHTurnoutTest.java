package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.setup.CreateTestObjects;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

/*
* Tests for the NBHTurnout Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class NBHTurnoutTest {

    private PropertyChangeListener _testListener1 = null;
    private PropertyChangeListener _testListener2 = null;

    @Test
    public void testGetsAndSets() {
//         CreateTestObjects.createTurnout("IT91", "IT 91");
//         CreateTestObjects.createTurnout("IT92", "IT 92");
        CreateTestObjects.createTurnout("IT93", "IT 93");

//         Use NB constructor
//         Turnout turnout91 = InstanceManager.getDefault(TurnoutManager.class).getTurnout("IT91");
//         Assert.assertNotNull(turnout91);
//         NamedBeanHandle<Turnout> nbTurnout91 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(turnout91.getUserName(), turnout91);
//         Assert.assertNotNull(nbTurnout91);
//         NBHTurnout turnout = new NBHTurnout(nbTurnout91);
//         Assert.assertNotNull(turnout);
//
//         Use regular constructor with optional true
//         NBHSensor turnout92 = new NBHSensor("Module", "UserId", "Parameter", "IS92", true);
//         Assert.assertNotNull(turnout92);

        // Use regular constructor
        NBHTurnout turnout93 = new NBHTurnout("Module", "UserId", "Parameter", "IT93", false);
        Assert.assertNotNull(turnout93);
        realBean(turnout93);

        // Use regular constructor with invalid name
        NBHTurnout turnout94 = new NBHTurnout("Module", "UserId", "Parameter", "IT94", false);
        Assert.assertNotNull(turnout94);
        nullBean(turnout94);

        JUnitAppender.suppressErrorMessage("Module, UserIdParameter, Sensor does not exist: IT94");
        JUnitAppender.suppressErrorMessage("expected Sensor 1 not defined - IT93");
        JUnitAppender.suppressErrorMessage("expected Sensor 2 not defined - IT93");
    }

// WARN  - expected Sensor 1 not defined - IT93 [main] jmri.implementation.AbstractTurnout.setInitialKnownStateFromFeedback()
// WARN  - expected Sensor 2 not defined - IT93 [main] jmri.implementation.AbstractTurnout.setInitialKnownStateFromFeedback()
// ERROR - Module, UserIdParameter, Turnout does not exist: IT94 [main] jmri.jmrit.ctc.CTCException.logError()

    public void nullBean(NBHTurnout turnout) {
        Turnout tbean = turnout.getBean();
        Assert.assertNull(tbean);

        int known = turnout.getKnownState();
        Assert.assertEquals(Turnout.CLOSED, known);

        turnout.setCommandedState(Turnout.THROWN);
        int cmd = turnout.getCommandedState();
        Assert.assertEquals(Turnout.CLOSED, cmd);

        boolean consistent = turnout.isConsistentState();
        Assert.assertFalse(consistent);

        int types = turnout.getValidFeedbackTypes();
        Assert.assertEquals(0, types);

        String[] typeNames = turnout.getValidFeedbackNames();
        Assert.assertEquals(0, typeNames.length);

        turnout.setFeedbackMode("UNKNOWN");
        String fbString = turnout.getFeedbackModeName();
        Assert.assertEquals("UNKNOWN", fbString);

        turnout.setFeedbackMode(Turnout.ONESENSOR);
        int feedback = turnout.getFeedbackMode();
        Assert.assertEquals(0, feedback);

        turnout.requestUpdateFromLayout();

        turnout.setInhibitOperation(true);
        boolean inhibit = turnout.getInhibitOperation();
        Assert.assertFalse(inhibit);

        turnout.setTurnoutOperation(null);
        jmri.TurnoutOperation top = turnout.getTurnoutOperation();
        Assert.assertNull(top);

        try {
            turnout.provideFirstFeedbackSensor("IS95");
            jmri.Sensor firstSensor = turnout.getFirstSensor();
            Assert.assertNull(firstSensor);

            turnout.provideSecondFeedbackSensor("IS96");
            jmri.Sensor secondSensor = turnout.getSecondSensor();
            Assert.assertNull(secondSensor);
        } catch (jmri.JmriException | java.lang.NullPointerException ex) {
            log.warn("Turnout provide sensor exception ", ex);
        }

        NamedBeanHandle<jmri.Sensor> firstNamed = turnout.getFirstNamedSensor();
        Assert.assertNull(firstNamed);

        NamedBeanHandle<jmri.Sensor> secondNamed = turnout.getSecondNamedSensor();
        Assert.assertNull(secondNamed);

        turnout.setInitialKnownStateFromFeedback();

        turnout.setNumberOutputBits(2);
        int bits = turnout.getNumberOutputBits();
        Assert.assertEquals(0, bits);

        turnout.setControlType(1);
        int ctype = turnout.getControlType();
        Assert.assertEquals(0, ctype);

        turnout.setInverted(true);
        boolean inverted = turnout.getInverted();
        Assert.assertFalse(inverted);
        boolean canInvert = turnout.canInvert();
        Assert.assertFalse(canInvert);

        turnout.setLocked(1, true);
        boolean locked = turnout.getLocked(1);
        Assert.assertFalse(locked);

        boolean canLock = turnout.canLock(1);
        Assert.assertFalse(canLock);

        turnout.enableLockOperation(1,false);

        int lockModes = turnout.getPossibleLockModes();
        Assert.assertEquals(0, lockModes);

        turnout.setReportLocked(true);
        boolean lockReport = turnout.getReportLocked();
        Assert.assertFalse(lockReport);

        String[] decNames = turnout.getValidDecoderNames();
        Assert.assertEquals(0, decNames.length);

        turnout.setDecoderName(null);
        String decName = turnout.getDecoderName();
        Assert.assertNotNull(decName);

        turnout.setBinaryOutput(true);

        try {
            turnout.setDivergingSpeed("Slow");
            turnout.setStraightSpeed("Normal");
        } catch (jmri.JmriException exs) {
            log.warn("Turnout spped exception ", exs);
        }

        String divSpeed = turnout.getDivergingSpeed();
        Assert.assertEquals("UNKNOWN", divSpeed);
        float divLimit = turnout.getDivergingLimit();
        Assert.assertEquals(0, divLimit, 0.00);

        String normSpeed = turnout.getStraightSpeed();
        Assert.assertEquals("UNKNOWN", normSpeed);
        float normLimit = turnout.getStraightLimit();
        Assert.assertEquals(0, normLimit, 0.00);

        String userName = turnout.getUserName();
        Assert.assertEquals("UNKNOWN", userName);
        turnout.setUserName(userName);

        String systemName = turnout.getSystemName();
        Assert.assertEquals("UNKNOWN", systemName);

        String displayName = turnout.getDisplayName();
        Assert.assertEquals("UNKNOWN", displayName);

        String fullName = turnout.getFullyFormattedDisplayName();
        Assert.assertEquals("UNKNOWN", fullName);

        String comment = turnout.getComment();
        Assert.assertNotNull(comment);
        turnout.setComment(comment);

        turnout.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        turnout.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        turnout.removePropertyChangeListener(_testListener2);
        turnout.updateListenerRef(_testListener1, "newRef");

        String ref = turnout.getListenerRef(_testListener1);
        Assert.assertEquals("UNKNOWN", ref);

        java.util.ArrayList<String> refs = turnout.getListenerRefs();
        Assert.assertEquals(0, refs.size());

        int num = turnout.getNumPropertyChangeListeners();
        Assert.assertEquals(0, num);

        PropertyChangeListener[] numrefs = turnout.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        int state = turnout.getState();
        Assert.assertEquals(0, state);
        String stateName = turnout.describeState(state);
        Assert.assertEquals("UNKNOWN", stateName);

        try {
            turnout.setState(state);
//             turnout.vetoableChange(null);
//         } catch (jmri.JmriException | java.beans.PropertyVetoException | java.lang.NullPointerException ext) {
        } catch (jmri.JmriException ext) {
            log.warn("Sensor veto exception: {}", ext);
        }

        turnout.setProperty("Test", "Value");
        String property = (String) turnout.getProperty("Test");
        Assert.assertNull(property);
        turnout.removeProperty("Test");
        java.util.Set keys = turnout.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = turnout.getBeanType();
        Assert.assertEquals("UNKNOWN", type);

        turnout.compareSystemNameSuffix("", "", null);
        turnout.dispose();
    }

    public void realBean(NBHTurnout turnout) {
        Turnout tbean = turnout.getBean();
        Assert.assertNotNull(tbean);

        int known = turnout.getKnownState();
        Assert.assertEquals(Turnout.CLOSED, known);

        turnout.setCommandedState(Turnout.THROWN);
        int cmd = turnout.getCommandedState();
        Assert.assertEquals(Turnout.THROWN, cmd);

        boolean consistent = turnout.isConsistentState();
        Assert.assertTrue(consistent);

        int types = turnout.getValidFeedbackTypes();
        Assert.assertEquals(177, types);

        String[] typeNames = turnout.getValidFeedbackNames();
        Assert.assertEquals(4, typeNames.length);

        turnout.setFeedbackMode("TWOSENSOR");
        String fbString = turnout.getFeedbackModeName();
        Assert.assertEquals("TWOSENSOR", fbString);

        turnout.setFeedbackMode(Turnout.ONESENSOR);
        int feedback = turnout.getFeedbackMode();
        Assert.assertEquals(16, feedback);

        turnout.requestUpdateFromLayout();

        turnout.setInhibitOperation(true);
        boolean inhibit = turnout.getInhibitOperation();
        Assert.assertTrue(inhibit);

        turnout.setTurnoutOperation(null);
        jmri.TurnoutOperation top = turnout.getTurnoutOperation();
        Assert.assertNull(top);

        try {
            turnout.provideFirstFeedbackSensor("IS95");
            jmri.Sensor firstSensor = turnout.getFirstSensor();
            Assert.assertEquals("IS95", firstSensor.getSystemName());

            turnout.provideSecondFeedbackSensor("IS96");
            jmri.Sensor secondSensor = turnout.getSecondSensor();
            Assert.assertEquals("IS96", secondSensor.getSystemName());
        } catch (jmri.JmriException ex) {
            log.warn("Turnout provide sensor exception ", ex);
        }

        NamedBeanHandle<jmri.Sensor> firstNamed = turnout.getFirstNamedSensor();
        Assert.assertNotNull(firstNamed);

        NamedBeanHandle<jmri.Sensor> secondNamed = turnout.getSecondNamedSensor();
        Assert.assertNotNull(secondNamed);

        turnout.setInitialKnownStateFromFeedback();

        turnout.setNumberOutputBits(2);
        int bits = turnout.getNumberOutputBits();
        Assert.assertEquals(2, bits);

        turnout.setControlType(1);
        int ctype = turnout.getControlType();
        Assert.assertEquals(1, ctype);

        turnout.setInverted(true);
        boolean inverted = turnout.getInverted();
        Assert.assertTrue(inverted);
        boolean canInvert = turnout.canInvert();
        Assert.assertFalse(canInvert);

        turnout.setLocked(1, true);
        boolean locked = turnout.getLocked(1);
        Assert.assertFalse(locked);

        boolean canLock = turnout.canLock(1);
        Assert.assertFalse(canLock);

        turnout.enableLockOperation(1,false);

        int lockModes = turnout.getPossibleLockModes();
        Assert.assertEquals(0, lockModes);

        turnout.setReportLocked(true);
        boolean lockReport = turnout.getReportLocked();
        Assert.assertTrue(lockReport);

        String[] decNames = turnout.getValidDecoderNames();
        Assert.assertEquals(4, decNames.length);

        turnout.setDecoderName(null);
        String decName = turnout.getDecoderName();
        Assert.assertNull(decName);

        turnout.setBinaryOutput(true);

        try {
            turnout.setDivergingSpeed("Slow");
            turnout.setStraightSpeed("Normal");
        } catch (jmri.JmriException exs) {
            log.warn("Turnout spped exception ", exs);
        }

        String divSpeed = turnout.getDivergingSpeed();
        Assert.assertEquals("Slow", divSpeed);
        float divLimit = turnout.getDivergingLimit();
        Assert.assertEquals(30, divLimit, 0.00);

        String normSpeed = turnout.getStraightSpeed();
        Assert.assertEquals("Normal", normSpeed);
        float normLimit = turnout.getStraightLimit();
        Assert.assertEquals(100, normLimit, 0.00);

        String userName = turnout.getUserName();
        Assert.assertEquals("IT 93", userName);
        turnout.setUserName(userName);

        String systemName = turnout.getSystemName();
        Assert.assertEquals("IT93", systemName);

        String displayName = turnout.getDisplayName();
        Assert.assertEquals("IT 93", displayName);

        String fullName = turnout.getFullyFormattedDisplayName();
        Assert.assertEquals("IT93(IT 93)", fullName);

        String comment = turnout.getComment();
        Assert.assertNull(comment);
        turnout.setComment(comment);

        turnout.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        turnout.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        turnout.removePropertyChangeListener(_testListener2);
        turnout.updateListenerRef(_testListener1, "newRef");

        String ref = turnout.getListenerRef(_testListener1);
        Assert.assertEquals("newRef", ref);

        java.util.ArrayList<String> refs = turnout.getListenerRefs();
        Assert.assertEquals(2, refs.size());

        int num = turnout.getNumPropertyChangeListeners();
        Assert.assertEquals(2, num);

        PropertyChangeListener[] numrefs = turnout.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        int state = turnout.getState();
        Assert.assertEquals(1, state);
        String stateName = turnout.describeState(state);
        Assert.assertEquals("Unknown", stateName);

        try {
            turnout.setState(state);
//             turnout.vetoableChange(null);
//         } catch (jmri.JmriException | java.beans.PropertyVetoException | java.lang.NullPointerException ext) {
        } catch (jmri.JmriException ext) {
            log.warn("Sensor veto exception: {}", ext);
        }

        turnout.setProperty("Test", "Value");
        String property = (String) turnout.getProperty("Test");
        Assert.assertEquals("Value", property);
        turnout.removeProperty("Test");
        java.util.Set keys = turnout.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = turnout.getBeanType();
        Assert.assertEquals("Turnout", type);

        turnout.compareSystemNameSuffix("", "", null);
        turnout.dispose();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NBHTurnoutTest.class);
}