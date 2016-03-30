package jmri.implementation;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.Turnout;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the SingleTurnoutSignalHead implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class SingleTurnoutSignalHeadTest extends TestCase {

    public void testNoDarkValidTypes() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        SingleTurnoutSignalHead h
                = new SingleTurnoutSignalHead("IH1",
                        new jmri.NamedBeanHandle<Turnout>("IT1", t),
                        SignalHead.GREEN, SignalHead.RED);

        int[] states = h.getValidStates();
        Assert.assertEquals(2, states.length);
        Assert.assertEquals(SignalHead.GREEN, states[0]);
        Assert.assertEquals(SignalHead.RED, states[1]);

        String[] names = h.getValidStateNames();
        Assert.assertEquals(2, names.length);
        Assert.assertEquals("Green", names[0]);
        Assert.assertEquals("Red", names[1]);

        h.setAppearance(SignalHead.GREEN);
        Assert.assertEquals(SignalHead.GREEN, h.getAppearance());
        Assert.assertEquals("Green", h.getAppearanceName());

        h.setAppearance(SignalHead.RED);
        Assert.assertEquals(SignalHead.RED, h.getAppearance());
        Assert.assertEquals("Red", h.getAppearanceName());

    }

    public void testDarkValidTypes1() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        SingleTurnoutSignalHead h
                = new SingleTurnoutSignalHead("IH1",
                        new jmri.NamedBeanHandle<Turnout>("IT1", t),
                        SignalHead.DARK, SignalHead.RED);

        int[] states = h.getValidStates();
        Assert.assertEquals(3, states.length);
        Assert.assertEquals(SignalHead.DARK, states[0]);
        Assert.assertEquals(SignalHead.RED, states[1]);
        Assert.assertEquals(SignalHead.FLASHRED, states[2]);

        String[] names = h.getValidStateNames();
        Assert.assertEquals(3, names.length);
        Assert.assertEquals("Dark", names[0]);
        Assert.assertEquals("Red", names[1]);
        Assert.assertEquals("Flashing Red", names[2]);

        h.setAppearance(SignalHead.DARK);
        Assert.assertEquals(SignalHead.DARK, h.getAppearance());
        Assert.assertEquals("Dark", h.getAppearanceName());

        h.setAppearance(SignalHead.FLASHRED);
        Assert.assertEquals(SignalHead.FLASHRED, h.getAppearance());
        Assert.assertEquals("Flashing Red", h.getAppearanceName());

        h.setAppearance(SignalHead.RED);   // stops flash timer
        Assert.assertEquals(SignalHead.RED, h.getAppearance());
        Assert.assertEquals("Red", h.getAppearanceName());

    }

    public void testDarkValidTypes2() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        SingleTurnoutSignalHead h
                = new SingleTurnoutSignalHead("IH1",
                        new jmri.NamedBeanHandle<Turnout>("IT1", t),
                        SignalHead.GREEN, SignalHead.DARK);

        int[] states = h.getValidStates();
        Assert.assertEquals(3, states.length);
        Assert.assertEquals(SignalHead.GREEN, states[0]);
        Assert.assertEquals(SignalHead.FLASHGREEN, states[1]);
        Assert.assertEquals(SignalHead.DARK, states[2]);

        String[] names = h.getValidStateNames();
        Assert.assertEquals(3, names.length);
        Assert.assertEquals("Green", names[0]);
        Assert.assertEquals("Flashing Green", names[1]);
        Assert.assertEquals("Dark", names[2]);

        h.setAppearance(SignalHead.DARK);
        Assert.assertEquals(SignalHead.DARK, h.getAppearance());
        Assert.assertEquals("Dark", h.getAppearanceName());

        h.setAppearance(SignalHead.FLASHGREEN);
        Assert.assertEquals(SignalHead.FLASHGREEN, h.getAppearance());
        Assert.assertEquals("Flashing Green", h.getAppearanceName());

        h.setAppearance(SignalHead.GREEN);   // stops flash timer
        Assert.assertEquals(SignalHead.GREEN, h.getAppearance());
        Assert.assertEquals("Green", h.getAppearanceName());

    }

    // from here down is testing infrastructure
    public SingleTurnoutSignalHeadTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SingleTurnoutSignalHeadTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SingleTurnoutSignalHeadTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
    static protected Logger log = LoggerFactory.getLogger(SingleTurnoutSignalHeadTest.class.getName());
}
