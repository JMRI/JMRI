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
 * Tests for the {@link SplitHundredsVariableValue} class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2015
 * @author Dave Heap Copyright 2019
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_LOAD_OF_KNOWN_NULL_VALUE",
    justification = "passing known null variables for clarity in constructors")
public class SplitHundredsVariableValueTest extends AbstractVariableValueTestBase {

    // Local tests version of makeVar with extra parameters and cvList support.
    SplitHundredsVariableValue makeVar(String label, String comment, String cvName,
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
        return new SplitHundredsVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
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
    @NotApplicable("mask is ignored, test requires further development")
    public void testVariableValueCreateLargeValue() {
    }

    @Override
    @Test
    @Disabled("Test requires further development")
    public void testVariableSynch() {
    }

    @Override
    @Test
    @Disabled("Test requires further development")
    public void testVariableReadOnly() {
    }

    @Override
    @Test
    @Disabled("Test requires further development")
    public void testVariableValueStates() {
    }

    @Override
    @Test
    @Disabled("Test requires further development")
    public void testVariableRepStateColor() {
    }

    @Override
    @Test
    @Disabled("Test requires further development")
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
        String name = "Hundreds Field";
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
    public void testGetTextFromValue() {
        String name = "Hundreds Field";
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
        SplitHundredsVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        Assert.assertEquals("3", "3", var.getTextFromValue(3));
        
        Assert.assertEquals("12", "12", var.getTextFromValue(12L));
        
        Assert.assertEquals("123", "123", var.getTextFromValue(256*1L+23L));
        
        Assert.assertEquals("12345", "12345", var.getTextFromValue(256*256*1L + 256*23L + 45L));
        
    }

    @Test
    public void testGetValueFromText() {
        String name = "Hundreds Field";
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
        SplitHundredsVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, displayCase, extra2, extra3, extra4);
        Assert.assertNotNull("makeVar returned null", var);

        Assert.assertEquals("3", 3L, var.getValueFromText("3"));
        
        Assert.assertEquals("12", 12L, var.getValueFromText("12"));
        
        Assert.assertEquals("123", 256*1L+23L, var.getValueFromText("123"));
        
        Assert.assertEquals("12345", 256*256*1L + 256*23L + 45L, var.getValueFromText("12345"));
        
    }
    
    @Test
    public void testCvChange() {
        String name = "Hundreds Field";
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
        SplitHundredsVariableValue var = makeVar(name, comment, cvName,
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
        ((JTextField) var.getCommonRep()).setText("12345678");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var full value", "12345678", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set CV" + cv[0].number(), 78, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 56, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 34, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 12, cv[3].getValue());

        // change some CVs, expect to see a change in the variable value
        cv[0].setValue(98);
        cv[1].setValue(76);

        Assert.assertEquals("set CV" + cv[0].number(), 98, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 76, cv[1].getValue());

        Assert.assertEquals("set var full value", "12347698", ((JTextField) var.getCommonRep()).getText());

        // change sto zero value
        cv[0].setValue(0);
        cv[1].setValue(0);
        cv[2].setValue(0);
        cv[3].setValue(0);

        Assert.assertEquals("set CV" + cv[0].number(), 0, cv[0].getValue());
        Assert.assertEquals("set CV" + cv[1].number(), 0, cv[1].getValue());
        Assert.assertEquals("set CV" + cv[2].number(), 0, cv[2].getValue());
        Assert.assertEquals("set CV" + cv[3].number(), 0, cv[3].getValue());

        Assert.assertEquals("set var full value", "0", ((JTextField) var.getCommonRep()).getText());
    }

    @Test
    public void testTextShorten() {
        String name = "Hundreds Field";
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
        SplitHundredsVariableValue var = makeVar(name, comment, cvName,
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
        ((JTextField) var.getCommonRep()).setText("5678");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "5678", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501", 78, cv501.getValue());
        Assert.assertEquals("set var cv502", 56, cv502.getValue());
        Assert.assertEquals("set var cv503",  0, cv503.getValue());
        Assert.assertEquals("set var cv504",  0, cv504.getValue());

        // change to shorter text
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("203");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "203", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501",  3, cv501.getValue());
        Assert.assertEquals("set var cv502",  2, cv502.getValue());
        Assert.assertEquals("set var cv503",  0, cv503.getValue());
        Assert.assertEquals("set var cv504",  0, cv504.getValue());

        // change to 0 input
        var.focusGained(focusEvent);
        ((JTextField) var.getCommonRep()).setText("0");  // to start with a value
        var.focusLost(focusEvent);
        Assert.assertEquals("set var text value", "0", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("set var cv501",  0, cv501.getValue());
        Assert.assertEquals("set var cv502",  0, cv502.getValue());
        Assert.assertEquals("set var cv503",  0, cv503.getValue());
        Assert.assertEquals("set var cv504",  0, cv504.getValue());

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

//    private final static Logger log = LoggerFactory.getLogger(SplitHundredsVariableValueTest.class);
}
