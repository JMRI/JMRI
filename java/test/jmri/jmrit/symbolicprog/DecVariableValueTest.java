package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTextField;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * @author	Bob Jacobsen Copyright 2003, 2006
 */
public class DecVariableValueTest extends AbstractVariableValueTestBase {

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((DecVariableValue) var).setValue(Integer.valueOf(val).intValue());
    }

    @Override
    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JTextField) var.getCommonRep()).getText());
    }

    @Override
    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JLabel) var.getCommonRep()).getText());
    }

    // end of abstract members
    
    // test the handling of offset (base) masks
    public void testBaseMasks() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "9", 0, 3, v, null, null);
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");
        // check variable value
        checkValue(variable, "value object contains ", "2");
        // see if the CV was updated
        Assert.assertEquals("cv value", 18, cv.getValue());
        
        // now check that other parts are maintained
        cv.setValue(3+2*9+81);
        // check variable value
        checkValue(variable, "value object contains ", "2");
        // see if the CV was updated
        Assert.assertEquals("cv value", 3+2*9+81, cv.getValue());

        // and try setting another value
        setValue(variable, "1");
        checkValue(variable, "value object contains ", "1");
        Assert.assertEquals("cv value", 3+9+81, cv.getValue());
                       
    }
    
    // from here down is testing infrastructure
    public DecVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DecVariableValueTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DecVariableValueTest.class);
        return suite;
    }

}
