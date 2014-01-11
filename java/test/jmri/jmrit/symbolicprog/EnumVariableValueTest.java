// EnumVariableValueTest.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import java.awt.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.util.*;

/**
 * Test EnumVariableValue
 *
 * @author		Bob Jacobsen  Copyright 2003
 * @version             $Revision$
 */

public class EnumVariableValueTest extends VariableValueTest {

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, String cvName,
                          boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                          String cvNum, String mask, int minVal, int maxVal,
                          HashMap<String, CvValue> v, JLabel status, String item) {
        EnumVariableValue v1 = new EnumVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                                     cvNum, mask, minVal, maxVal,
                                                     v, status, item);
        v1.nItems(9);
        v1.addItem("0");
        v1.addItem("1");
        v1.addItem("2");
        v1.addItem("3");
        v1.addItem("4");
        v1.addItem("5");
        v1.addItem("6");
        v1.addItem("7");
        v1.addItem("9", 9);
        v1.lastItem();

        return v1;
    }


    void setValue(VariableValue var, String val) {
        ((JComboBox)var.getCommonRep()).setSelectedItem(val);
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((EnumVariableValue)var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        // we treat one test case (from the parent) specially...
        if (val.equals("14"))
            Assert.assertEquals(comment, "Reserved value "+val, var.getTextValue());
        else
            Assert.assertEquals(comment, val, var.getTextValue());
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        checkValue(var, comment, val);
    }

    // check synchonization of value, representations.
    // This replaces a parent member function (test) that had just
    // too many casts in it to work.
    public void testVariableSynch() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        // now get value, check
        checkValue(variable, "first value check ", "5");
        Component val1 = variable.getCommonRep();
        // now get rep, check
        JComboBox rep1 = (JComboBox) variable.getNewRep("");
        Assert.assertEquals("initial rep ", "5", (String)rep1.getSelectedItem());

        // update via value
        setValue(variable, "2");

        // check again with existing reference
        Assert.assertEquals("same value object ", val1, variable.getCommonRep());
        Assert.assertEquals("1 saved rep ", "2", (String)rep1.getSelectedItem());
        // pick up new references and check
        checkValue(variable, "1 new value ", "2");
        Assert.assertEquals("1 new rep ", "2", (String)((JComboBox)variable.getNewRep("")).getSelectedItem());

        // update via rep
        rep1.setSelectedItem("9");

        // check again with existing references
        Assert.assertEquals("2 saved value ", "9", ((JComboBox)val1).getSelectedItem());
        Assert.assertEquals("2 saved rep ", "9", (String)rep1.getSelectedItem());
        // pick up new references and check
        checkValue(variable, "2 new value ", "9");
        Assert.assertEquals("2 new rep ", "9", ((JComboBox)variable.getNewRep("")).getSelectedItem());
    }


    // end of abstract members for common testing - start of custom tests

    public void testSetValue() {
        log.debug("testSetValue");
        EnumVariableValue val = createOutOfSequence();
        for (int i=0; i< 21; i++) {
            val.setValue(i);
            Assert.assertEquals("check set to", i, val.getIntValue());
        }
    }

    public void testSetIntValue() {
        EnumVariableValue val = createOutOfSequence();
        for (int i=0; i< 21; i++) {
            val.setIntValue(i);
            Assert.assertEquals("check set to", i, val.getIntValue());
        }
    }

    public void testGetTextValue() {
        EnumVariableValue val = createOutOfSequence();
        val.setIntValue(0);
        Assert.assertEquals("zero", "name0", val.getTextValue());
        val.setIntValue(7);
        Assert.assertEquals("seven", "name7", val.getTextValue());
        val.setIntValue(12);
        Assert.assertEquals("twelve", "name12", val.getTextValue());
        val.setIntValue(1);
        Assert.assertEquals("one", "Reserved value 1", val.getTextValue());
        val.setIntValue(2);
        Assert.assertEquals("one", "Reserved value 2", val.getTextValue());
    }

    public EnumVariableValue createOutOfSequence() {
        // prepare
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);

        // text
        EnumVariableValue v1 = new EnumVariableValue("label check", null, "", false, false, false, false,
                                                         "81", "XXXXXXXX", 0, 2, v, null, null);
        v1.nItems(5);
        v1.addItem("name0");
        v1.addItem("5", 5);
        v1.addItem("name7", 7);
        v1.addItem("9", 9);
        v1.addItem("name12", 12);
        v1.lastItem();

        return v1;
    }

    // from here down is testing infrastructure

    public  EnumVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = { "-noloading", EnumVariableValueTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( EnumVariableValueTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger( EnumVariableValueTest.class.getName());

}
