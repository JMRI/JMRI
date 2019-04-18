package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.SignalHead;
import jmri.util.JUnitUtil;
import org.junit.*;

/*
* Tests for the NBHSignalHead Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class NBHSignalHeadTest {

    private PropertyChangeListener _testListener1 = null;
    private PropertyChangeListener _testListener2 = null;

    @Test
    public void testGetsAndSets() {
        NBHSignalHead head = new NBHSignalHead("No Head");
        Assert.assertNotNull(head);
        nullBean(head);

        SignalHead signalhead = new jmri.implementation.VirtualSignalHead("IH99", "Good Head");
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(signalhead);
        signalhead.setAppearance(SignalHead.RED);

        head = new NBHSignalHead("Good Head");
        Assert.assertNotNull(head);
        realBean(head);
    }

    public void nullBean(NBHSignalHead head) {
        SignalHead sigHead = (SignalHead) head.getBean();
        Assert.assertNull(sigHead);

        head.setCTCHeld(false);

        boolean danger = head.isDanger();
        Assert.assertFalse(danger);

        int appearance = head.getAppearance();
        Assert.assertEquals(0, appearance);
        head.setAppearance(appearance);

        String appearanceName = head.getAppearanceName();
        Assert.assertEquals("UNKNOWN", appearanceName);
        appearanceName = head.getAppearanceName(4);
        Assert.assertEquals("UNKNOWN", appearanceName);

        boolean lit = head.getLit();
        Assert.assertFalse(lit);
        head.setLit(lit);

        boolean held = head.getHeld();
        Assert.assertFalse(held);
        head.setHeld(held);

        int[] states = head.getValidStates();
        Assert.assertEquals(0, states.length);
        String[] stateNames = head.getValidStateNames();
        Assert.assertEquals(0, stateNames.length);

        boolean cleared = head.isCleared();
        Assert.assertFalse(cleared);

        boolean restricting = head.isShowingRestricting();
        Assert.assertFalse(restricting);

        boolean atStop = head.isAtStop();
        Assert.assertFalse(atStop);

        String userName = head.getUserName();
        Assert.assertEquals("UNKNOWN", userName);
        head.setUserName(userName);

        String systemName = head.getSystemName();
        Assert.assertEquals("UNKNOWN", systemName);

        String displayName = head.getDisplayName();
        Assert.assertEquals("UNKNOWN", displayName);

        String fullName = head.getFullyFormattedDisplayName();
        Assert.assertEquals("UNKNOWN", fullName);

        String comment = head.getComment();
        Assert.assertEquals("UNKNOWN", comment);
        head.setComment(comment);

        head.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        head.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        head.removePropertyChangeListener(_testListener2);
        head.updateListenerRef(_testListener1, "newRef");

        String ref = head.getListenerRef(_testListener1);
        Assert.assertEquals("UNKNOWN", ref);

        java.util.ArrayList<String> refs = head.getListenerRefs();
        Assert.assertEquals(0, refs.size());

        int num = head.getNumPropertyChangeListeners();
        Assert.assertEquals(0, num);

        PropertyChangeListener[] numrefs = head.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        int state = head.getState();
        Assert.assertEquals(0, state);
        String stateName = head.describeState(state);
        Assert.assertEquals("UNKNOWN", stateName);
        try {
            head.vetoableChange(new PropertyChangeEvent(this, "XYZ", 0, 1));
            head.setState(state);
        } catch (jmri.JmriException | java.beans.PropertyVetoException ex) {
            log.warn("Signal Head exception: {}", ex);
        }

        head.setProperty("Test", "Value");
        String property = (String) head.getProperty("Test");
        Assert.assertNull(property);
        head.removeProperty("Test");
        java.util.Set keys = head.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = head.getBeanType();
        Assert.assertEquals("UNKNOWN", type);

        head.compareSystemNameSuffix("", "", null);
        head.dispose();
    }

    public void realBean(NBHSignalHead head) {
        SignalHead sigHead = (SignalHead) head.getBean();
        Assert.assertNotNull(sigHead);

        head.setCTCHeld(false);

        boolean danger = head.isDanger();
        Assert.assertTrue(danger);

        int appearance = head.getAppearance();
        Assert.assertEquals(1, appearance);
        head.setAppearance(appearance);

        String appearanceName = head.getAppearanceName();
        Assert.assertEquals("Red", appearanceName);
        appearanceName = head.getAppearanceName(4);
        Assert.assertEquals("Yellow", appearanceName);

        boolean lit = head.getLit();
        Assert.assertTrue(lit);
        head.setLit(lit);

        boolean held = head.getHeld();
        Assert.assertFalse(held);
        head.setHeld(held);

        int[] states = head.getValidStates();
        Assert.assertEquals(7, states.length);
        String[] stateNames = head.getValidStateNames();
        Assert.assertEquals(7, stateNames.length);

        boolean cleared = head.isCleared();
        Assert.assertFalse(cleared);

        boolean restricting = head.isShowingRestricting();
        Assert.assertFalse(restricting);

        boolean atStop = head.isAtStop();
        Assert.assertTrue(atStop);

        String userName = head.getUserName();
        Assert.assertEquals("Good Head", userName);
        head.setUserName(userName);

        String systemName = head.getSystemName();
        Assert.assertEquals("IH99", systemName);

        String displayName = head.getDisplayName();
        Assert.assertEquals("Good Head", displayName);

        String fullName = head.getFullyFormattedDisplayName();
        Assert.assertEquals("IH99(Good Head)", fullName);

        String comment = head.getComment();
        Assert.assertNull(comment);
        head.setComment(comment);

        head.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        head.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        head.removePropertyChangeListener(_testListener2);
        head.updateListenerRef(_testListener1, "newRef");

        String ref = head.getListenerRef(_testListener1);
        Assert.assertEquals("newRef", ref);

        java.util.ArrayList<String> refs = head.getListenerRefs();
        Assert.assertEquals(2, refs.size());

        int num = head.getNumPropertyChangeListeners();
        Assert.assertEquals(2, num);

        PropertyChangeListener[] numrefs = head.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        int state = head.getState();
        Assert.assertEquals(1, state);
        String stateName = head.describeState(state);
        Assert.assertEquals("Unknown", stateName);
        try {
            head.vetoableChange(new PropertyChangeEvent(this, "XYZ", 0, 1));
            head.setState(state);
        } catch (jmri.JmriException | java.beans.PropertyVetoException ex) {
            log.warn("Signal Head exception: {}", ex);
        }

        head.setProperty("Test", "Value");
        String property = (String) head.getProperty("Test");
        Assert.assertEquals("Value", property);
        head.removeProperty("Test");
        java.util.Set keys = head.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = head.getBeanType();
        Assert.assertEquals("Signal Head", type);

        head.compareSystemNameSuffix("", "", null);
        head.dispose();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NBHSignalMastTest.class);
}