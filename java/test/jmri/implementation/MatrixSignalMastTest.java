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
 * Tests for the MatrixSignalMast implementation.
 *
 * @author	Egbert Broerse Copyright (C) 2016, 2019
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
        //System.out.println(it11.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)); //debug
        Assert.assertEquals("output2", "IT12", m.outputsToBeans.get("output2").getName());
    }

    @Test
    public void testHeld() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assert.assertFalse(m.getHeld());

        m.setHeld(true);
        Assert.assertTrue(m.getHeld());

        m.setHeld(false);
        Assert.assertFalse(m.getHeld());
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
        m.setMatrixMastCommandDelay(0);
        // wait for outputs and outputbits to be set

        //log.debug(java.util.Arrays.toString(m.getBitsForAspect("Stop")));
        Assert.assertEquals("check bitarray for Stop", "[0, 0, 1]", java.util.Arrays.toString(m.getBitsForAspect("Stop")));

        m.setAspect("Clear");
        Assert.assertEquals("check Clear", "Clear", m.getAspect());
        JUnitUtil.waitFor( ()->{ return it11.getCommandedState() == Turnout.CLOSED; }, "it11 for Clear" );
        m.setAspect("Stop");
        Assert.assertEquals("check Stop", "Stop", m.getAspect());
        JUnitUtil.waitFor( ()->{ return it12.getCommandedState() == Turnout.THROWN; }, "it12 for Stop" );
        // it12 state is more fragile
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

    @Test
    public void testSetDelay() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assert.assertEquals("initial mast delay 0", 0, m.getMatrixMastCommandDelay());
        m.setMatrixMastCommandDelay(150);
        Assert.assertEquals("get new mast delay", 150, m.getMatrixMastCommandDelay());
        m.setMatrixMastCommandDelay(0);
    }

    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(MatrixSignalMastTest.class);

}
