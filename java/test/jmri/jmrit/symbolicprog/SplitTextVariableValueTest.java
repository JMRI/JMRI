package jmri.jmrit.symbolicprog;

import static java.nio.charset.Charset.defaultCharset;

import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.progdebugger.ProgDebugger;
import jmri.util.CvUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SplitTextVariableValueTest.java
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2015
 * @author Dave Heap Copyright 2019
 */
public class SplitTextVariableValueTest extends AbstractVariableValueTestBase {

    // Local tests version of makeVar with extra parameters.
    SplitTextVariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item,
            String highCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        ProgDebugger p = new ProgDebugger();

        if (!cvNum.equals("")) { // some variables have no CV per se
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
        if (highCV != null) {
            CvValue cvNext = new CvValue(highCV, p);
            cvNext.setValue(0);
            v.put(highCV, cvNext);
        }
        return new SplitTextVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item, highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        String highCV = null;
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "VVVVVVVV";
        String match = "";
        String termByteStr = "";
        String padByteStr = "";
        String charSet = defaultCharset().name();

        return makeVar(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item, highCV, pFactor, pOffset, uppermask, match, termByteStr, padByteStr, charSet);
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((SplitVariableValue) var).setValue(Integer.parseInt(val));
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
    public void testVariableValueCreate() {
    }// mask is ignored by splitAddre

    @Override
    public void testVariableFromCV() {
    }     // low CV is upper part of address

    @Override
    public void testVariableValueRead() {
    } // due to multi-cv nature of SplitAddr
    // public void testVariableReadOnly() {} // due to multi-cv nature of SplitAddr

    @Override
    public void testVariableValueWrite() {
    } // due to multi-cv nature of SplitAddr

    @Override
    public void testVariableCvWrite() {
    }    // due to multi-cv nature of SplitAddr

    @Override
    public void testWriteSynch2() {
    }        // programmer synch is different

    // at some point, these should pass, but have to think hard about
    // how to define the split/shift/mask operations for long CVs
    @Override
    public void testVariableValueCreateLargeValue() {
    } // mask is ignored

    @Override
    public void testVariableSynch() {
    }

    @Override
    public void testVariableReadOnly() {
    }

    @Override
    public void testVariableValueCreateLargeMaskValue() {
    } // mask is ignored

    @Override
    public void testVariableValueCreateLargeMaskValue256() {
    } // mask is ignored

    @Override
    public void testVariableValueCreateLargeMaskValue2up16() {
    } // mask is ignored

    // Local tests
    @Test
    public void testCtor() {
        String name = "Text Field";
        String comment = "";
        String cvName = "23:2";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "23:2";
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
    public void test1() {
        String name = "Text Field";
        String comment = "";
        String cvName = "";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "275:20";
        String mask = "VVVVVVVV";
        int minVal = 0;
        int maxVal = 0;
        HashMap<String, CvValue> v = createCvMap();
        JLabel status = new JLabel();
        String stdname = "";
        String highCV = null;
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String match = "";
        String termByteStr = "45";
        String padByteStr = "50";
        String charSet = defaultCharset().name();
        SplitTextVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, match, termByteStr, padByteStr, charSet);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs();

        Assert.assertEquals("number of CVs is ", 20, cv.length);

        int startCV = 275;
        int cvInc = 1;

        for (int i = 0; i < cv.length; i++) {
            Assert.assertEquals("cv[" + i + "] is ", (startCV + (cvInc * i)) + "", cv[i].number());
            log.warn("Contents of CV{} is '{}'", cv[i].number(), (cv[i].getValue()));
        }

        String testStr = "Test String";
        ((JTextField) var.getCommonRep()).setText(testStr);  // to start with a value
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
        String resultStr = ((JTextField) var.getCommonRep()).getText();
        Assert.assertEquals("length of result string", testStr.length(), resultStr.length());
        Assert.assertEquals("contents of result string", testStr, resultStr);

        // debugging
        for (int i = 0; i < cv.length; i++) {
            log.warn("Contents of CV{} is '{}'", cv[i].number(), (cv[i].getValue()));
        }

        // check that CVs were loaded correctly
        for (int i = 0; i < (resultStr.length()); i++) {
            log.warn("Character {} of result string'{}'is '{}' value '{}'", i, resultStr, resultStr.charAt(i), (int) resultStr.charAt(i));
            Assert.assertEquals("set CV" + cv[i].number(), testStr.charAt(i), (cv[i].getValue()));
        }
        // check string terminator byte if applicable
        if ((resultStr.length() < cv.length) && !termByteStr.equals("")) {
            Assert.assertEquals("check string terminator byte", (byte) Integer.parseUnsignedInt(termByteStr), (cv[resultStr.length()].getValue()));
        }
        // check string terminator byte if applicable
        if ((resultStr.length() < cv.length - 1) && !padByteStr.equals("")) {
            for (int i = cv.length + 1; i < cv.length; i++) {
                Assert.assertEquals("check string pad byte at " + i, (byte) Integer.parseUnsignedInt(padByteStr) + 1, (cv[resultStr.length()].getValue()));
            }
        }

//        Assert.assertEquals("set var full value", "Test String", ((JTextField) var.getCommonRep()).getText());
//        for (int i = 0; i < (testStr.length()); i++) {
//            log.warn("Character {} of source string'{}'is '{}' value '{}'", i, testStr, testStr.charAt(i), (int) testStr.charAt(i));
//        }
//        Assert.assertEquals("set CV" + cv[0].number(), 'T', (cv[0].getValue()));
//        Assert.assertEquals("set CV" + cv[testStr.length()].number(), 0, cv[testStr.length()].getValue());
//        Assert.assertEquals("set CV" + cv[2].number(), 0xD2, cv[2].getValue());
//        Assert.assertEquals("set CV" + cv[3].number(), 0xFF, cv[3].getValue());
//
//        // change some CVs, expect to see a change in the variable value
//        cv[0].setValue(0x21);
//        cv[1].setValue(0x89);
//
//        Assert.assertEquals("set CV" + cv[0].number(), 0x21, cv[0].getValue());
//        Assert.assertEquals("set CV" + cv[1].number(), 0x89, cv[1].getValue());
//
//        Assert.assertEquals("set var full value", "FFD28921", ((JTextField) var.getCommonRep()).getText());
    }
    // from here down is testing infrastructure

    public SplitTextVariableValueTest(String s) {
        super(s);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitTextVariableValueTest.class
            .getName());

}
