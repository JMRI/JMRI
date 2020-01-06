package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    
    // test the handling of radix masks
    @Test
    public void testBaseMasks3() {
        log.trace("testBaseMasks3");
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create a variable pointed at CV 81
        //      Mask = 9, minVal = 0, maxVal = 2
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "9", 0, 2, v, null, null);
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

    @Test
    public void testBaseMasksDecimalValues() {
        log.trace("testBaseMasksDecimalValues");
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create variables pointed at CV 81
        //  Upper:  Mask = 10, minVal = 0, maxVal = 9
        //  Lower:  Mask =  1, minVal = 0, maxVal = 9
        VariableValue variableU = makeVar("upper", "comment", "", false, false, false, false, "81", "10", 0, 9, v, null, null);
        VariableValue variableL = makeVar("lower", "comment", "", false, false, false, false, "81",  "1", 0, 9, v, null, null);
        checkValue(variableU, "upper initially contains ", "0");
        checkValue(variableL, "lower initially contains ", "0");
        Assert.assertEquals("cv value", 0, cv.getValue());

        // pretend you've edited the upper value & manually notify
        setValue(variableU, "2");
        // see if the CV was updated
        Assert.assertEquals("cv value", 20, cv.getValue());
        // check variable values
        checkValue(variableU, "value object contains ", "2");
        checkValue(variableL, "value object contains ", "0");
        
        // set CV value
        cv.setValue(31);
        checkValue(variableU, "value object contains ", "3");
        checkValue(variableL, "value object contains ", "1");

        setValue(variableL, "9");
        // check variable values
        checkValue(variableU, "value object contains ", "3");
        checkValue(variableL, "value object contains ", "9");
        // see if the CV was updated
        Assert.assertEquals("cv value", 39, cv.getValue());
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

    private final static  org.slf4j.Logger log =  org.slf4j.LoggerFactory.getLogger(DecVariableValueTest.class);
}
