package jmri.jmrit.symbolicprog;

import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.util.CvUtil;
import jmri.util.junit.annotations.NotApplicable;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link SplitHexVariableValue} class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2015
 * @author Dave Heap Copyright 2019
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_LOAD_OF_KNOWN_NULL_VALUE",
    justification = "passing known null variables for clarity in constructors")
public class SplitHexVariableValueTest extends AbstractVariableValueTestBase {

    // Local tests version of makeVar with extra parameters and cvList support.
    SplitHexVariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item,
            String highCV, int pFactor, int pOffset, String uppermask,
            String extra1, String extra2, String extra3, String extra4) {

        if (!cvNum.isEmpty()) { // some variables have no CV per se
            List<String> cvList = CvUtil.expandCvList(cvNum);
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
        if (highCV != null && !highCV.isEmpty()) {
            CvValue cvNext = new CvValue(highCV, p);
            cvNext.setValue(0);
            v.put(highCV, cvNext);
        }
        return new SplitHexVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item,
                highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "VVVVVVVV";
        String displayCase = "lower";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;

        return makeVar(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal, v, status, item,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((SplitVariableValue) var).setLongValue(Long.parseUnsignedLong(val, 16));
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
    @NotApplicable("mask is ignored by splitAddress tests")
    public void testVariableValueCreate() {
    }

    @Override
    @Test
    @NotApplicable("mask is ignored")
    public void testVariableValueTwinMask() {
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
    @Disabled("test requires further development")
    public void testVariableSynch() {
    }

    @Override
    @Test
    @Disabled("test requires further development")
    public void testVariableReadOnly() {
    }

    @Override
    @Test
    @Disabled("test requires further development")
    public void testVariableValueStates() {
    }

    @Override
    @Test
    @Disabled("test requires further development")
    public void testVariableRepStateColor() {
    }

    @Override
    @Test
    @Disabled("test requires further development")
    public void testVariableVarChangeColorRep() {
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
    public void testCtor() {
        String name = "Hex Field";
        String comment = "";
        String cvName = "275:4";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:4";
        String mask = "VVVVVVVV";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        VariableValue instance = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname);
        Assert.assertNotNull("testCtor returned null", instance);
    }

    @Test
    public void testCvChangesUpper0() {
        String name = "Hex Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:4";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 4, cv.length);

        Assert.assertEquals("cv[0] is", "275", cv[0].number());
        Assert.assertEquals("cv[1] is", "276", cv[1].number());
        Assert.assertEquals("cv[2] is", "277", cv[2].number());
        Assert.assertEquals("cv[3] is", "278", cv[3].number());

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("FFD2720F");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "FFD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());

        // change some CVs, expect to see a change in the variable value
        cv[0].setValue(0x21);
        cv[1].setValue(0x89);

        Assert.assertEquals("set CV" + cv[0].number(), 0x21, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x89, cv[1].getValue());

        Assert.assertEquals("set var full value", "FFD28921", ((JTextField) var.getCommonRep()).getText());
    }

    @Test
    public void testCvChangesUpper1() {
        String name = "Hex Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "392:-4";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);
        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 4, cv.length);

        Assert.assertEquals("cv[0] is", "392", cv[0].number());
        Assert.assertEquals("cv[1] is", "391", cv[1].number());
        Assert.assertEquals("cv[2] is", "390", cv[2].number());
        Assert.assertEquals("cv[3] is", "389", cv[3].number());

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("FFD2720F");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "FFD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());

        // change some CVs, expect to see a change in the variable value
        cv[0].setValue(0x21);
        cv[1].setValue(0x89);

        Assert.assertEquals("set CV" + cv[0].number(), 0x21, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x89, cv[1].getValue());

        Assert.assertEquals("set var full value", "FFD28921", ((JTextField) var.getCommonRep()).getText());
    }

    @Test
    public void testCvChangesLower() {
        String name = "Hex Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "(392,255,43,870)";
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
        String displayCase = "lower";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is", 4, cv.length);

        Assert.assertEquals("cv[0] is", "392", cv[0].number());
        Assert.assertEquals("cv[1] is", "255", cv[1].number());
        Assert.assertEquals("cv[2] is", "43", cv[2].number());
        Assert.assertEquals("cv[3] is", "870", cv[3].number());

        ((JTextField) var.getCommonRep()).setText("FFD2720F");  // to start with a value
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name)); // notify that we changed variable value
        Assert.assertEquals("set var full value", "ffd2720f", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());

        // change some CVs, expect to see a change in the variable value
        cv[0].setValue(0x21);
        cv[1].setValue(0x89);
        Assert.assertEquals("set CV" + cv[0].number(), 0x21, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x89, cv[1].getValue());

        Assert.assertEquals("set var full value", "ffd28921", ((JTextField) var.getCommonRep()).getText());
    }

    @Test
    public void testTextShorten1() {
        String name = "Hex Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "50(1-4)";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        CvValue cv501 = v.get("501");
        CvValue cv502 = v.get("502");
        CvValue cv503 = v.get("503");
        CvValue cv504 = v.get("504");

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("FFD2720F");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "FFD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501", 0x0F, cv501.getValue());
        Assert.assertEquals("set var cv502", 0x72, cv502.getValue());
        Assert.assertEquals("set var cv503", 0xD2, cv503.getValue());
        Assert.assertEquals("set var cv504", 0xFF, cv504.getValue());

        // change to shorter text
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("F837");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "F837", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501", 0x37, cv501.getValue());
        Assert.assertEquals("set var cv502", 0xF8, cv502.getValue());
        Assert.assertEquals("set var cv503", 0x00, cv503.getValue());
        Assert.assertEquals("set var cv504", 0x00, cv504.getValue());

    }

    @Test
    public void testTextShorten2() {
        String name = "Hex Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "50(1-4)";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);
        FocusEvent focusEvent = new FocusEvent(var.getCommonRep(), 0, true);

        CvValue cv501 = v.get("501");
        CvValue cv502 = v.get("502");
        CvValue cv503 = v.get("503");
        CvValue cv504 = v.get("504");

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("FFD2720F");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "FFD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501", 0x0F, cv501.getValue());
        Assert.assertEquals("set var cv502", 0x72, cv502.getValue());
        Assert.assertEquals("set var cv503", 0xD2, cv503.getValue());
        Assert.assertEquals("set var cv504", 0xFF, cv504.getValue());

        // change to shorter text
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("837");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "837", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501", 0x37, cv501.getValue());
        Assert.assertEquals("set var cv502", 0x08, cv502.getValue());
        Assert.assertEquals("set var cv503", 0x00, cv503.getValue());
        Assert.assertEquals("set var cv504", 0x00, cv504.getValue());

    }

    @Test
    public void testTextMaxHexVal() {
        String name = "Hex Field";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
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

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("7FD2720F");  // to start with a random value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "7FD2720F", ((JTextField) var.getCommonRep()).getText());
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
        ((JTextField) var.getCommonRep()).setText("FFFFFFFFFFFFFFFF");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "FFFFFFFFFFFFFFFF", ((JTextField) var.getCommonRep()).getText());
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
        ((JTextField) var.getCommonRep()).setText("FFFFFFFFFFFFFFFE");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "FFFFFFFFFFFFFFFE", ((JTextField) var.getCommonRep()).getText());
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
        ((JTextField) var.getCommonRep()).setText("7FFFFFFFFFFFFFFF");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "7FFFFFFFFFFFFFFF", ((JTextField) var.getCommonRep()).getText());
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
        ((JTextField) var.getCommonRep()).setText("8000000000000000");
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "8000000000000000", ((JTextField) var.getCommonRep()).getText());
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
    public void testCvChangesMaxHexVal() {
        String name = "Hex Field";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
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
        Assert.assertEquals("set var text value", "7FD2720F", ((JTextField) var.getCommonRep()).getText());

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
        Assert.assertEquals("set var text value", "FFFFFFFFFFFFFFFF", ((JTextField) var.getCommonRep()).getText());

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
        Assert.assertEquals("set var text value", "FFFFFFFFFFFFFFFE", ((JTextField) var.getCommonRep()).getText());

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
        Assert.assertEquals("set var text value", "7FFFFFFFFFFFFFFF", ((JTextField) var.getCommonRep()).getText());

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
        Assert.assertEquals("set var text value", "8000000000000000", ((JTextField) var.getCommonRep()).getText());

    }

    @Test
    public void testTextInvalidHexValEntered() {
        String name = "Hex Field";
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
        String displayCase = "upper";
        String extra2 = null;
        String extra3 = null;
        String extra4 = null;
        SplitHexVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
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

        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("7FD2720F");  // to start with a random value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "7FD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change text to an invalid hex value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("FFFFFFFGFFFFFFFF");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assert.assertEquals("set var text value", "7FD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

        // change text to another invalid hex value
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("3H");
        var.focusLost(focusEvent);
        // ensure original text restored and value unchanged
        Assert.assertEquals("set var text value", "7FD2720F", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 0x0F, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0x72, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0x7F, cv[3].getValue());
        Assert.assertEquals("set CV" + cv[4].number(), 0x00, cv[4].getValue());
        Assert.assertEquals("set CV" + cv[5].number(), 0x00, cv[5].getValue());
        Assert.assertEquals("set CV" + cv[6].number(), 0x00, cv[6].getValue());
        Assert.assertEquals("set CV" + cv[7].number(), 0x00, cv[7].getValue());

    }

    // from here down is testing infrastructure
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

//    private final static Logger log = LoggerFactory.getLogger(SplitHexVariableValueTest.class);
}
