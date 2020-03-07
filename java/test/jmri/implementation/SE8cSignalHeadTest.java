package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SE8cSignalHead implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * updated to JUnit4 2016
 */
public class SE8cSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testCtor1() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");

        Assert.assertEquals("to low before", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high before", Turnout.UNKNOWN, it12.getCommandedState());

        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12));

        Assert.assertEquals("system name", "IH:SE8c:\"11\";\"12\"", s.getSystemName());

        Assert.assertEquals("to low", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high", Turnout.CLOSED, it12.getCommandedState());  // dark
    }

    @Test
    public void testCtor2() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );

        Assert.assertEquals("system name", "IH:SE8c:\"11\";\"12\"", s.getSystemName());
        Assert.assertEquals("user name", "user name", s.getUserName());

        Assert.assertEquals("to low", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high", Turnout.CLOSED, it12.getCommandedState());  // dark
    }

    @Test
    public void testCtor3() {
        // original ctor from number and user name
        SE8cSignalHead s = new SE8cSignalHead(11, "user name");

        Assert.assertEquals("system name", "LH11", s.getSystemName());
        Assert.assertEquals("user name", "user name", s.getUserName());
    }

    @Test
    public void testCtor4() {
        // original ctor from number only 
        SE8cSignalHead s = new SE8cSignalHead(11);

        Assert.assertEquals("system name", "LH11", s.getSystemName());
    }

    @Test
    public void testRedState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.RED);

        Assert.assertEquals("to low after", Turnout.THROWN, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.CLOSED, it12.getCommandedState());

    }

    @Test
    public void testYellowState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.YELLOW);

        Assert.assertEquals("to low after", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.THROWN, it12.getCommandedState());

    }

    @Test
    public void testGreenState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.GREEN);

        Assert.assertEquals("to low after", Turnout.CLOSED, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.CLOSED, it12.getCommandedState());

    }

    @Test
    public void testDarkState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.DARK);

        Assert.assertEquals("to low after", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.CLOSED, it12.getCommandedState());

    }

    // from here down is testing infrastructure

    @Override
    public SignalHead getHeadToTest() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        return new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );
    }

    // The minimal setup for log4J/JUnit4
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
