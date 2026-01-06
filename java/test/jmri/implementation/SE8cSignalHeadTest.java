package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SE8cSignalHead implementation
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * updated to JUnit4 2016
 */
public class SE8cSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testSE8cSignalHeadCtor1() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");

        assertEquals( Turnout.UNKNOWN, it11.getCommandedState(), "to low before");
        assertEquals( Turnout.UNKNOWN, it12.getCommandedState(), "to high before");

        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12));

        assertEquals( "IH:SE8c:\"11\";\"12\"", s.getSystemName(), "system name");

        assertEquals( Turnout.UNKNOWN, it11.getCommandedState(), "to low");
        assertEquals( Turnout.CLOSED, it12.getCommandedState(), "to high");  // dark
    }

    @Test
    public void testSE8cSignalHeadCtor2() {
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        SE8cSignalHead s = new SE8cSignalHead(
                new NamedBeanHandle<>("11", it11),
                new NamedBeanHandle<>("12", it12),
                "user name"
        );

        assertEquals( "IH:SE8c:\"11\";\"12\"", s.getSystemName(), "system name");
        assertEquals( "user name", s.getUserName(), "user name");

        assertEquals( Turnout.UNKNOWN, it11.getCommandedState(), "to low");
        assertEquals( Turnout.CLOSED, it12.getCommandedState(), "to high");  // dark
    }

    @Test
    public void testSE8cSignalHeadCtor3() {
        // original ctor from number and user name
        SE8cSignalHead s = new SE8cSignalHead(11, "user name");

        assertEquals( "LH11", s.getSystemName(), "system name");
        assertEquals( "user name", s.getUserName(), "user name");
    }

    @Test
    public void testSE8cSignalHeadCtor4() {
        // original ctor from number only 
        SE8cSignalHead s = new SE8cSignalHead(11);

        assertEquals( "LH11", s.getSystemName(), "system name");
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

        assertEquals( Turnout.THROWN, it11.getCommandedState(), "to low after");
        assertEquals( Turnout.CLOSED, it12.getCommandedState(), "to high after");

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

        assertEquals( Turnout.UNKNOWN, it11.getCommandedState(), "to low after");
        assertEquals( Turnout.THROWN, it12.getCommandedState(), "to high after");

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

        assertEquals( Turnout.CLOSED, it11.getCommandedState(), "to low after");
        assertEquals( Turnout.CLOSED, it12.getCommandedState(), "to high after");

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

        assertEquals( Turnout.UNKNOWN, it11.getCommandedState(), "to low after");
        assertEquals( Turnout.CLOSED, it12.getCommandedState(), "to high after");

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
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
