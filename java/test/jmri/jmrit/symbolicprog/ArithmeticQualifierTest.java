package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author	Bob Jacobsen, Copyright 2014
 */
public class ArithmeticQualifierTest {

    ProgDebugger p = new ProgDebugger();

    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    // start of base tests
    class TestArithmeticQualifier extends ArithmeticQualifier {

        TestArithmeticQualifier(VariableValue watchedVal, int value, String relation) {
            super(watchedVal, value, relation);
        }

        @Override
        public void setWatchedAvailable(boolean t) {
        }

        @Override
        public boolean currentAvailableState() {
            return true;
        }
    }

    @Test
    public void testVariableNotExistsOk() {

        ArithmeticQualifier aq = new TestArithmeticQualifier(null, 0, "exists");
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    @Test
    public void testVariableNotExistsNOk() {

        ArithmeticQualifier aq = new TestArithmeticQualifier(null, 1, "exists");
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    @Test
    public void testVariableExistsOk() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test Exists
        ArithmeticQualifier aq = new TestArithmeticQualifier(variable, 1, "exists");
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    @Test
    public void testVariableExistsNotOk() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test Exists
        ArithmeticQualifier aq = new TestArithmeticQualifier(variable, 0, "exists");
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    @Test
    public void testVariableEq() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "eq"
        ArithmeticQualifier aq = new TestArithmeticQualifier(variable, 10, "eq");
        Assert.assertEquals(false, aq.currentDesiredState());
        cv.setValue(10);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv.setValue(20);
        Assert.assertEquals(false, aq.currentDesiredState());

    }

    @Test
    public void testVariableGe() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");

        // test "ge"
        ArithmeticQualifier aq = new TestArithmeticQualifier(variable, 10, "ge");
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
        ArithmeticQualifier aq = new TestArithmeticQualifier(null, 10, "eq");
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
