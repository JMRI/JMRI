package jmri.jmrit.symbolicprog;

import jmri.progdebugger.ProgDebugger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.HashMap;

/**
 * @author Egbert Broerse Copyright 2022
 * Based on DecVariableValueTest
 */
public class CompDecVariableValueTest extends AbstractVariableValueTestBase {

    // Local tests version of makeVar with settable parameters and cvList support.
    private CompDecVariableValue makeVarDec(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item, int offset, int factor) {
        ProgDebugger pp = new ProgDebugger();

        CvValue cvNext = new CvValue(cvNum, pp);
        cvNext.setValue(0);
        v.put(cvName, cvNext);
        return new CompDecVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item, offset, factor);
    }

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        return new CompDecVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item, 0, 1);
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

    // test the handling of XXVVVXXX masks
    @Test
    public void testBaseMasks3() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create a variable pointed at CV 81
        //      Mask = 9, minVal = 0, maxVal = 2
        VariableValue variable = makeVar("label", "comment", "", false,
                false, false, false, "81", "XXXVVVVX", 0, 1200,
                v, null, null);
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");
        // check variable value
        checkValue(variable, "1 value object contains ", "2");
        // see if the CV was updated
        Assertions.assertEquals(20, cv.getValue(), "cv value 1");

        // now check that other parts are maintained
        cv.setValue(1200);
        // check variable value
        checkValue(variable, "2 value object contains ", "1200");
        // see if the CV was updated
        Assertions.assertEquals(31200, cv.getValue(), "cv value 2");

        // and try setting another value
        setValue(variable, "1");
        checkValue(variable, "3 value object contains ", "1200");
        Assertions.assertEquals(43210, cv.getValue(), "cv value 3");
    }

    // done
    @Test
    public void testBaseMasksDecimalValues() {
        log.trace("testBaseMasksDecimalValues");
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create variables pointed at CV 81
        VariableValue variableU = makeVar("upper", "comment", "", false, false, false, false, "81", "10", 0, 9, v, null, null);
        checkValue(variableU, "upper initially contains ", "0");
        Assertions.assertEquals(0, cv.getValue(), "cv value");
        // pretend you've edited the upper value & manually notify
        variableU.getValueInCV(81, "10", 9);
        // expect error messages
        jmri.util.JUnitAppender.assertErrorMessage("Can't handle Radix mask");

        variableU.setValueInCV(0, 2, "10", 9);
        // expect error messages
        jmri.util.JUnitAppender.assertErrorMessage("Can't handle Radix mask on CompDecVariableValue");
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
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        int offset = 3;
        int factor = 50;
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
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        int offset = 3;
        int factor = 50;
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompDecVariableValueTest.class);
}
