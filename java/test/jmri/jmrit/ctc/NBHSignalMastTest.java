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

    private PropertyChangeListener _testListener = null;

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

        jmri.SignalAppearanceMap appearanceMap = mast.getAppearanceMap();
        Assert.assertNull(appearanceMap);

        boolean held = mast.getHeld();
        Assert.assertFalse(held);
        mast.setHeld(held);

        String displayName = mast.getDisplayName();
        Assert.assertEquals("UNKNOWN", displayName);

        mast.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        mast.removePropertyChangeListener(_testListener);
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

        jmri.SignalAppearanceMap appearanceMap = mast.getAppearanceMap();
        Assert.assertNotNull(appearanceMap);

        boolean held = mast.getHeld();
        Assert.assertFalse(held);
        mast.setHeld(held);

        String displayName = mast.getDisplayName();
        Assert.assertEquals("Good Mast", displayName);

        mast.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        mast.removePropertyChangeListener(_testListener);
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
}