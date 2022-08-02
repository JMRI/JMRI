package jmri.jmrit.symbolicprog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Test CompEnumVariableValue
 *
 * @author Egbert Broerse Copyright 2022
 * Based on EnumVariableValueTest
 */
public class CompEnumVariableValueTest extends AbstractVariableValueTestBase {

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        CompEnumVariableValue v1 = new CompEnumVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item);
        v1.nItems(10);
        v1.addItem("0");
        v1.addItem("1");
        v1.addItem("2");
        v1.addItem("3");
        v1.addItem("4");
        v1.addItem("5");
        v1.addItem("6");
        v1.addItem("7");
        v1.addItem("9", 9);
        // values needed for specific tests
        v1.addItem("40000", 40000);

        v1.lastItem();

        return v1;
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JComboBox<?>) var.getCommonRep()).setSelectedItem(val);
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((CompEnumVariableValue) var).setValue(Integer.parseInt(val));
    }

    @Override
    void checkValue(VariableValue var, String comment, String val) {
        // we treat one test case (from the parent) specially...
        if (val.equals("14")) {
            Assertions.assertEquals("Reserved value " + val, var.getTextValue(), comment);
        } else {
            Assertions.assertEquals(val, var.getTextValue(), comment);
        }
    }

    @Override
    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        checkValue(var, comment, val);
    }

    // check synchronization of value, representations.
    // This replaces a parent member function (test) that had just
    // too many casts in it to work.
    @Override
    @Test
    public void testVariableSynch() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false,
                false, false, "81", "XXXVVVVV", 0, 10, v, null, null);
        setValue(variable, "5");

        // now get value, check
        checkValue(variable, "first value check ", "5");
        Component val1 = variable.getCommonRep();
        // now get rep, check
        JComboBox<?> rep1 = (JComboBox<?>) variable.getNewRep("");
        Assertions.assertEquals("5", rep1.getSelectedItem(), "initial rep ");

        // update via value
        setValue(variable, "2");

        // check again with existing reference
        Assertions.assertEquals(val1, variable.getCommonRep(), "same value object ");
        Assertions.assertEquals("2", rep1.getSelectedItem(), "1 saved rep ");
        // pick up new references and check
        checkValue(variable, "1 new value ", "2");
        Assertions.assertEquals("2", ((JComboBox<?>) variable.getNewRep("")).getSelectedItem(), "1 new rep ");

        // update via rep
        rep1.setSelectedItem("9");

        // check again with existing references
        Assertions.assertEquals("9", ((JComboBox<?>) val1).getSelectedItem(), "2 saved value ");
        Assertions.assertEquals("9", rep1.getSelectedItem(), "2 saved rep ");
        // pick up new references and check
        checkValue(variable, "2 new value ", "9");
        Assertions.assertEquals("9", ((JComboBox<?>) variable.getNewRep("")).getSelectedItem(), "2 new rep ");
    }

    // end of abstract members for common testing - start of custom tests
    @Test
    public void testSetValue() {
        log.debug("testSetValue");
        CompEnumVariableValue val = createOutOfSequence();
        for (int i = 0; i < 13; i++) {
            val.setValue(i);
            Assertions.assertEquals(i, val.getIntValue(), "check set to " + i);
        }
    }

    @Test
    public void testSetIntValue() {
        CompEnumVariableValue val = createOutOfSequence();
        for (int i = 0; i < 13; i++) {
            val.setIntValue(i);
            Assertions.assertEquals(i, val.getIntValue(), "check set to " + i);
        }
    }

    @Test
    public void testGetTextValue() {
        CompEnumVariableValue val = createOutOfSequence();
        val.setIntValue(0);
        Assertions.assertEquals("name0", val.getTextValue(), "zero");
        val.setIntValue(7);
        Assertions.assertEquals("name7", val.getTextValue(), "seven");
        val.setIntValue(12);
        Assertions.assertEquals("name12", val.getTextValue(), "twelve");
        val.setIntValue(1);
        Assertions.assertEquals("Reserved value 1", val.getTextValue(), "one");
        val.setIntValue(2);
        Assertions.assertEquals("Reserved value 2", val.getTextValue(), "one");
    }

    @Test
    public void testGetValueString() {
        CompEnumVariableValue val = createOutOfSequence();
        val.setIntValue(0);
        Assertions.assertEquals("0", val.getValueString(), "setIntValue zero");
        val.setIntValue(5);
        Assertions.assertEquals("5", val.getValueString(), "setIntValue five");
        val.setIntValue(7);
        Assertions.assertEquals("7", val.getValueString(), "setIntValue seven");
        val.setIntValue(9);
        Assertions.assertEquals("9", val.getValueString(), "setIntValue nine");
        val.setIntValue(12);
        Assertions.assertEquals("12", val.getValueString(), "setIntValue twelve");
        val.setIntValue(1);
        Assertions.assertEquals("1", val.getValueString(), "setIntValue one");
        val.setIntValue(2);
        Assertions.assertEquals("2", val.getValueString(), "setIntValue two");
    }

    public CompEnumVariableValue createOutOfSequence() {
        // prepare
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);

        // text
        CompEnumVariableValue v1 = new CompEnumVariableValue("label check", null, "", false,
                false, false, false, "81", "XXXXXXVV", 0, 12, v,
                null, null);
        v1.nItems(5);
        v1.addItem("name0");
        v1.addItem("5", 5);
        v1.addItem("name7", 7);
        v1.addItem("9", 9);
        v1.addItem("name12", 12);
        v1.lastItem();

        return v1;
    }

    @Test
    public void testRadixMask() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        CompEnumVariableValue variable = new CompEnumVariableValue("label", "comment", "",
                false, false, false, false, "81", "9", 0, 3,
                v, null, null);
        variable.nItems(3);
        variable.addItem("A");
        variable.addItem("B");
        variable.addItem("C");
        variable.lastItem();

        checkValue(variable, "value object initially contains ", "A");
        Assertions.assertEquals(0, cv.getValue(), "cv value"); // mask ignored, default mask applied
        // expect error messages only after reading through variable
        setValue(variable, "B");
        jmri.util.JUnitAppender.assertErrorMessage("Can't handle Radix mask on CompEnumVariableValue");
    }

    @Test
    public void testBaseMasks3() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(0);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        CompEnumVariableValue variable = new CompEnumVariableValue("label", "comment", "",
                false, false, false, false, "81", "XXXXXVVX", 0, 99,
                v, null, null);
        variable.nItems(3);
        variable.addItem("A", 9);
        variable.addItem("B");
        variable.addItem("C");
        variable.lastItem();
        
        checkValue(variable, "value object initially contains ", "A");
        Assertions.assertEquals(0, cv.getValue(), "cv value");

        setValue(variable, "B");
        checkValue(variable, "value object contains ", "B");
        Assertions.assertEquals(100, cv.getValue(), "cv value");

        setValue(variable, "A");
        checkValue(variable, "value object contains ", "A");
        Assertions.assertEquals(90, cv.getValue(), "cv value");

        // pretend you've edited the value & manually notify
        setValue(variable, "C");   // 3rd choice, value = 2
        // check variable value
        checkValue(variable, "value object contains ", "C");
        // see if the CV was updated
        Assertions.assertEquals(110, cv.getValue(), "cv value");
        
        // now check that other parts are maintained
        cv.setValue(12100);
        // check variable value
        checkValue(variable, "value object contains ", "B");
        // see if the CV was updated
        Assertions.assertEquals(12100, cv.getValue(), "cv value");

        // and try setting another value
        setValue(variable, "A");
        Assertions.assertEquals(12090, cv.getValue(), "cv value");
        checkValue(variable, "value object contains ", "A");
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

    private final static Logger log = LoggerFactory.getLogger(CompEnumVariableValueTest.class);

}
