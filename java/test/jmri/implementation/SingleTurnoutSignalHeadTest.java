package jmri.implementation;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the SingleTurnoutSignalHead implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class SingleTurnoutSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
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

    @Test
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

    @Test
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

    @Override
    public SignalHead getHeadToTest() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        return new SingleTurnoutSignalHead("IH1",
                        new jmri.NamedBeanHandle<Turnout>("IT1", t),
                        SignalHead.GREEN, SignalHead.DARK);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
