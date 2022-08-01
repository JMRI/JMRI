package jmri.jmrit.symbolicprog;

import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
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
    private CompDecVariableValue makeCompDecVar(String label, String comment, String cvName,
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

    // can we create one, then manipulate the variable to change the CV?
    @Override
    @Test
    public void testVariableValueCreate() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "5");

        // check value
        checkValue(variable, "value object contains ", "5");

        // see if the CV was updated
        Assertions.assertEquals(5 * Math.pow(10, 2) + 3, cv.getValue(), "cv value"); // value 3 stays, 5 added as 500 (mask digit 2)
    }

    //  check create&manipulate for large mask values
    @Override
    @Test
    public void testVariableValueCreateLargeMaskValue() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(32768 + 3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXVVVVVXXXXXXXX", 0, 60000, v, null, null);
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");

        // check value
        checkValue(variable, "value object contains ", "2");

        // see if the CV was updated
        Assertions.assertEquals(2 * Math.pow(10, 8) + 32768 + 3, cv.getValue(), "cv value");
    }

    @Override
    @Test
    public void testVariableValueCreateLargeMaskValue256() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(32768 + 3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXXXXXVXXXXXXXX", 0, 60000, v, null, null);
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "1");

        // check value
        checkValue(variable, "value object contains ", "1");

        // see if the CV was updated
        Assertions.assertEquals(1 * Math.pow(10, 8) + 32768 + 3, cv.getValue(), "cv value");
    }

    @Override
    @Test
    public void testVariableValueCreateLargeMaskValue2up16() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "VXXXXXXX", 0, 10, v, null, null);
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "1");

        // check value
        checkValue(variable, "value object contains ", "1");

        // see if the CV was updated
        Assertions.assertEquals(1 * Math.pow(10, 7) + 3, cv.getValue(), "cv value");
    }

    // can we change the CV and see the result in the Variable?
    @Override
    @Test
    public void testVariableFromCV() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVX", 0, 255, v, null, null);
        Assertions.assertNotNull(variable.getCommonRep(), "getValue not null ");
        setValue(variable, "5");
        checkValue(variable, "variable value", "5");

        // change the CV, expect to see a change in the variable value
        cv.setValue(7 * 100 + 6);
        checkValue(variable, "value after CV set", "70");
        Assertions.assertEquals(7 * 100 + 6, cv.getValue(), "cv after CV set ");
    }

    // check a read operation
    @Override
    @Test
    public void testVariableValueRead() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXXXVVX", 0, 255, v, null, null);
        setValue(variable, "5");

        variable.readAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()-> !variable.isBusy(), "variable.isBusy");

        checkValue(variable, "text var value ", "12"); // mask out digit 0
        Assertions.assertEquals(AbstractValue.READ, variable.getState(), "var state ");
        Assertions.assertEquals(123, cv.getValue(), "cv value");
        Assertions.assertEquals(AbstractValue.READ, cv.getState(), "CV state ");
    }

    // check a write operation to the variable
    @Override
    @Test
    public void testVariableValueWrite() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        cv.setValue(171);

        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        variable.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()-> !variable.isBusy(), "variable.isBusy");

        checkValue(variable, "value ", "5");
        Assertions.assertEquals(AbstractValue.STORED, variable.getState(), "var state ");
        Assertions.assertEquals(AbstractValue.STORED, cv.getState(), "cv state ");
        Assertions.assertEquals(571, p.lastWrite(), "last program write "); // include checking original bits
    }

    // check synch during a write operation to the CV
    @Override
    @Test
    public void testVariableCvWrite() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        JLabel statusLabel = new JLabel("nothing");
        cv.write(statusLabel);  // JLabel is for reporting status, ignored here
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()-> !cv.isBusy(), "cv.isBusy");

        checkValue(variable, "value ", "5");
        Assertions.assertEquals(AbstractValue.STORED, variable.getState(), "variable state ");
        Assertions.assertEquals(AbstractValue.STORED, cv.getState(), "cv state ");
        Assertions.assertEquals(500 + 3, p.lastWrite(), "value written "); // includes initial value bits
        Assertions.assertEquals("OK", statusLabel.getText(), "status label ");
    }

    // check synchronization of two vars during a write
    @Test
    public void testWriteSynch2() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue var1 = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        VariableValue var2 = makeVar("alternate", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(var1, "5");

        var1.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !var1.isBusy();}, "var1.isBusy");

        checkValue(var1, "var 1 value", "5");
        checkValue(var2, "var 2 value", "5");
        Assertions.assertEquals(AbstractValue.STORED, var1.getState(), "1st variable state ");
        Assertions.assertEquals(AbstractValue.STORED, var2.getState(), "2nd variable state ");
        Assertions.assertEquals(500 + 3, p.lastWrite(), "value written to programmer "); // includes initial value bits
    }

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
        checkValue(variable, "2 value object contains ", "120");
        // see if the CV was updated
        Assertions.assertEquals(1200, cv.getValue(), "cv value 2");

        // and try setting another value
        setValue(variable, "1");
        checkValue(variable, "3 value object contains ", "1");
        Assertions.assertEquals(10, cv.getValue(), "cv value 10");
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
        String name = "Compound Decimal Field";
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
        int offset = 1;
        int factor = 2;
        CompDecVariableValue var = makeCompDecVar(name, comment, cvName,
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
        Assertions.assertEquals(2, cv[0].getValue(), "set CV" + cv[0].number());

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
        String name = "Compound Decimal Field";
        String comment = "";
        String cvName = "174";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "174";
        String mask = "XXXVVVXX";
        int minVal = 3;
        int maxVal = 27;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        int offset = 0;
        int factor = 3;
        CompDecVariableValue var = makeCompDecVar(name, comment, cvName,
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
        ((JTextField) var.getCommonRep()).setText("9");
        var.actionPerformed(actionEvent);
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(300, cv[0].getValue(), "set CV" + cv[0].number());

        // change text to an invalid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("54");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another invalid value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("Fred");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to an out-of-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal + 1));
        var.actionPerformed(actionEvent);
        // ensure value unchanged
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another out-of-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(minVal - 1));
        var.actionPerformed(actionEvent);
        // ensure value unchanged
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to a just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal));
        var.actionPerformed(actionEvent);
        // ensure value unchanged, NOTE: except for rounding factor 3
        Assertions.assertEquals(Integer.toString(maxVal), ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to a just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(maxVal));
        var.actionPerformed(actionEvent);
        // ensure value unchanged, NOTE: except for rounding factor 3
        Assertions.assertEquals(Integer.toString(maxVal), ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change text to another just in-range value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText(Integer.toString(minVal));
        var.actionPerformed(actionEvent);
        // ensure value unchanged, NOTE: except for rounding factor 3
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
