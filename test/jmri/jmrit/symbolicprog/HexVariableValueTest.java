// HexVariableValueTest.java

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.*;
import jmri.progdebugger.*;

/**
 * Test the HexVariableValue class
 *
 * @author	Bob Jacobsen  Copyright 2001
 * @version     $Revision: 1.4 $
 */
public class HexVariableValueTest extends VariableValueTest {

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, String cvName,
                          boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                          int cvNum, String mask, int minVal, int maxVal,
                          Vector v, JLabel status, String item) {
        return new HexVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }


    void setValue(VariableValue var, String val) {
        ((JTextField)var.getValue()).setText(val);
        ((JTextField)var.getValue()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((HexVariableValue)var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        String hexval = Integer.toHexString(Integer.valueOf(val).intValue());
        Assert.assertEquals(comment, hexval, ((JTextField)var.getValue()).getText() );
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        String hexval = Integer.toHexString(Integer.valueOf(val).intValue());
        Assert.assertEquals(comment, hexval, ((JLabel)var.getValue()).getText() );
    }

    // end of abstract members


    // from here down is testing infrastructure

    public  HexVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = { HexVariableValueTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( HexVariableValueTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( HexVariableValueTest.class.getName());

}
