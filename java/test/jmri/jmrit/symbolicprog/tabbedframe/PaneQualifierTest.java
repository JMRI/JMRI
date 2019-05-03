package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.DecVariableValue;
import jmri.jmrit.symbolicprog.VariableValue;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Some tests in this file are derived from the test for ArthmeticQualifier.
 *
 * @author Bob Jacobsen, Copyright 2014
 * @author Paul Bender Copyright (C) 2017
 */
public class PaneQualifierTest {

    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    @Test
    public void testVariableNotExistsOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        PaneQualifier aq = new PaneQualifier(jp,null, 0, "exists",jtp,0);
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    @Test
    public void testVariableNotExistsNOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        PaneQualifier aq = new PaneQualifier(jp,null, 1, "exists",jtp,0);
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    @Test
    public void testVariableExistsOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test Exists
        PaneQualifier aq = new PaneQualifier(jp,variable, 1, "exists",jtp,0);
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    @Test
    public void testVariableExistsNotOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test Exists
        PaneQualifier aq = new PaneQualifier(jp,variable, 0, "exists",jtp,0);
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    @Test
    public void testVariableEq() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "eq"
        PaneQualifier aq = new PaneQualifier(jp,variable, 10, "eq",jtp,0);
        Assert.assertEquals(false, aq.currentDesiredState());
        cv.setValue(10);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv.setValue(20);
        Assert.assertEquals(false, aq.currentDesiredState());

    }

    @Test
    public void testVariableGe() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "ge"
        PaneQualifier aq = new PaneQualifier(jp,variable, 10, "ge",jtp,0);
        Assert.assertEquals(false, aq.currentDesiredState());
        cv.setValue(10);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv.setValue(20);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv.setValue(5);
        Assert.assertEquals(false, aq.currentDesiredState());

    }

    @Test
    public void testVariableRefEqNotExist() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PaneProgPane jp = new PaneProgPane();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add(jp);
        // test arithmetic operation when variable not found
        PaneQualifier aq = new PaneQualifier(jp,null, 10, "eq",jtp,0);
        Assert.assertEquals(true, aq.currentDesiredState()); // chosen default in this case
        jmri.util.JUnitAppender.assertErrorMessage("Arithmetic EQ operation when watched value doesn't exist");
    }

    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
