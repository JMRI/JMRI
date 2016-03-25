// SE8cSignalHeadTest.java
package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SE8cSignalHead implmentation
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class SE8cSignalHeadTest extends TestCase {

    public void testCtor1() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");

        Assert.assertEquals("to low before", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high before", Turnout.UNKNOWN, it12.getCommandedState());

        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12));

        Assert.assertEquals("system name", "IH:SE8C:\"11\";\"12\"", s.getSystemName());

        Assert.assertEquals("to low", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high", Turnout.CLOSED, it12.getCommandedState());  // dark
    }

    public void testCtor2() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        Assert.assertEquals("system name", "IH:SE8C:\"11\";\"12\"", s.getSystemName());
        Assert.assertEquals("user name", "user name", s.getUserName());

        Assert.assertEquals("to low", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high", Turnout.CLOSED, it12.getCommandedState());  // dark
    }

    public void testCtor3() {
        // original ctor from number and user name
        SE8cSignalHead s = new SE8cSignalHead(11, "user name");

        Assert.assertEquals("system name", "LH11", s.getSystemName());
        Assert.assertEquals("user name", "user name", s.getUserName());
    }

    public void testCtor4() {
        // original ctor from number and user name
        SE8cSignalHead s = new SE8cSignalHead(11);

        Assert.assertEquals("system name", "LH11", s.getSystemName());
    }

    public void testRedState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.RED);

        Assert.assertEquals("to low after", Turnout.THROWN, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.CLOSED, it12.getCommandedState());

    }

    public void testYellowState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.YELLOW);

        Assert.assertEquals("to low after", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.THROWN, it12.getCommandedState());

    }

    public void testGreenState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.GREEN);

        Assert.assertEquals("to low after", Turnout.CLOSED, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.CLOSED, it12.getCommandedState());

    }

    public void testDarkState() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        s.setAppearance(SignalHead.DARK);

        Assert.assertEquals("to low after", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("to high after", Turnout.CLOSED, it12.getCommandedState());

    }

    public void testStateFollowing() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s1 = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        SE8cSignalHead s2 = new SE8cSignalHead(
                new NamedBeanHandle<Turnout>("11", it11),
                new NamedBeanHandle<Turnout>("12", it12),
                "user name"
        );

        s1.setAppearance(SignalHead.DARK);
        Assert.assertEquals("s2 after DARK", SignalHead.DARK, s2.getAppearance());

        s1.setAppearance(SignalHead.RED);
        Assert.assertEquals("s2 after RED", SignalHead.RED, s2.getAppearance());

        s1.setAppearance(SignalHead.GREEN);
        Assert.assertEquals("s2 after GREEN", SignalHead.GREEN, s2.getAppearance());

        s1.setAppearance(SignalHead.YELLOW);
        Assert.assertEquals("s2 after YELLOW", SignalHead.YELLOW, s2.getAppearance());

        s1.setAppearance(SignalHead.DARK);
        Assert.assertEquals("s2 after DARK", SignalHead.DARK, s2.getAppearance());

    }

    // from here down is testing infrastructure
    public SE8cSignalHeadTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SE8cSignalHeadTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SE8cSignalHeadTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
