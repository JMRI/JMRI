package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.progdebugger.ProgDebugger;
import jmri.util.CvUtil;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.NotApplicable;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link SplitVariableValue} class.
 *
 * TODO need a check of the MIXED state model for long address
 * @author Bob Jacobsen Copyright 2001, 2002, 2015
 * @author Dave Heap Copyright 2019
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_LOAD_OF_KNOWN_NULL_VALUE",
    justification = "passing known null variables for clarity in constructors")
public class SplitVariableValueTest extends AbstractVariableValueTestBase {

    final String lowCV = "12";
    final String highCV = "18";

    // Local tests version of makeVar with settable parameters and cvList support.
    SplitVariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item,
            String highCV, int pFactor, int pOffset, String uppermask,
            String extra1, String extra2, String extra3, String extra4) {
        ProgDebugger pp = new ProgDebugger();

        if (!cvNum.isEmpty()) { // some variables have no CV per se
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
        if (highCV != null && !highCV.isEmpty()) {
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
        Assertions.assertEquals(val, ((JTextField) var.getCommonRep()).getText(), comment);
    }

    @Override
    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assertions.assertEquals(val, ((JLabel) var.getCommonRep()).getText(), comment);
    }

    // end of abstract members
    // some of the premade tests don't quite make sense; override them here.
    @Override
    @Test
    @NotApplicable("mask is ignored by splitAddress tests")
    public void testVariableValueCreate() {
    }

    @Override
    @Test
    @NotApplicable("low CV is upper part of address")
    public void testVariableFromCV() {
    }

    @Override
    @Test
    @NotApplicable("due to multi-cv nature of splitAddress tests")
    public void testVariableValueRead() {
    }

    @Override
    @Test
    @NotApplicable("due to multi-cv nature of splitAddress tests")
    public void testVariableValueWrite() {
    }

    @Override
    @Test
    @NotApplicable("due to multi-cv nature of splitAddress tests")
    public void testVariableCvWrite() {
    }

    @Override
    @Test
    @NotApplicable("programmer synch is different")
    public void testWriteSynch2() {
    }

    // at some point, these should pass, but have to think hard about
    // how to define the split/shift/mask operations for long CVs
    @Override
    @Test
    @Disabled("mask is ignored, test requires further development")
    public void testVariableValueCreateLargeValue() {
    }

    @Override
    @Test
    @NotApplicable("mask is ignored")
    public void testVariableValueCreateLargeMaskValue() {
    }

    @Override
    @Test
    @NotApplicable("mask is ignored")
    public void testVariableValueCreateLargeMaskValue256() {
    }

    @Override
    @Test
    @NotApplicable("mask is ignored")
    public void testVariableValueCreateLargeMaskValue2up16() {
    }

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
        Assertions.assertEquals("" + (1029), ((JTextField) var.getCommonRep()).getText(), "set var full value");
        Assertions.assertEquals(5, cv1.getValue(), "set var low bits");
        Assertions.assertEquals(4, cv2.getValue(), "set var high bits");

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assertions.assertEquals(21, cv1.getValue(), "set cv low bits");
        Assertions.assertEquals("" + (189 * 256 + 21), ((JTextField) var.getCommonRep()).getText(), "set cv full value");
        Assertions.assertEquals(189, cv2.getValue(), "set cv high bits");
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
        Assertions.assertEquals("" + (1029), ((JTextField) var.getCommonRep()).getText(), "set var full value");
        Assertions.assertEquals(0xF5, cv1.getValue(), "set var low bits");
        Assertions.assertEquals(4 * 16, cv2.getValue(), "set var high bits");

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assertions.assertEquals(21, cv1.getValue(), "set cv low bits");
        Assertions.assertEquals("" + (189 * 16 + (21 & 0xF)), ((JTextField) var.getCommonRep()).getText(), "set cv full value");
        Assertions.assertEquals(189, cv2.getValue(), "set cv high bits");
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
        Assertions.assertEquals("" + (1029), ((JTextField) var.getCommonRep()).getText(), "set var full value");
        Assertions.assertEquals(5, cv1.getValue(), "set var low bits");
        Assertions.assertEquals(0xC3 + 4 * 4, cv2.getValue(), "set var high bits");

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assertions.assertEquals(21, cv1.getValue(), "set cv low bits");
        Assertions.assertEquals("" + ((189 & 0x3C) / 4 * 256 + (21)), ((JTextField) var.getCommonRep()).getText(), "set cv full value");
        Assertions.assertEquals(189, cv2.getValue(), "set cv high bits");
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
        Assertions.assertEquals("" + (1029), ((JTextField) var.getCommonRep()).getText(), "set var full value");
        Assertions.assertEquals(5 * 2 + 0x81, cv1.getValue(), "set var low bits");
        Assertions.assertEquals(0x83 + 0x40, cv2.getValue(), "set var high bits");

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assertions.assertEquals(21, cv1.getValue(), "set cv low bits");
        Assertions.assertEquals("" + ((189 & 0x3C) / 4 * 64 + (10)), ((JTextField) var.getCommonRep()).getText(), "set cv full value");
        Assertions.assertEquals(189, cv2.getValue(), "set cv high bits");
    }

    private List<java.beans.PropertyChangeEvent> evtList = null;  // holds a list of ParameterChange events

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
        java.beans.PropertyChangeListener listen = e -> {
            evtList.add(e);
            if (e.getPropertyName().equals("Busy") && e.getNewValue().equals(Boolean.FALSE)) {
                log.debug("Busy false seen in test");
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
        JUnitUtil.waitFor(() -> !var.isBusy(), "var.isBusy");

        int nBusyFalse = 0;
        for (java.beans.PropertyChangeEvent e : evtList) {
            if (e.getPropertyName().equals("Busy") && e.getNewValue().equals(Boolean.FALSE)) {
                nBusyFalse++;
            }
        }
        Assertions.assertEquals(1, nBusyFalse, "only one Busy -> false transition ");

        Assertions.assertEquals("" + ((123 & 0x3f) + (123) * 64), ((JTextField) var.getCommonRep()).getText(), "text value ");  // 15227 = (1230x3f)*256+123
        Assertions.assertEquals(AbstractValue.ValueState.READ, var.getState(), "Var state");
        Assertions.assertEquals(123, cv1.getValue(), "CV 1 value ");  // 123 with 128 bit set
        Assertions.assertEquals(123, cv2.getValue(), "CV 2 value ");
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
        JUnitUtil.waitFor(() -> !var.isBusy(), "var.isBusy");

        Assertions.assertEquals(61, cv1.getValue(), "CV 1 value ");
        Assertions.assertEquals(74, cv2.getValue(), "CV 2 value ");
        Assertions.assertEquals("4797", ((JTextField) var.getCommonRep()).getText(), "text ");
        Assertions.assertEquals(AbstractValue.ValueState.STORED, var.getState(), "Var state");
        Assertions.assertEquals(74, p.lastWrite(), "last write");
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
        String mHighCV = "";
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
                mHighCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assertions.assertNotNull(var, "makeVar returned null");

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(8, cv.length, "number of CVs is");

        Assertions.assertEquals("275", cv[0].number(), "cv[0] is");
        Assertions.assertEquals("276", cv[1].number(), "cv[1] is");
        Assertions.assertEquals("277", cv[2].number(), "cv[2] is");
        Assertions.assertEquals("278", cv[3].number(), "cv[3] is");
        Assertions.assertEquals("279", cv[4].number(), "cv[4] is");
        Assertions.assertEquals("280", cv[5].number(), "cv[5] is");
        Assertions.assertEquals("281", cv[6].number(), "cv[6] is");
        Assertions.assertEquals("282", cv[7].number(), "cv[7] is");

        // start with a random value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.focusLost(focusEvent);
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

        // change to maximum unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("18446744073709551615");
        var.focusLost(focusEvent);
        Assertions.assertEquals("18446744073709551615", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0xFF, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0xFF, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xFF, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0xFF, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0xFF, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0xFF, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0xFF, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0xFF, cv[7].getValue(), "set CV" + cv[7].number());

        // change to one less than maximum unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("18446744073709551614");
        var.focusLost(focusEvent);
        Assertions.assertEquals("18446744073709551614", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0xFE, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0xFF, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xFF, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0xFF, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0xFF, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0xFF, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0xFF, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0xFF, cv[7].getValue(), "set CV" + cv[7].number());

        // change to last 63 bit unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("9223372036854775807");
        var.focusLost(focusEvent);
        Assertions.assertEquals("9223372036854775807", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0xFF, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0xFF, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xFF, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0xFF, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0xFF, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0xFF, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0xFF, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x7F, cv[7].getValue(), "set CV" + cv[7].number());

        // change to first 64 bit unsigned value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("9223372036854775808");
        var.focusLost(focusEvent);
        Assertions.assertEquals("9223372036854775808", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x00, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x00, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0x00, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x00, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x80, cv[7].getValue(), "set CV" + cv[7].number());

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
        String mHighCV = "";
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
                mHighCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assertions.assertNotNull(var, "makeVar returned null");

        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(8, cv.length, "number of CVs is");

        Assertions.assertEquals("275", cv[0].number(), "cv[0] is");
        Assertions.assertEquals("276", cv[1].number(), "cv[1] is");
        Assertions.assertEquals("277", cv[2].number(), "cv[2] is");
        Assertions.assertEquals("278", cv[3].number(), "cv[3] is");
        Assertions.assertEquals("279", cv[4].number(), "cv[4] is");
        Assertions.assertEquals("280", cv[5].number(), "cv[5] is");
        Assertions.assertEquals("281", cv[6].number(), "cv[6] is");
        Assertions.assertEquals("282", cv[7].number(), "cv[7] is");

        // start with a random value
        cv[0].setValue(0x0F);
        cv[1].setValue(0x72);
        cv[2].setValue(0xD2);
        cv[3].setValue(0x7F);
        cv[4].setValue(0x00);
        cv[5].setValue(0x00);
        cv[6].setValue(0x00);
        cv[7].setValue(0x00);
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change to maximum unsigned value
        cv[0].setValue(0xFF);
        cv[1].setValue(0xFF);
        cv[2].setValue(0xFF);
        cv[3].setValue(0xFF);
        cv[4].setValue(0xFF);
        cv[5].setValue(0xFF);
        cv[6].setValue(0xFF);
        cv[7].setValue(0xFF);
        Assertions.assertEquals(0xFF, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0xFF, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xFF, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0xFF, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0xFF, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0xFF, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0xFF, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0xFF, cv[7].getValue(), "set CV" + cv[7].number());
        Assertions.assertEquals("18446744073709551615", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change to one less than maximum unsigned value
        cv[0].setValue(0xFE);
        cv[1].setValue(0xFF);
        cv[2].setValue(0xFF);
        cv[3].setValue(0xFF);
        cv[4].setValue(0xFF);
        cv[5].setValue(0xFF);
        cv[6].setValue(0xFF);
        cv[7].setValue(0xFF);
        Assertions.assertEquals(0xFE, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0xFF, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xFF, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0xFF, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0xFF, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0xFF, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0xFF, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0xFF, cv[7].getValue(), "set CV" + cv[7].number());
        Assertions.assertEquals("18446744073709551614", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change to last 63 bit unsigned value
        cv[0].setValue(0xFF);
        cv[1].setValue(0xFF);
        cv[2].setValue(0xFF);
        cv[3].setValue(0xFF);
        cv[4].setValue(0xFF);
        cv[5].setValue(0xFF);
        cv[6].setValue(0xFF);
        cv[7].setValue(0x7F);
        Assertions.assertEquals(0xFF, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0xFF, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xFF, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0xFF, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0xFF, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0xFF, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0xFF, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x7F, cv[7].getValue(), "set CV" + cv[7].number());
        Assertions.assertEquals("9223372036854775807", ((JTextField) var.getCommonRep()).getText(), "set var text value");

        // change to first 64 bit unsigned value
        cv[0].setValue(0x00);
        cv[1].setValue(0x00);
        cv[2].setValue(0x00);
        cv[3].setValue(0x00);
        cv[4].setValue(0x00);
        cv[5].setValue(0x00);
        cv[6].setValue(0x00);
        cv[7].setValue(0x80);
        Assertions.assertEquals(0x00, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x00, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0x00, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x00, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x80, cv[7].getValue(), "set CV" + cv[7].number());
        Assertions.assertEquals("9223372036854775808", ((JTextField) var.getCommonRep()).getText(), "set var text value");

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
        Assertions.assertNotNull(var, "makeVar returned null");

        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(2, cv.length, "number of CVs is");

        Assertions.assertEquals(lowCVnum, cv[0].number(), "cv[0] is");
        Assertions.assertEquals(highCVnum, cv[1].number(), "cv[1] is");

        // Start with all zero values
        var.setLongValue(0x00);
        cv[0].setValue(0x00);
        cv[1].setValue(0x00);
        Assertions.assertEquals(0x00, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(0x00, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(0x00, var.getLongValue(), "get Value");
        Assertions.assertEquals("0", ((JTextField) var.getCommonRep()).getText(), "get text value");

        // Following samples provided by Robin Becker
        cv[0].setValue(255);
        cv[1].setValue(0);
        Assertions.assertEquals(255, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(0, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(9, var.getLongValue(), "get Value");
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(0);
        cv[1].setValue(255);
        Assertions.assertEquals(0, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(255, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(16, var.getLongValue(), "get Value");
        Assertions.assertEquals("16", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(2);
        cv[1].setValue(255);
        Assertions.assertEquals(2, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(255, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(17, var.getLongValue(), "get Value");
        Assertions.assertEquals("17", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(255);
        cv[1].setValue(2);
        Assertions.assertEquals(255, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(2, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(9, var.getLongValue(), "get Value");
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(255);
        cv[1].setValue(255);
        Assertions.assertEquals(255, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(255, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(25, var.getLongValue(), "get Value");
        Assertions.assertEquals("25", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(1);
        cv[1].setValue(240);
        Assertions.assertEquals(1, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(0, var.getLongValue(), "get Value");
        Assertions.assertEquals("0", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(4);
        cv[1].setValue(240);
        Assertions.assertEquals(4, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(0, var.getLongValue(), "get Value");
        Assertions.assertEquals("0", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(7);
        cv[1].setValue(240);
        Assertions.assertEquals(7, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(1, var.getLongValue(), "get Value");
        Assertions.assertEquals("1", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(2);
        cv[1].setValue(240);
        Assertions.assertEquals(2, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(1, var.getLongValue(), "get Value");
        Assertions.assertEquals("1", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(0);
        cv[1].setValue(15);
        Assertions.assertEquals(0, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(15, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(16, var.getLongValue(), "get Value");
        Assertions.assertEquals("16", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(15);
        cv[1].setValue(15);
        Assertions.assertEquals(15, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(15, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(17, var.getLongValue(), "get Value");
        Assertions.assertEquals("17", ((JTextField) var.getCommonRep()).getText(), "get text value");

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
        Assertions.assertNotNull(var, "makeVar returned null");

        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(2, cv.length, "number of CVs is");

        Assertions.assertEquals(lowCVnum, cv[0].number(), "cv[0] is");
        Assertions.assertEquals(highCVnum, cv[1].number(), "cv[1] is");

        // We will set all ignored bits in the CVs to check that they don't affect the result.
        int lowIgnored = ~var.maskValAsInt(mask) & 0xFF;
        int highIgnored = ~var.maskValAsInt(uppermask) & 0xFF;

        // Start with all zero values
        var.setLongValue(0x00);
        cv[0].setValue(0x00 | lowIgnored);
        cv[1].setValue(0x00 | highIgnored);
        Assertions.assertEquals(0x00 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(0x00 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(0x00, var.getLongValue(), "get Value");
        Assertions.assertEquals("0", ((JTextField) var.getCommonRep()).getText(), "get text value");

        // Following samples provided by Robin Becker
        cv[0].setValue(255 | lowIgnored);
        cv[1].setValue(0 | highIgnored);
        Assertions.assertEquals(255 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(0 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(9, var.getLongValue(), "get Value");
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(0 | lowIgnored);
        cv[1].setValue(255 | highIgnored);
        Assertions.assertEquals(0 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(255 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(16, var.getLongValue(), "get Value");
        Assertions.assertEquals("16", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(2 | lowIgnored);
        cv[1].setValue(255 | highIgnored);
        Assertions.assertEquals(2 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(255 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(17, var.getLongValue(), "get Value");
        Assertions.assertEquals("17", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(255 | lowIgnored);
        cv[1].setValue(2 | highIgnored);
        Assertions.assertEquals(255 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(2 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(9, var.getLongValue(), "get Value");
        Assertions.assertEquals("9", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(255 | lowIgnored);
        cv[1].setValue(255 | highIgnored);
        Assertions.assertEquals(255 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(255 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(25, var.getLongValue(), "get Value");
        Assertions.assertEquals("25", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(1 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assertions.assertEquals(1 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(0, var.getLongValue(), "get Value");
        Assertions.assertEquals("0", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(4 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assertions.assertEquals(4 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(0, var.getLongValue(), "get Value");
        Assertions.assertEquals("0", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(7 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assertions.assertEquals(7 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(1, var.getLongValue(), "get Value");
        Assertions.assertEquals("1", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(2 | lowIgnored);
        cv[1].setValue(240 | highIgnored);
        Assertions.assertEquals(2 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(240 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(1, var.getLongValue(), "get Value");
        Assertions.assertEquals("1", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(0 | lowIgnored);
        cv[1].setValue(15 | highIgnored);
        Assertions.assertEquals(0 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(15 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(16, var.getLongValue(), "get Value");
        Assertions.assertEquals("16", ((JTextField) var.getCommonRep()).getText(), "get text value");

        cv[0].setValue(15 | lowIgnored);
        cv[1].setValue(15 | highIgnored);
        Assertions.assertEquals(15 | lowIgnored, cv[0].getValue(), "get CV" + cv[0].number());
        Assertions.assertEquals(15 | highIgnored, cv[1].getValue(), "get CV" + cv[1].number());
        Assertions.assertEquals(17, var.getLongValue(), "get Value");
        Assertions.assertEquals("17", ((JTextField) var.getCommonRep()).getText(), "get text value");

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
        String mHighCV = "";
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
                mHighCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assertions.assertNotNull(var, "makeVar returned null");

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(8, cv.length, "number of CVs is");

        Assertions.assertEquals("275", cv[0].number(), "cv[0] is");
        Assertions.assertEquals("276", cv[1].number(), "cv[1] is");
        Assertions.assertEquals("277", cv[2].number(), "cv[2] is");
        Assertions.assertEquals("278", cv[3].number(), "cv[3] is");
        Assertions.assertEquals("279", cv[4].number(), "cv[4] is");
        Assertions.assertEquals("280", cv[5].number(), "cv[5] is");
        Assertions.assertEquals("281", cv[6].number(), "cv[6] is");
        Assertions.assertEquals("282", cv[7].number(), "cv[7] is");

        // start with a random value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.focusLost(focusEvent);
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

        // change text to an invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("184467F4079551615");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

        // change text to another invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("3G");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

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
        String mHighCV = "";
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
                mHighCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        Assertions.assertNotNull(var, "makeVar returned null");

        ActionEvent actionEvent = new ActionEvent(var.getCommonRep(), ActionEvent.ACTION_PERFORMED, name);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assertions.assertEquals(8, cv.length, "number of CVs is");

        Assertions.assertEquals("275", cv[0].number(), "cv[0] is");
        Assertions.assertEquals("276", cv[1].number(), "cv[1] is");
        Assertions.assertEquals("277", cv[2].number(), "cv[2] is");
        Assertions.assertEquals("278", cv[3].number(), "cv[3] is");
        Assertions.assertEquals("279", cv[4].number(), "cv[4] is");
        Assertions.assertEquals("280", cv[5].number(), "cv[5] is");
        Assertions.assertEquals("281", cv[6].number(), "cv[6] is");
        Assertions.assertEquals("282", cv[7].number(), "cv[7] is");

        // start with a random value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("2144498191");
        var.actionPerformed(actionEvent);
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

        // change text to an invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("184467F4079551615");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

        // change text to another invalid long value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("3G");
        var.actionPerformed(actionEvent);
        // ensure original text restored and value unchanged
        Assertions.assertEquals("2144498191", ((JTextField) var.getCommonRep()).getText(), "set var text value");
        Assertions.assertEquals(0x0F, cv[0].getValue(), "set CV" + cv[0].number());
        Assertions.assertEquals(0x72, cv[1].getValue(), "set CV" + cv[1].number());
        Assertions.assertEquals(0xD2, cv[2].getValue(), "set CV" + cv[2].number());
        Assertions.assertEquals(0x7F, cv[3].getValue(), "set CV" + cv[3].number());
        Assertions.assertEquals(0x00, cv[4].getValue(), "set CV" + cv[4].number());
        Assertions.assertEquals(0x00, cv[5].getValue(), "set CV" + cv[5].number());
        Assertions.assertEquals(0x00, cv[6].getValue(), "set CV" + cv[6].number());
        Assertions.assertEquals(0x00, cv[7].getValue(), "set CV" + cv[7].number());

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

        Assertions.assertEquals("VXXXXXXX XXXXVVVV", var.getMask(), "mask at start");
        Assertions.assertEquals("XXXXVVVV", var.getMask(1), "mask 2");
        Assertions.assertEquals("XXXXVVVV", var.getMask(2), "mask 3");

        var.simplifyMask(); // no effect on mask returned
        Assertions.assertEquals("VXXXXXXX XXXXVVVV", var.getMask(), "mask after simplify");
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SplitVariableValueTest.class);

}
