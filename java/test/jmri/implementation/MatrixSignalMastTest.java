package jmri.implementation;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the MatrixSignalMast implementation
 *
 * @author	Egbert Broerse Copyright (C) 2016
 */
public class MatrixSignalMastTest {

    @Test
    public void testSetup() {
        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        Assert.assertEquals("it11 before", Turnout.UNKNOWN, it11.getCommandedState());
        Assert.assertEquals("it12 before", Turnout.UNKNOWN, it12.getCommandedState());
        Assert.assertEquals("it13 before", Turnout.UNKNOWN, it13.getCommandedState());
    }

    @Test
    @SuppressWarnings("unused") // it11 etc. are indirectly used as NamedBeans IT11 etc.
    public void testCtor1() {
        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11");
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        Assert.assertEquals("system name", "IF$xsm:basic:one-low($0001)-3t", m.getSystemName());
        Assert.assertEquals("user name", "user", m.getUserName());
        //System.out.println(it11.getFullyFormattedDisplayName()); //debug
        Assert.assertEquals("output2", "IT12", m.outputsToBeans.get("output2").getName());
    }

    @Test
    public void testHeld() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assert.assertTrue(!m.getHeld());

        m.setHeld(true);
        Assert.assertTrue(m.getHeld());

        m.setHeld(false);
        Assert.assertTrue(!m.getHeld());
    }

    @Test
    @SuppressWarnings("unused") // it11 etc. are indirectly used as NamedBeans IT11 etc.
    public void testLit() {
        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11"); // Note: "IT" added to name by system
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        m.setBitstring("Clear", "111");
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001");
        m.setBitstring("Unlit", "000");

        m.setAllowUnLit(true);
        m.setUnLitBits("000");

        m.aspect = "Clear"; // define some initial aspect before setting any aspect
        // wait for outputs and outputbits to be set

        Assert.assertTrue(m.getLit());

        m.setLit(false);
        Assert.assertTrue(!m.getLit());

        m.setLit(true);
        Assert.assertTrue(m.getLit());
    }

    @Test
    @SuppressWarnings("unused") // it11 etc. are indirectly used as NamedBeans IT11 etc.
    public void testAspects() {
        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11");
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        m.setBitstring("Clear", "111"); // used for test below
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001"); // used for test below
        m.setBitstring("Unlit", "000");

        m.setAllowUnLit(true);
        m.setUnLitBits("000");
        m.setAspectEnabled("Clear");
        m.setAspectEnabled("Approach");
        m.setAspectEnabled("Stop");
        m.setAspectEnabled("Unlit");

        m.aspect = "Stop"; // define some initial aspect before setting any aspect
        // wait for outputs and outputbits to be set

        log.debug(java.util.Arrays.toString(m.getBitsForAspect("Clear"))); //debug
        Assert.assertEquals("check bitarray for stop", "[0, 0, 1]", java.util.Arrays.toString(m.getBitsForAspect("Stop")));

        m.setAspect("Clear");
        Assert.assertEquals("check clear", "Clear", m.getAspect());
        Assert.assertEquals("it12 for Clear", Turnout.CLOSED, it12.getCommandedState());
        m.setAspect("Stop");
        Assert.assertEquals("check stop", "Stop", m.getAspect());
        Assert.assertEquals("it12 for Stop", Turnout.THROWN, it12.getCommandedState());
    }

    public void testAspectAttributes() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        m.setAspect("Clear");
        Assert.assertEquals("../../../resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-287.gif",
                m.getAppearanceMap().getProperty("Clear", "imagelink"));
    }

    @Test
    public void testAspectNotSet() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assert.assertNull("check null", m.getAspect());
    }

    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

    private final static Logger log = LoggerFactory.getLogger(MatrixSignalMastTest.class);
}
