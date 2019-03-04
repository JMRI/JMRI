package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.SignalMast;
import jmri.util.JUnitUtil;
import org.junit.*;

/*
* Tests for the NBHSignalMast Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class NBHSignalMastTest {

    private PropertyChangeListener _testListener1 = null;
    private PropertyChangeListener _testListener2 = null;

    @Test
    public void testGetsAndSets() {
        NBHSignalMast mast = new NBHSignalMast("No Mast");
        Assert.assertNotNull(mast);
        nullBean(mast);
        jmri.SignalMast virtMast = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($0001)", "Good Mast");
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).register(virtMast);
        virtMast.setAspect("Stop");
        mast = new NBHSignalMast("Good Mast");
        Assert.assertNotNull(mast);
        realBean(mast);
    }

    public void nullBean(NBHSignalMast mast) {
        SignalMast sigMast = (SignalMast) mast.getBean();
        Assert.assertNull(sigMast);

        String aspect = mast.getAspect();
        Assert.assertEquals("UNKNOWN", aspect);
        mast.setAspect(aspect);

        java.util.Vector<String> validAspects = mast.getValidAspects();
        Assert.assertEquals(0, validAspects.size());

        jmri.SignalSystem signalSystem = mast.getSignalSystem();
        Assert.assertNull(signalSystem);

        jmri.SignalAppearanceMap appearanceMap = mast.getAppearanceMap();
        Assert.assertNull(appearanceMap);

        String mastType = mast.getMastType();
        Assert.assertEquals("UNKNOWN", mastType);
        mast.setMastType(mastType);

        boolean lit = mast.getLit();
        Assert.assertFalse(lit);
        mast.setLit(lit);

        boolean held = mast.getHeld();
        Assert.assertFalse(held);
        mast.setHeld(held);

        boolean disabled = mast.isAspectDisabled("Stop");
        Assert.assertFalse(disabled);

        mast.setAllowUnLit(false);

        boolean unlit = mast.allowUnLit();
        Assert.assertFalse(unlit);

        String userName = mast.getUserName();
        Assert.assertEquals("UNKNOWN", userName);
        mast.setMastType(userName);

        String systemName = mast.getSystemName();
        Assert.assertEquals("UNKNOWN", systemName);

        String displayName = mast.getDisplayName();
        Assert.assertEquals("UNKNOWN", displayName);

        String fullName = mast.getFullyFormattedDisplayName();
        Assert.assertEquals("UNKNOWN", fullName);

        String comment = mast.getComment();
        Assert.assertEquals("UNKNOWN", comment);
        mast.setComment(comment);

        mast.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        mast.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        mast.removePropertyChangeListener(_testListener2);
        mast.updateListenerRef(_testListener1, "newRef");

        String ref = mast.getListenerRef(_testListener1);
        Assert.assertEquals("UNKNOWN", ref);

        java.util.ArrayList<String> refs = mast.getListenerRefs();
        Assert.assertEquals(0, refs.size());

        int num = mast.getNumPropertyChangeListeners();
        Assert.assertEquals(0, num);

        PropertyChangeListener[] numrefs = mast.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        int state = mast.getState();
        Assert.assertEquals(0, state);
        String stateName = mast.describeState(state);
        Assert.assertEquals("UNKNOWN", stateName);
        try {
            mast.vetoableChange(null);
            mast.setState(state);
        } catch (jmri.JmriException | java.beans.PropertyVetoException ex) {
            log.warn("Signal Mast exception: {}", ex);
        }

        mast.setProperty("Test", "Value");
        String property = (String) mast.getProperty("Test");
        Assert.assertNull(property);
        mast.removeProperty("Test");
        java.util.Set keys = mast.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = mast.getBeanType();
        Assert.assertEquals("UNKNOWN", type);

        mast.compareSystemNameSuffix("", "", null);
        mast.dispose();
    }

    public void realBean(NBHSignalMast mast) {
        SignalMast sigMast = (SignalMast) mast.getBean();
        Assert.assertNotNull(sigMast);

        mast.setCTCHeld(false);

        boolean danger = mast.isDanger();
        Assert.assertTrue(danger);

        String[] names = mast.getValidStateNames();
        Assert.assertEquals(0, names.length);

        int[] states = mast.getValidStates();
        Assert.assertEquals(0, states.length);

        mast.setAppearance(0);

        // SignalMast methods
        String aspect = mast.getAspect();
        Assert.assertEquals("Stop", aspect);
        mast.setAspect(aspect);

        java.util.Vector<String> validAspects = mast.getValidAspects();
        Assert.assertEquals(4, validAspects.size());

        jmri.SignalSystem signalSystem = mast.getSignalSystem();
        Assert.assertNotNull(signalSystem);

        jmri.SignalAppearanceMap appearanceMap = mast.getAppearanceMap();
        Assert.assertNotNull(appearanceMap);

        String mastType = mast.getMastType();
        Assert.assertEquals("one-searchlight", mastType);
        mast.setMastType(mastType);

        boolean lit = mast.getLit();
        Assert.assertTrue(lit);
        mast.setLit(lit);

        boolean held = mast.getHeld();
        Assert.assertFalse(held);
        mast.setHeld(held);

        boolean disabled = mast.isAspectDisabled("Stop");
        Assert.assertFalse(disabled);

        mast.setAllowUnLit(false);

        boolean unlit = mast.allowUnLit();
        Assert.assertFalse(unlit);

        String userName = mast.getUserName();
        Assert.assertEquals("Good Mast", userName);
        mast.setUserName(userName);

        String systemName = mast.getSystemName();
        Assert.assertEquals("IF$vsm:basic:one-searchlight($0001)", systemName);

        String displayName = mast.getDisplayName();
        Assert.assertEquals("Good Mast", displayName);

        String fullName = mast.getFullyFormattedDisplayName();
        Assert.assertEquals("IF$vsm:basic:one-searchlight($0001)(Good Mast)", fullName);

        String comment = mast.getComment();
        Assert.assertNull(comment);
        mast.setComment(comment);

        mast.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        mast.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        mast.removePropertyChangeListener(_testListener2);
        mast.updateListenerRef(_testListener1, "newRef");

        String ref = mast.getListenerRef(_testListener1);
        Assert.assertEquals("newRef", ref);

        java.util.ArrayList<String> refs = mast.getListenerRefs();
        Assert.assertEquals(2, refs.size());

        int num = mast.getNumPropertyChangeListeners();
        Assert.assertEquals(2, num);

        PropertyChangeListener[] numrefs = mast.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        int state = mast.getState();
        Assert.assertEquals(-1, state);
        String stateName = mast.describeState(state);
        Assert.assertEquals("Unexpected value: -1", stateName);
        try {
            mast.vetoableChange(null);
            mast.setState(state);
        } catch (jmri.JmriException | java.beans.PropertyVetoException ex) {
            log.warn("Signal Mast exception: {}", ex);
        }

        mast.setProperty("Test", "Value");
        String property = (String) mast.getProperty("Test");
        Assert.assertEquals("Value", property);
        mast.removeProperty("Test");
        java.util.Set keys = mast.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = mast.getBeanType();
        Assert.assertEquals("Signal Mast", type);

        mast.compareSystemNameSuffix("", "", null);
        mast.dispose();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultSignalMastManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NBHSignalMastTest.class);
}