package jmri.jmrit.symbolicprog;

import static java.nio.charset.Charset.defaultCharset;

import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.progdebugger.ProgDebugger;
import jmri.util.CvUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link SplitTextVariableValue} class.
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
        if (highCV != null && !highCV.equals("")) {
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
        String highCV = "";
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
        ((SplitVariableValue) var).setLongValue(Integer.parseInt(val));
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
    } // due to multi-cv nature of SplitAddr

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
    public void testVariableSynch() {
    }

    @Override
    @Test
    public void testVariableReadOnly() {
    }

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
    public void testLoadStringDefaultMatchTermBytePadByte() {
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
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String match = "";
        String termByteStr = "0";
        String padByteStr = "0";
        String charSet = defaultCharset().name();

        String beforeStr;
        String testStr;
        String resultStr;

        SplitTextVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, match, termByteStr, padByteStr, charSet);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs(); // get an array of the used CVs (access by offset)

        // edit to match specification in string cvNum above
        int startCV = 275;
        int cvInc = 1;
        Assert.assertEquals("number of CVs is ", 20, cv.length);

        // Check for correct CV allocation
        for (int i = 0; i < cv.length; i++) {
            Assert.assertEquals("cv[" + i + "] is ", (startCV + (cvInc * i)) + "", cv[i].number());
            log.debug("Contents of CV{} is '{}'", cv[i].number(), (cv[i].getValue()));
        }
        //Now for some tests

        testStr = "Test String";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test Short";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String Longer";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String tooo Long";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String All Good";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

    }

    @Test
    public void testLoadStringCustomMatch() {
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
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String match = "[a-zA-Z0-9]*";
        String termByteStr = "0";
        String padByteStr = "0";
        String charSet = defaultCharset().name();

        String beforeStr;
        String testStr;
        String resultStr;

        SplitTextVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, match, termByteStr, padByteStr, charSet);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs(); // get an array of the used CVs (access by offset)

        // edit to match specification in string cvNum above
        int startCV = 275;
        int cvInc = 1;
        Assert.assertEquals("number of CVs is ", 20, cv.length);

        // Check for correct CV allocation
        for (int i = 0; i < cv.length; i++) {
            Assert.assertEquals("cv[" + i + "] is ", (startCV + (cvInc * i)) + "", cv[i].number());
            log.debug("Contents of CV{} is '{}'", cv[i].number(), (cv[i].getValue()));
        }
        //Now for some tests

        testStr = "TestValid12345";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test Invalid";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String Longer";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String tooo Long";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String All Good";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

    }

    @Test
    public void testLoadStringCustomTermBytePadByte() {
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
        String highCV = "";
        int pFactor = 1;
        int pOffset = 0;
        String uppermask = "";
        String match = "";
        String termByteStr = "45";
        String padByteStr = "50";
        String charSet = defaultCharset().name();

        String beforeStr;
        String testStr;
        String resultStr;

        SplitTextVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, match, termByteStr, padByteStr, charSet);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue[] cv = var.usesCVs(); // get an array of the used CVs (access by offset)

        // edit to match specification in string cvNum above
        int startCV = 275;
        int cvInc = 1;
        Assert.assertEquals("number of CVs is ", 20, cv.length);

        // Check for correct CV allocation
        for (int i = 0; i < cv.length; i++) {
            Assert.assertEquals("cv[" + i + "] is ", (startCV + (cvInc * i)) + "", cv[i].number());
            log.debug("Contents of CV{} is '{}'", cv[i].number(), (cv[i].getValue()));
        }
        //Now for some tests

        testStr = "Test String";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test Short";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String Longer";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String tooo Long";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

        testStr = "Test String All Good";
        beforeStr = ((JTextField) var.getCommonRep()).getText();  // get the current contents
        resultStr = loadString(testStr, var, name);  // load a value, get the result
        checkResults(beforeStr, testStr, resultStr, cv, match, termByteStr, padByteStr, charSet);

    }

    // Local shared test methods
    /**
     * Common method to load new value and return result.
     *
     * @param testStr new value to load
     * @param var     variable to load it into
     * @param name    the variable name
     * @return The result after loading.
     */
    String loadString(String testStr, SplitTextVariableValue var, String name) {
        ((JTextField) var.getCommonRep()).setText(testStr);  // load a value
        var.exitField();
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, name));
        return ((JTextField) var.getCommonRep()).getText();  // get the result
    }

    /**
     * Common method to check the results of loading a value.
     * <br><br>
     * Performs a suite of checks on the resultant text value and associated CV
     * values.
     *
     * @param beforeStr   the value retrieved before loading
     * @param testStr     the value we attempted to load
     * @param resultStr   the result of loading the value
     * @param cv          the CV table
     * @param match       the {@code match} parameter (from the variable
     *                    definition)
     * @param termByteStr the {@code termByteStr} parameter (from the variable
     *                    definition)
     * @param padByteStr  the {@code padByteStr} parameter (from the variable
     *                    definition)
     * @param charSet     the {@code charSet} parameter (from the variable
     *                    definition)
     */
    public void checkResults(String beforeStr, String testStr, String resultStr, CvValue[] cv,
            String match, String termByteStr, String padByteStr, String charSet) {

        // check if match parameter applies and modify expectations accordingly
        if (match != null && !match.equals("") && !testStr.matches(match)) {
            log.debug("Match Failed with beforeStr='{}', testStr='{}', resultStr='{}'", beforeStr, testStr, resultStr);
            if (!testStr.equals(beforeStr)) {
                Assert.assertNotEquals("Match failed, contents of result string should not match", testStr, resultStr);
                return; // nothing else can be guaranteed
            } else {
                return; // nothing can be guaranteed
            }
        }

        // check if length exceeded and modify expectations accordingly
        if (testStr.length() > cv.length) {
            Assert.assertNotEquals("length of result string", testStr.length(), resultStr.length());
        } else {
            Assert.assertEquals("contents of result string", testStr, resultStr);
            Assert.assertEquals("length of result string", testStr.length(), resultStr.length());
        }
        // debugging information only
        for (int i = 0; i < cv.length; i++) {
            log.debug("Contents of CV{} is '{}'", cv[i].number(), (cv[i].getValue()));
        }
        // check that CVs were loaded correctly
        for (int i = 0; i < (resultStr.length()); i++) {
            log.debug("Character {} of result string'{}'is '{}' value '{}'", i, resultStr, resultStr.charAt(i), (int) resultStr.charAt(i));
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
    }

    // from here down is testing infrastructure
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

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitTextVariableValueTest.class
            .getName());

}
