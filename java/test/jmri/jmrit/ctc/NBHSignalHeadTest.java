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

        boolean held = head.getHeld();
        Assert.assertFalse(held);
        head.setHeld(held);

        int[] states = head.getValidStates();
        Assert.assertEquals(0, states.length);
        String[] stateNames = head.getValidStateNames();
        Assert.assertEquals(0, stateNames.length);

        head.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        head.removePropertyChangeListener(_testListener2);
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

        boolean held = head.getHeld();
        Assert.assertFalse(held);
        head.setHeld(held);

        int[] states = head.getValidStates();
        Assert.assertEquals(7, states.length);
        String[] stateNames = head.getValidStateNames();
        Assert.assertEquals(7, stateNames.length);

        head.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        head.removePropertyChangeListener(_testListener2);
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