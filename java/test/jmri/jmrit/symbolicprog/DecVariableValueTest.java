package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.progdebugger.ProgDebugger;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen Copyright 2003, 2006
 */
public class DecVariableValueTest extends AbstractVariableValueTestBase {

    // Local tests version of makeVar with settable parameters and cvList support.
    private DecVariableValue makeVarDec(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item, int offset, int factor) {
        ProgDebugger pp = new ProgDebugger();

        CvValue cvNext = new CvValue(cvNum, pp);
        cvNext.setValue(0);
        v.put(cvName, cvNext);
        return new DecVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item, offset, factor);
    }

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new DecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item);
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((DecVariableValue) var).setValue(Integer.parseInt(val));
    }

    @Override
    void checkValue(VariableValue var, String comment, String val) {
        Assertions.assertEquals(val, ((JTextField) var.getCommonRep()).getText(), comment);
    }

    @Override
    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assertions.assertEquals(val, ((JLabel) var.getCommonRep()).getText(), comment);
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
        VariableValue variable = makeVar("label", "comment", "", false,
                false, false, false, "81", "9", 0, 2,
                v, null, null);
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");
        // check variable value
        checkValue(variable, "value object contains ", "2");
        // see if the CV was updated
        Assertions.assertEquals(18, cv.getValue(), "cv value");

        // now check that other parts are maintained
        cv.setValue(3 + 2 * 9 + 81);
        // check variable value
        checkValue(variable, "value object contains ", "2");
        // see if the CV was updated
        Assertions.assertEquals(3 + 2 * 9 + 81, cv.getValue(), "cv value");

        // and try setting another value
        setValue(variable, "1");
        checkValue(variable, "value object contains ", "1");
        Assertions.assertEquals(3 + 9 + 81, cv.getValue(), "cv value");
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
        VariableValue variableL = makeVar("lower", "comment", "", false, false, false, false, "81", "1", 0, 9, v, null, null);
        checkValue(variableU, "upper initially contains ", "0");
        checkValue(variableL, "lower initially contains ", "0");
        Assertions.assertEquals(0, cv.getValue(), "cv value");

        // pretend you've edited the upper value & manually notify
        setValue(variableU, "2");
        // see if the CV was updated
        Assertions.assertEquals(20, cv.getValue(), "cv value");
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
        Assertions.assertEquals(39, cv.getValue(), "cv value");
    }

    // test handling of out of range entered value when focus is lost (e.g.tab key)
    @Test
    public void testTextOutOfRangeValueEnteredFocusLost() {
        String name = "Decimal Field";
        String comment = "";
        String cvName = "33";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "33";
        String mask = "VVVVVVVV";
        int minVal = 3;
        int maxVal = 31;
        int offset = 0;
        int factor = 1;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        DecVariableValue var = makeVarDec(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, stdname, offset, factor);
        Assertions.assertNotNull(var, "makeVar returned null");

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(1, cv.length, "number of CVs is");

        Assertions.assertEquals("33", cv[0].number(), "cv[0] is");

        // start with a valid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("5");
        var.focusLost(focusEvent);
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(5, cv[0].getValue(), "set CV" + cv[0].number());

        // change text to an invalid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("54");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another invalid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("Fred");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to an out-of-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal + 1));
        var.focusLost(focusEvent);
        // ensure value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another out-of-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(minVal - 1));
        var.focusLost(focusEvent);
        // ensure value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to a just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal));
        var.focusLost(focusEvent);
        // ensure value unchanged
        Assertions.assertEquals(Integer.toString(maxVal), ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(minVal));
        var.focusLost(focusEvent);
        // ensure value unchanged
        Assertions.assertEquals(Integer.toString(minVal), ((JTextField) var.getCommonRep()).getText(), "set var text value");
    }

    // test handling of out of range entered value when action performed (e.g.enter key)
    @Test
    public void testTextOutOfRangeValueEnteredActionPerformed() {
        String name = "Decimal Field";
        String comment = "";
        String cvName = "174";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "174";
        String mask = "XXXVVVVV";
        int minVal = 4;
        int maxVal = 28;
        int offset = 0;
        int factor = 1;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        DecVariableValue var = makeVarDec(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, stdname, offset, factor);
        Assertions.assertNotNull(var, "makeVar returned null");

        ActionEvent actionEvent = new ActionEvent(var.getCommonRep(), ActionEvent.ACTION_PERFORMED, name);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(1, cv.length, "number of CVs is");

        Assertions.assertEquals("174", cv[0].number(), "cv[0] is");

        // start with a valid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("5");
        var.actionPerformed(actionEvent);
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(5, cv[0].getValue(), "set CV" + cv[0].number());

        // change text to an invalid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("54");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another invalid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("Fred");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to an out-of-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal + 1));
        var.actionPerformed(actionEvent);
        // ensure value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another out-of-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(minVal - 1));
        var.actionPerformed(actionEvent);
        // ensure value unchanged
        Assertions.assertEquals("5", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to a just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal));
        var.actionPerformed(actionEvent);
        // ensure value unchanged
        Assertions.assertEquals(Integer.toString(maxVal), ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(minVal));
        var.actionPerformed(actionEvent);
        // ensure value unchanged
        Assertions.assertEquals(Integer.toString(minVal), ((JTextField) var.getCommonRep()).getText(), "set var text value");
    }

    // test handling of factor and ratio effect
    @Test
    public void testFactorRatio1() {
        int offset1 = 0;
        int factor1 = 1;
        int offset2 = 0;
        int factor2 = 1;
        log.trace("testFactorRatioDecimalValues");
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        VariableValue variable1 = makeVarDec("lower", "comment", "", false,
                false, false, false, "81", "XXXXVVVV", 0, 255,
                v, null, null, offset1, factor1);
        VariableValue variable2 = makeVarDec("higher", "comment", "", false,
                false, false, false, "81", "VVVVXXXX", 0, 255,
                v, null, null, offset2, factor2);
        checkValue(variable1, "var1 initially contains ", "0");
        checkValue(variable2, "var2 initially contains ", "0");
        Assertions.assertEquals(0, cv.getValue(), "cv value");

        // pretend you've edited the upper value & manually notify
        setValue(variable1, "5");
        // see if the CV was updated
        Assertions.assertEquals(5, cv.getValue(), "cv value");
        // check variable values
        checkValue(variable1, "value1 object contains ", "5");
        checkValue(variable2, "value2 object contains ", "0");

        // set CV value
        cv.setValue(226);
        checkValue(variable1, "value1 object contains ", "2");
        checkValue(variable2, "value2 object contains ", "14");

        setValue(variable2, "14");
        // check variable values
        checkValue(variable1, "value1 object contains ", "2");
        checkValue(variable2, "value2 object contains ", "14");
        // see if the CV was updated
        Assertions.assertEquals(226, cv.getValue(), "cv value");
    }

    @Test
    public void testFactorRatio2() {
        int offset1 = 1;
        int factor1 = 1;
        int offset2 = 1;
        int factor2 = 2;
        log.trace("testFactorRatioDecimalValues");
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        VariableValue variable1 = makeVarDec("lower", "comment", "", false,
                false, false, false, "81", "XXXXVVVV", 0, 255,
                v, null, null, offset1, factor1);
        VariableValue variable2 = makeVarDec("higher", "comment", "", false,
                false, false, false, "81", "VVVVXXXX", 0, 255,
                v, null, null, offset2, factor2);
        checkValue(variable1, "var1 initially contains ", "0");
        checkValue(variable2, "var2 initially contains ", "0");
        Assertions.assertEquals(0, cv.getValue(), "cv value");

        // pretend you've edited the upper value & manually notify
        setValue(variable1, "6");
        // see if the CV was updated
        Assertions.assertEquals(5, cv.getValue(), "cv value");
        // check variable values
        checkValue(variable1, "value1 object contains ", "6");
        checkValue(variable2, "value2 object contains ", "1");

        // set CV value
        cv.setValue(226);
        checkValue(variable1, "value1 object contains ", "3");
        checkValue(variable2, "value2 object contains ", "29");

        setValue(variable2, "17");
        // check variable values
        checkValue(variable1, "value1 object contains ", "3");
        checkValue(variable2, "value2 object contains ", "17");
        // see if the CV was updated
        Assertions.assertEquals(130, cv.getValue(), "cv value");
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecVariableValueTest.class);
}
