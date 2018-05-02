package jmri.jmrit.symbolicprog.tabbedframe;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.DecVariableValue;
import jmri.jmrit.symbolicprog.VariableValue;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Some tests in this file are derived from the test for ArthmeticQualifier.
 *
 * @author Bob Jacobsen, Copyright 2014
 * @author Paul Bender Copyright (C) 2017
 */
public class JComponentQualifierTest {

    private JPanel jp = null;

    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    @Test
    public void testVariableNotExistsOk() {

        JComponentQualifier aq = new JComponentQualifier(jp,null, 0, "exists");
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    @Test
    public void testVariableNotExistsNOk() {

        JComponentQualifier aq = new JComponentQualifier(jp,null, 1, "exists");
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    @Test
    public void testVariableExistsOk() {
        HashMap<String, CvValue> v = createCvMap();
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test Exists
        JComponentQualifier aq = new JComponentQualifier(jp,variable, 1, "exists");
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    @Test
    public void testVariableExistsNotOk() {
        HashMap<String, CvValue> v = createCvMap();
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test Exists
        JComponentQualifier aq = new JComponentQualifier(jp,variable, 0, "exists");
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    @Test
    public void testVariableEq() {
        HashMap<String, CvValue> v = createCvMap();
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "eq"
        JComponentQualifier aq = new JComponentQualifier(jp,variable, 10, "eq");
        Assert.assertEquals(false, aq.currentDesiredState());
        cv.setValue(10);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv.setValue(20);
        Assert.assertEquals(false, aq.currentDesiredState());

    }

    @Test
    public void testVariableGe() {
        HashMap<String, CvValue> v = createCvMap();
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "ge"
        JComponentQualifier aq = new JComponentQualifier(jp,variable, 10, "ge");
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
        // test arithmetic operation when variable not found
        JComponentQualifier aq = new JComponentQualifier(jp,null, 10, "eq");
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
        jp = new JPanel();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
