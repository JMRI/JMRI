package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTextField;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author	Bob Jacobsen Copyright 2003, 2006
 * @version
 */
public class DecVariableValueTest extends VariableValueTest {

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((DecVariableValue) var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JTextField) var.getCommonRep()).getText());
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JLabel) var.getCommonRep()).getText());
    }

    // end of abstract members
    // from here down is testing infrastructure
    public DecVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DecVariableValueTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DecVariableValueTest.class);
        return suite;
    }

}
