package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the HexVariableValue class
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class HexVariableValueTest extends AbstractVariableValueTestBase {

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new HexVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    @Override
    void setValue(VariableValue var, String val) {
        String hexval = Integer.toHexString(Integer.valueOf(val).intValue());
        ((JTextField) var.getCommonRep()).setText(hexval);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((HexVariableValue) var).setValue(Integer.valueOf(val).intValue());
    }

    @Override
    void checkValue(VariableValue var, String comment, String val) {
        String hexval = Integer.toHexString(Integer.valueOf(val).intValue());
        Assert.assertEquals(comment, hexval, ((JTextField) var.getCommonRep()).getText());
    }

    @Override
    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        String hexval = Integer.toHexString(Integer.valueOf(val).intValue());
        Assert.assertEquals(comment, hexval, ((JLabel) var.getCommonRep()).getText());
    }

    // end of abstract members

    // test the handling of radix masks
    @Test
    public void testBaseMasks20() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "3", 0, 19, v, null, null);
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");
        // check variable value
        checkValue(variable, "value object contains ", "2");
        // see if the CV was updated
        Assert.assertEquals("cv value", 6, cv.getValue());
        
        // now check that other parts are maintained
        cv.setValue(1+2*3+3*3*20);
        // check variable value
        checkValue(variable, "value object contains ", "2");
        // see if the CV was updated
        Assert.assertEquals("cv value", (1+2*3+3*3*20), cv.getValue());

        // and try setting another value
        setValue(variable, "15");
        checkValue(variable, "value object contains ", "15");
        Assert.assertEquals("cv value", (1+15*3+3*3*20), cv.getValue());                
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
    }
    
    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
