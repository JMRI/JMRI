/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.CvUtil;

/**
 *
 * @author heap
 */
public class SplitTextVariableValueTest {

    final String lowCV = "12";
    final String highCV = "18";
    ProgDebugger p = new ProgDebugger();
//    HashMap<String, CvValue> v;

    public SplitTextVariableValueTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    VariableValue makeVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        if (!cvNum.equals("")) { // some variables have no CV per se
            List<String> cvList = CvUtil.expandCVlist(cvNum);
            if (cvList.isEmpty()) {
                CvValue cvNext = new CvValue(cvNum, p);
                cvNext.setValue(0);
                v.put(cvName, cvNext);
            } else { // or require expansion
                for (String s : cvList) {
                    CvValue cvNext = new CvValue(s, p);
                    cvNext.setValue(0);
                    v.put(s, cvNext);
                }
            }
        }
        if (highCV != null) {
            CvValue cvNext = new CvValue(highCV, p);
            cvNext.setValue(0);
            v.put(highCV, cvNext);
        }
        return new SplitTextVariableValue(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    /**
     * Test of stepOneActions method, of class SplitTextVariableValue.
     */
    @Test
    public void testStepOneActions() {
        System.out.println("stepOneActions");
        String name = "";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "";
        String mask = "";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = null;
        JLabel status = null;
        String stdname = "";
        String pSecondCV = "";
        int pFactor = 0;
        int pOffset = 0;
        String uppermask = "";
        String extra1 = "";
        String extra2 = "";
        String extra3 = "";
        String extra4 = "";
        SplitTextVariableValue instance = null;
        instance.stepOneActions(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stepTwoActions method, of class SplitTextVariableValue.
     */
    @Test
    public void testStepTwoActions() {
        System.out.println("stepTwoActions");
        SplitTextVariableValue instance = null;
        instance.stepTwoActions();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isMatched method, of class SplitTextVariableValue.
     */
    @Test
    public void testIsMatched() {
        System.out.println("isMatched");
        String s = "";
        SplitTextVariableValue instance = null;
        boolean expResult = false;
        boolean result = instance.isMatched(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBytesFromText method, of class SplitTextVariableValue.
     */
    @Test
    public void testGetBytesFromText() {
        System.out.println("getBytesFromText");
        String s = "";
        SplitTextVariableValue instance = null;
        byte[] expResult = null;
        byte[] result = instance.getBytesFromText(s);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTextFromBytes method, of class SplitTextVariableValue.
     */
    @Test
    public void testGetTextFromBytes() {
        System.out.println("getTextFromBytes");
        byte[] v = null;
        SplitTextVariableValue instance = null;
        String expResult = "";
        String result = instance.getTextFromBytes(v);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of unsupportedCharset method, of class SplitTextVariableValue.
     */
    @Test
    public void testUnsupportedCharset() {
        System.out.println("unsupportedCharset");
        SplitTextVariableValue instance = null;
        instance.unsupportedCharset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCvValsFromTextField method, of class SplitTextVariableValue.
     */
    @Test
    public void testGetCvValsFromTextField() {
        System.out.println("getCvValsFromTextField");
        SplitTextVariableValue instance = null;
        int[] expResult = null;
        int[] result = instance.getCvValsFromTextField();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateVariableValue method, of class SplitTextVariableValue.
     */
    @Test
    public void testUpdateVariableValue() {
        System.out.println("updateVariableValue");
        int[] intVals = null;
        SplitTextVariableValue instance = null;
        instance.updateVariableValue(intVals);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exitField method, of class SplitTextVariableValue.
     */
    @Test
    public void testExitField() {
        System.out.println("exitField");
        SplitTextVariableValue instance = null;
        instance.exitField();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionPerformed method, of class SplitTextVariableValue.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");
        ActionEvent e = null;
        SplitTextVariableValue instance = null;
        instance.actionPerformed(e);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIntValue method, of class SplitTextVariableValue.
     */
    @Test
    public void testGetIntValue() {
        System.out.println("getIntValue");
        SplitTextVariableValue instance = null;
        int expResult = 0;
        int result = instance.getIntValue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class SplitTextVariableValue.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String value = "";
        SplitTextVariableValue instance = null;
        instance.setValue(value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
