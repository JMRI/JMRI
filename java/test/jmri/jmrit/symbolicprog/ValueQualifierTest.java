package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 *
 * @author	Bob Jacobsen, Copyright 2014
 * @author	Bob Jacobsen, Copyright 2017 
 */
public class ValueQualifierTest extends TestCase {

    private ProgDebugger p = new ProgDebugger();
    private VariableValue qualified = null; 
    private VariableValue watched = null; 
    private CvValue cv1 = null;
    private CvValue cv2 = null;
    HashMap<String, CvValue> v = null;

    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    public void testVariableNotExistsOk() {

        ValueQualifier aq = new ValueQualifier(qualified, null , 0, "exists");
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    public void testVariableNotExistsNOk() {

        ValueQualifier aq = new ValueQualifier(qualified, null, 1, "exists");
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    public void testVariableExistsOk() {

        // test Exists
        ValueQualifier aq = new ValueQualifier(qualified, watched, 1, "exists");
        Assert.assertEquals(true, aq.currentDesiredState());
    }

    public void testVariableExistsNotOk() {
        // test Exists
        ValueQualifier aq = new ValueQualifier(qualified, watched, 0, "exists");
        Assert.assertEquals(false, aq.currentDesiredState());
    }

    public void testVariableEq() {
        // test "eq"
        ValueQualifier aq = new ValueQualifier(qualified, watched, 10, "eq");
        Assert.assertEquals(false, aq.currentDesiredState());
        cv1.setValue(10);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv1.setValue(20);
        Assert.assertEquals(false, aq.currentDesiredState());

    }

    public void testVariableGe() {
        // test "ge"
        ValueQualifier aq = new ValueQualifier(qualified, watched, 10, "ge");
        Assert.assertEquals(false, aq.currentDesiredState());
        cv1.setValue(10);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv1.setValue(20);
        Assert.assertEquals(true, aq.currentDesiredState());
        cv1.setValue(5);
        Assert.assertEquals(false, aq.currentDesiredState());

    }

    public void testVariableRefEqNotExist() {
        // test arithmetic operation when variable not found
        ValueQualifier aq = new ValueQualifier(qualified, null, 10, "eq");
        Assert.assertEquals(true, aq.currentDesiredState()); // chosen default in this case
        jmri.util.JUnitAppender.assertErrorMessage("Arithmetic EQ operation when watched value doesn't exist");
    }

    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    // from here down is testing infrastructure
    public ValueQualifierTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ValueQualifierTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite(ValueQualifierTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        v = createCvMap();
        cv1 = new CvValue("81", p);
        cv1.setValue(3);
        v.put("81", cv1);
        cv2 = new CvValue("82", p);
        cv2.setValue(4);
        v.put("82", cv2);
        qualified = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");
        watched = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
