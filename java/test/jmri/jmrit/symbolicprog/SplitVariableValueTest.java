package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.progdebugger.ProgDebugger;
import jmri.util.CvUtil;
import jmri.util.JUnitUtil;

/**
 * Tests for the {@link SplitVariableValue} class.
 *
 * TODO need a check of the MIXED state model for long address
 * @author Bob Jacobsen Copyright 2001, 2002, 2015
 * @author Dave Heap Copyright 2019
 */
public class SplitVariableValueTest extends AbstractVariableValueTestBase {

    final String lowCV = "12";
    final String highCV = "18";
    ProgDebugger p = new ProgDebugger();

    // Local tests version of makeVar with settable parameters and cvList support.
    SplitVariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item,
            String highCV, int pFactor, int pOffset, String uppermask,
            String extra1, String extra2, String extra3, String extra4) {
        ProgDebugger pp = new ProgDebugger();

        if (!cvNum.equals("")) { // some variables have no CV per se
            List<String> cvList = CvUtil.expandCvList(cvNum);
            if (cvList.isEmpty()) {
                CvValue cvNext = new CvValue(cvNum, pp);
                cvNext.setValue(0);
                v.put(cvName, cvNext);
            } else { // or require expansion
                for (String s : cvList) {
                    CvValue cvNext = new CvValue(s, pp);
                    cvNext.setValue(0);
                    v.put(s, cvNext);
                }
            }
        }
        if (highCV != null && !highCV.equals("")) {
            CvValue cvNext = new CvValue(highCV, pp);
            cvNext.setValue(0);
            v.put(highCV, cvNext);
        }
        return new SplitVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        // make sure next CV exists
        CvValue cvNext = new CvValue(highCV, p);
        cvNext.setValue(0);
        v.put(highCV, cvNext);
        return new SplitVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, "XXXXVVVV", minVal, maxVal, v, status, item,
                highCV, 1, 0, "VVVVVVVV", null, null, null, null);
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((SplitVariableValue) var).setLongValue(Long.parseUnsignedLong(val));
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
    // some of the premade tests don't quite make sense; override them here.
    @Override
    @Test
    public void testVariableValueCreate() {
    }// mask is ignored by splitAddress tests

    @Override
    @Test
    public void testVariableFromCV() {
    }     // low CV is upper part of address

    @Override
    @Test
    public void testVariableValueRead() {
    } // due to multi-cv nature of splitAddress tests

    @Override
    @Test
    public void testVariableValueWrite() {
    } // due to multi-cv nature of splitAddress tests

    @Override
    @Test
    public void testVariableCvWrite() {
    } // due to multi-cv nature of splitAddress tests

    @Override
    @Test
    public void testWriteSynch2() {
    }        // programmer synch is different

    // at some point, these should pass, but have to think hard about
    // how to define the split/shift/mask operations for long CVs
    @Override
    @Test
    public void testVariableValueCreateLargeValue() {
    } // mask is ignored

    @Override
    @Test
    public void testVariableValueCreateLargeMaskValue() {
    } // mask is ignored

    @Override
    @Test
    public void testVariableValueCreateLargeMaskValue256() {
    } // mask is ignored

    @Override
    @Test
    public void testVariableValueCreateLargeMaskValue2up16() {
    } // mask is ignored

    // Local tests
    @Test
    public void testSplitAddressFromCV1() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(highCV, p);
        cv1.setValue(2);
        cv2.setValue(3);
        v.put(lowCV, cv1);
        v.put(highCV, cv2);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                "VVVVVVVV", 0, 255, v, null, null,
                highCV, 1, 0, "VVVVVVVV", null, null, null, null);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "" + (1029), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 5, cv1.getValue());
        Assert.assertEquals("set var high bits", 4, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", "" + (189 * 256 + 21), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    @Test
    public void testSplitAddressFromCV2() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(highCV, p);
        cv1.setValue(0xFF);
        cv2.setValue(0xFF);
        v.put(lowCV, cv1);
        v.put(highCV, cv2);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                "XXXXVVVV", 0, 255, v, null, null,
                highCV, 1, 0, "VVVVVVVV", null, null, null, null);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "" + (1029), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 0xF5, cv1.getValue());
        Assert.assertEquals("set var high bits", 4 * 16, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", "" + (189 * 16 + (21 & 0xF)), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    @Test
    public void testSplitAddressFromCV3() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(highCV, p);
        cv1.setValue(0xFF);
        cv2.setValue(0xFF);
        v.put(lowCV, cv1);
        v.put(highCV, cv2);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                "VVVVVVVV", 0, 255, v, null, null,
                highCV, 1, 0, "XXVVVVXX", null, null, null, null);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "" + (1029), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 5, cv1.getValue());
        Assert.assertEquals("set var high bits", 0xC3 + 4 * 4, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", "" + ((189 & 0x3C) / 4 * 256 + (21)), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    @Test
    public void testSplitAddressFromCV4() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(highCV, p);
        cv1.setValue(0xFF);
        cv2.setValue(0xFF);
        v.put(lowCV, cv1);
        v.put(highCV, cv2);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                "XVVVVVVX", 0, 255, v, null, null,
                highCV, 1, 0, "XVVVVVXX", null, null, null, null);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "" + (1029), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 5 * 2 + 0x81, cv1.getValue());
        Assert.assertEquals("set var high bits", 0x83 + 0x40, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", "" + ((189 & 0x3C) / 4 * 64 + (10)), ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    List<java.beans.PropertyChangeEvent> evtList = null;  // holds a list of ParameterChange events

    // check a long address read operation
    @Test
    public void testSplitAddressRead1() {
        log.debug("testSplitAddressRead starts");

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(highCV, p);
        v.put(lowCV, cv1);
        v.put(highCV, cv2);

        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false,
                lowCV, "XXVVVVVV", 0, 255, v, null, null,
                highCV, 1, 0, "VVVVVVVV", null, null, null, null);
        // register a listener for parameter changes
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                evtList.add(e);
                if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                    log.debug("Busy false seen in test");
                }
            }
        };
        evtList = new ArrayList<>();
        var.addPropertyChangeListener(listen);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        // set to specific value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("5");
        var.focusLost(focusEvent);

        // read should get 123, 123 from CVs
        var.readAll();

        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(() -> {
            return !var.isBusy();
        }, "var.isBusy");

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = evtList.get(k);
            if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                nBusyFalse++;
            }
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("text value ", "" + ((123 & 0x3f) + (123) * 64), ((JTextField) var.getCommonRep()).getText());  // 15227 = (1230x3f)*256+123
        Assert.assertEquals("Var state", AbstractValue.READ, var.getState());
        Assert.assertEquals("CV 1 value ", 123, cv1.getValue());  // 123 with 128 bit set
        Assert.assertEquals("CV 2 value ", 123, cv2.getValue());
    }

    // check a long address write operation
    @Test
    public void testSplitAddressWrite1() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(highCV, p);
        v.put(lowCV, cv1);
        v.put(highCV, cv2);

        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false,
                lowCV, "XXVVVVVV", 0, 255, v, null, null,
                highCV, 1, 0, "VVVVVVVV", null, null, null, null);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("4797");
        var.focusLost(focusEvent);

        var.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(() -> {
            return !var.isBusy();
        }, "var.isBusy");

        Assert.assertEquals("CV 1 value ", 61, cv1.getValue());
        Assert.assertEquals("CV 2 value ", 74, cv2.getValue());
        Assert.assertEquals("text ", "4797", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("Var state", AbstractValue.STORED, var.getState());
        Assert.assertEquals("last write", 74, p.lastWrite());
        // how do you check separation of the two writes?  State model?
    }

    @Test
    public void testTextMaxLongVal() {
        String name = "Decimal Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:8";
        String mask = "VVVVVVVV";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String extra1 = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 8, cv.length);

        Assert.assertEquals("cv[0] is", "275", cv[0].number());
        Assert.assertEquals("cv[1] is", "276", cv[1].number());
        Assert.assertEquals("cv[2] is", "277", cv[2].number());
        Assert.assertEquals("cv[3] is", "278", cv[3].number());
        Assert.assertEquals("cv[4] is", "279", cv[4].number());
        Assert.assertEquals("cv[5] is", "280", cv[5].number());
        Assert.assertEquals("cv[6] is", "281", cv[6].number());
        Assert.assertEquals("cv[7] is", "282", cv[7].number());

        // start with a random value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change to maximum unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("18446744073709551615");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "18446744073709551615", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0xFF, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0xFF, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xFF, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0xFF, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0xFF, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0xFF, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0xFF, cv[7].getValue());

        // change to one less than maximum unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("18446744073709551614");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "18446744073709551614", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0xFE, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0xFF, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xFF, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0xFF, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0xFF, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0xFF, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0xFF, cv[7].getValue());

        // change to last 63 bit unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("9223372036854775807");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "9223372036854775807", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0xFF, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0xFF, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xFF, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0xFF, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0xFF, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0xFF, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x7F, cv[7].getValue());

        // change to first 64 bit unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("9223372036854775808");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "9223372036854775808", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x00, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x00, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0x00, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x00, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x80, cv[7].getValue());

    }

    @Test
    public void testCvChangesMaxLongVal() {
        String name = "Decimal Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:8";
        String mask = "VVVVVVVV";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String extra1 = null;
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 8, cv.length);

        Assert.assertEquals("cv[0] is", "275", cv[0].number());
        Assert.assertEquals("cv[1] is", "276", cv[1].number());
        Assert.assertEquals("cv[2] is", "277", cv[2].number());
        Assert.assertEquals("cv[3] is", "278", cv[3].number());
        Assert.assertEquals("cv[4] is", "279", cv[4].number());
        Assert.assertEquals("cv[5] is", "280", cv[5].number());
        Assert.assertEquals("cv[6] is", "281", cv[6].number());
        Assert.assertEquals("cv[7] is", "282", cv[7].number());

        // start with a random value
        cv[0].setValue(0x0F);
        cv[1].setValue(0x72);
        cv[2].setValue(0xD2);
        cv[3].setValue(0x7F);
        cv[4].setValue(0x00);
        cv[5].setValue(0x00);
        cv[6].setValue(0x00);
        cv[7].setValue(0x00);
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());

        // change to maximum unsigned value
        cv[0].setValue(0xFF);
        cv[1].setValue(0xFF);
        cv[2].setValue(0xFF);
        cv[3].setValue(0xFF);
        cv[4].setValue(0xFF);
        cv[5].setValue(0xFF);
        cv[6].setValue(0xFF);
        cv[7].setValue(0xFF);
        Assert.assertEquals("set CV" + cv[0].number(), 0xFF, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0xFF, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xFF, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0xFF, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0xFF, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0xFF, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0xFF, cv[7].getValue());
        Assert.assertEquals("set var text value", "18446744073709551615", ((JTextField) var.getCommonRep()).getText());

        // change to one less than maximum unsigned value
        cv[0].setValue(0xFE);
        cv[1].setValue(0xFF);
        cv[2].setValue(0xFF);
        cv[3].setValue(0xFF);
        cv[4].setValue(0xFF);
        cv[5].setValue(0xFF);
        cv[6].setValue(0xFF);
        cv[7].setValue(0xFF);
        Assert.assertEquals("set CV" + cv[0].number(), 0xFE, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0xFF, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xFF, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0xFF, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0xFF, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0xFF, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0xFF, cv[7].getValue());
        Assert.assertEquals("set var text value", "18446744073709551614", ((JTextField) var.getCommonRep()).getText());

        // change to last 63 bit unsigned value
        cv[0].setValue(0xFF);
        cv[1].setValue(0xFF);
        cv[2].setValue(0xFF);
        cv[3].setValue(0xFF);
        cv[4].setValue(0xFF);
        cv[5].setValue(0xFF);
        cv[6].setValue(0xFF);
        cv[7].setValue(0x7F);
        Assert.assertEquals("set CV" + cv[0].number(), 0xFF, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0xFF, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xFF, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0xFF, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0xFF, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0xFF, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x7F, cv[7].getValue());
        Assert.assertEquals("set var text value", "9223372036854775807", ((JTextField) var.getCommonRep()).getText());

        // change to first 64 bit unsigned value
        cv[0].setValue(0x00);
        cv[1].setValue(0x00);
        cv[2].setValue(0x00);
        cv[3].setValue(0x00);
        cv[4].setValue(0x00);
        cv[5].setValue(0x00);
        cv[6].setValue(0x00);
        cv[7].setValue(0x80);
        Assert.assertEquals("set CV" + cv[0].number(), 0x00, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x00, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0x00, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x00, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x80, cv[7].getValue());
        Assert.assertEquals("set var text value", "9223372036854775808", ((JTextField) var.getCommonRep()).getText());

    }

    @Test
    public void testCvChangesMaskedBits0() {
        String name = "Servo16";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String lowCVnum = "11";
        String mask = "XXXVXXVX";
        int minVal = 0;
        int maxVal = 0;
        JLabel status = new JLabel();
        String stdname = "";
        String highCVnum = "12";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "XXXXVXXX";
        String extra1 = null;
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCVnum, p);
        CvValue cv2 = new CvValue(highCVnum, p);
        cv1.setValue(0);
        cv2.setValue(0);
        v.put(lowCVnum, cv1);
        v.put(highCVnum, cv2);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                lowCVnum, mask, minVal, maxVal,
                v, status, stdname,
                highCVnum, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 2, cv.length);

        Assert.assertEquals("cv[0] is", lowCVnum, cv[0].number());
        Assert.assertEquals("cv[1] is", highCVnum, cv[1].number());

        // Start with all zero values
        var.setLongValue(0x00);
        cv[0].setValue(0x00);
        cv[1].setValue(0x00);
        Assert.assertEquals("get CV" + cv[0].number(), 0x00, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 0x00, cv[1].getValue());
        Assert.assertEquals("get Value", 0x00, var.getLongValue());
        Assert.assertEquals("get text value", "0", ((JTextField) var.getCommonRep()).getText());

        // Following samples provided by Robin Becker
        cv[0].setValue(255);
        cv[1].setValue(0);
        Assert.assertEquals("get CV" + cv[0].number(), 255, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 0, cv[1].getValue());
        Assert.assertEquals("get Value", 9, var.getLongValue());
        Assert.assertEquals("get text value", "9", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(0);
        cv[1].setValue(255);
        Assert.assertEquals("get CV" + cv[0].number(), 0, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 255, cv[1].getValue());
        Assert.assertEquals("get Value", 16, var.getLongValue());
        Assert.assertEquals("get text value", "16", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(2);
        cv[1].setValue(255);
        Assert.assertEquals("get CV" + cv[0].number(), 2, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 255, cv[1].getValue());
        Assert.assertEquals("get Value", 17, var.getLongValue());
        Assert.assertEquals("get text value", "17", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(255);
        cv[1].setValue(2);
        Assert.assertEquals("get CV" + cv[0].number(), 255, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 2, cv[1].getValue());
        Assert.assertEquals("get Value", 9, var.getLongValue());
        Assert.assertEquals("get text value", "9", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(255);
        cv[1].setValue(255);
        Assert.assertEquals("get CV" + cv[0].number(), 255, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 255, cv[1].getValue());
        Assert.assertEquals("get Value", 25, var.getLongValue());
        Assert.assertEquals("get text value", "25", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(1);
        cv[1].setValue(240);
        Assert.assertEquals("get CV" + cv[0].number(), 1, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240, cv[1].getValue());
        Assert.assertEquals("get Value", 0, var.getLongValue());
        Assert.assertEquals("get text value", "0", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(4);
        cv[1].setValue(240);
        Assert.assertEquals("get CV" + cv[0].number(), 4, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240, cv[1].getValue());
        Assert.assertEquals("get Value", 0, var.getLongValue());
        Assert.assertEquals("get text value", "0", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(7);
        cv[1].setValue(240);
        Assert.assertEquals("get CV" + cv[0].number(), 7, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240, cv[1].getValue());
        Assert.assertEquals("get Value", 1, var.getLongValue());
        Assert.assertEquals("get text value", "1", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(2);
        cv[1].setValue(240);
        Assert.assertEquals("get CV" + cv[0].number(), 2, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240, cv[1].getValue());
        Assert.assertEquals("get Value", 1, var.getLongValue());
        Assert.assertEquals("get text value", "1", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(0);
        cv[1].setValue(15);
        Assert.assertEquals("get CV" + cv[0].number(), 0, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 15, cv[1].getValue());
        Assert.assertEquals("get Value", 16, var.getLongValue());
        Assert.assertEquals("get text value", "16", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(15);
        cv[1].setValue(15);
        Assert.assertEquals("get CV" + cv[0].number(), 15, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 15, cv[1].getValue());
        Assert.assertEquals("get Value", 17, var.getLongValue());
        Assert.assertEquals("get text value", "17", ((JTextField) var.getCommonRep()).getText());

    }

    @Test
    public void testCvChangesMaskedBits1() {
        String name = "Servo16";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String lowCVnum = "11";
        String mask = "XXXVXXVX";
        int minVal = 0;
        int maxVal = 0;
        JLabel status = new JLabel();
        String stdname = "";
        String highCVnum = "12";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "XXXXVXXX";
        String extra1 = null;
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv1 = new CvValue(lowCVnum, p);
        CvValue cv2 = new CvValue(highCVnum, p);
        cv1.setValue(0);
        cv2.setValue(0);
        v.put(lowCVnum, cv1);
        v.put(highCVnum, cv2);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                lowCVnum, mask, minVal, maxVal,
                v, status, stdname,
                highCVnum, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 2, cv.length);

        Assert.assertEquals("cv[0] is", lowCVnum, cv[0].number());
        Assert.assertEquals("cv[1] is", highCVnum, cv[1].number());

        // We will set all ignored bits in the CVs to check that they don't affect the result.
        int lowIgnored = ~var.maskValAsInt(mask) & 0xFF;
        int highIgnored = ~var.maskValAsInt(uppermask) & 0xFF;

        // Start with all zero values
        var.setLongValue(0x00);
        cv[0].setValue(0x00 | lowIgnored);
        cv[1].setValue(0x00 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 0x00 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 0x00 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 0x00, var.getLongValue());
        Assert.assertEquals("get text value", "0", ((JTextField) var.getCommonRep()).getText());

        // Following samples provided by Robin Becker
        cv[0].setValue(255 | lowIgnored);
        cv[1].setValue(0 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 255 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 0 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 9, var.getLongValue());
        Assert.assertEquals("get text value", "9", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(0 | lowIgnored);
        cv[1].setValue(255 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 0 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 255 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 16, var.getLongValue());
        Assert.assertEquals("get text value", "16", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(2 | lowIgnored);
        cv[1].setValue(255 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 2 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 255 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 17, var.getLongValue());
        Assert.assertEquals("get text value", "17", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(255 | lowIgnored);
        cv[1].setValue(2 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 255 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 2 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 9, var.getLongValue());
        Assert.assertEquals("get text value", "9", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(255 | lowIgnored);
        cv[1].setValue(255 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 255 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 255 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 25, var.getLongValue());
        Assert.assertEquals("get text value", "25", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(1 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 1 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 0, var.getLongValue());
        Assert.assertEquals("get text value", "0", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(4 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 4 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 0, var.getLongValue());
        Assert.assertEquals("get text value", "0", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(7 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 7 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 1, var.getLongValue());
        Assert.assertEquals("get text value", "1", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(2 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 2 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 240 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 1, var.getLongValue());
        Assert.assertEquals("get text value", "1", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(0 | lowIgnored);
        cv[1].setValue(15 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 0 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 15 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 16, var.getLongValue());
        Assert.assertEquals("get text value", "16", ((JTextField) var.getCommonRep()).getText());

        cv[0].setValue(15 | lowIgnored);
        cv[1].setValue(15 | highIgnored);
        Assert.assertEquals("get CV" + cv[0].number(), 15 | lowIgnored, cv[0].getValue());
        Assert.assertEquals("get CV" + cv[1].number(), 15 | highIgnored, cv[1].getValue());
        Assert.assertEquals("get Value", 17, var.getLongValue());
        Assert.assertEquals("get text value", "17", ((JTextField) var.getCommonRep()).getText());

    }

    @Test
    public void testTextInvalidLongValEnteredFocusLost() {
        String name = "Decimal Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:8";
        String mask = "VVVVVVVV";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String extra1 = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 8, cv.length);

        Assert.assertEquals("cv[0] is", "275", cv[0].number());
        Assert.assertEquals("cv[1] is", "276", cv[1].number());
        Assert.assertEquals("cv[2] is", "277", cv[2].number());
        Assert.assertEquals("cv[3] is", "278", cv[3].number());
        Assert.assertEquals("cv[4] is", "279", cv[4].number());
        Assert.assertEquals("cv[5] is", "280", cv[5].number());
        Assert.assertEquals("cv[6] is", "281", cv[6].number());
        Assert.assertEquals("cv[7] is", "282", cv[7].number());

        // start with a random value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change text to an invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("184467F4079551615");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change text to another invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("3G");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

    }

    @Test
    public void testTextInvalidLongValEnteredActionPerformed() {
        String name = "Decimal Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:8";
        String mask = "VVVVVVVV";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String extra1 = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        ActionEvent actionEvent = new ActionEvent(var.getCommonRep(), ActionEvent.ACTION_PERFORMED, name);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 8, cv.length);

        Assert.assertEquals("cv[0] is", "275", cv[0].number());
        Assert.assertEquals("cv[1] is", "276", cv[1].number());
        Assert.assertEquals("cv[2] is", "277", cv[2].number());
        Assert.assertEquals("cv[3] is", "278", cv[3].number());
        Assert.assertEquals("cv[4] is", "279", cv[4].number());
        Assert.assertEquals("cv[5] is", "280", cv[5].number());
        Assert.assertEquals("cv[6] is", "281", cv[6].number());
        Assert.assertEquals("cv[7] is", "282", cv[7].number());

        // start with a random value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.actionPerformed(actionEvent);
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change text to an invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("184467F4079551615");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change text to another invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("3G");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assert.assertEquals("set var text value", "2144498191", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

    }

    @Test
    @Override
    public void testVariableValueTwinMask() {
        String name = "Masked Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "2,4,8";
        String mask = "VXXXXXXX XXXXVVVV"; // last mask also applied to CV8
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String extra1 = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, stdname,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);

        Assert.assertEquals("mask at start", "VXXXXXXX XXXXVVVV", var.getMask());
        Assert.assertEquals("mask 2", "XXXXVVVV", var.getMask(1));
        Assert.assertEquals("mask 3", "XXXXVVVV", var.getMask(2));

        var.simplifyMask(); // no effect on mask returned
        Assert.assertEquals("mask after simplify", "VXXXXXXX XXXXVVVV", var.getMask());
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

    private final static Logger log = LoggerFactory.getLogger(SplitVariableValueTest.class);

}
