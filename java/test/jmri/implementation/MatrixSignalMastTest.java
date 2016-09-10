package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalMast;
import jmri.SignalSystem;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
    public void testCtor1() {
        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "11");
        m.setOutput("output2", "12");
        m.setOutput("output3", "13");

        Assert.assertEquals("system name", "IF$xsm:basic:one-low($0001)-3t", m.getSystemName());
        Assert.assertEquals("user name", "user", m.getUserName());
        //Assert.assertEquals("output2", "12", m.outputsToBeans.get("output2").getName());
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
    public void testLit() {
        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "11");
        m.setOutput("output2", "12");
        m.setOutput("output3", "13");

        m.setBitstring("Clear", "111");
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001");
        m.setBitstring("Unlit", "000");

        m.setAllowUnLit(true);
        m.setUnLitBits("000");

        Assert.assertTrue(m.getLit());
        // wait for outputs and outputbits to be set

        //m.setLit(false);
        //Assert.assertTrue(!m.getLit());

        m.setLit(true);
        Assert.assertTrue(m.getLit());
    }

    @Test
    public void testAspects() {

        // provide 3 turnouts:
        Turnout it11 = InstanceManager.turnoutManagerInstance().provideTurnout("11");
        Turnout it12 = InstanceManager.turnoutManagerInstance().provideTurnout("12");
        Turnout it13 = InstanceManager.turnoutManagerInstance().provideTurnout("13");

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "11");
        m.setOutput("output2", "12");
        m.setOutput("output3", "13");

        m.setBitstring("Clear", "111");
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001");
        m.setBitstring("Unlit", "000");

//        m.setAspect("Clear");
//        Assert.assertEquals("check clear", "Clear", m.getAspect());
//        m.setAspect("Stop");
//        Assert.assertEquals("check stop", "Stop", m.getAspect());
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
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

    static protected Logger log = LoggerFactory.getLogger(SignalHeadSignalMastTest.class.getName());
}
