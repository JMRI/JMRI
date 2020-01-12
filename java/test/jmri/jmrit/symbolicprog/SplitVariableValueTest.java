package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.progdebugger.ProgDebugger;
import jmri.util.CvUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link SplitVariableValue} class.
 *
 * @todo need a check of the MIXED state model for long address
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
    }// mask is ignored by splitAddre

    @Override
    @Test
    public void testVariableFromCV() {
    }     // low CV is upper part of address

    @Override
    @Test
    public void testVariableValueRead() {
    }	// due to multi-cv nature of SplitAddr

    @Override
    @Test
    public void testVariableValueWrite() {
    } // due to multi-cv nature of SplitAddr

    @Override
    @Test
    public void testVariableCvWrite() {
    }    // due to multi-cv nature of SplitAddr

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

        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
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

        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
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

        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
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

        ((JTextField) var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
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

        // set to specific value
        ((JTextField) var.getCommonRep()).setText("5");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

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
        ((JTextField) var.getCommonRep()).setText("4797");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

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
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
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
        ((JTextField) var.getCommonRep()).setText("18446744073709551615");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
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
        ((JTextField) var.getCommonRep()).setText("18446744073709551614");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
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
        ((JTextField) var.getCommonRep()).setText("9223372036854775807");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
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
        ((JTextField) var.getCommonRep()).setText("9223372036854775808");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
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

    private final static Logger log = LoggerFactory.getLogger(SplitVariableValueTest.class);

}
