package jmri.jmrit.symbolicprog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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

/**
 * Tests for the {@link SplitDateTimeVariableValue} class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2015
 * @author Dave Heap Copyright 2019
 */
public class SplitDateTimeVariableValueTest extends AbstractVariableValueTestBase {

    // Local tests version of makeVar with extra parameters.
    SplitDateTimeVariableValue makeVar(String label, String comment, String cvName,
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
        return new SplitDateTimeVariableValue(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item, highCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
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
        String base = "2000-01-01T00:00:00";  // The S9.3.2 RailCom epoch
        String factor = "1";
        String unit = "Seconds";
        String display = "default";

        return makeVar(label, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item, highCV, pFactor, pOffset, uppermask, base, factor, unit, display);
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
    public void testVariableValueStates() {
    }

    @Override
    @Test
    public void testVariableRepStateColor() {
    }

    @Override
    @Test
    public void testVariableVarChangeColorRep() {
    }

    @Override
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
        String name = "Date Field";
        String comment = "";
        String cvName = "23:4";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "23:4";
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
    public void testRailComDateDefault() {
        String name = "Date Field";
        String comment = "";
        String cvName = "23:4";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "23:4";
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
        String base = "2000-01-01T00:00:00";  // The S9.3.2 RailCom epoch
        String factor = "1";
        String unit = "Seconds";
        String display = "default";
        SplitDateTimeVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, base, factor, unit, display);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue cv23 = v.get("23");
        CvValue cv24 = v.get("24");
        CvValue cv25 = v.get("25");
        CvValue cv26 = v.get("26");

        // change the CVs, expect to see a change in the variable value
        cv23.setValue(83);
        cv24.setValue(252);
        cv25.setValue(149);
        cv26.setValue(26);
        String temp = LocalDateTime.parse("2014-02-18T11:11:15").format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        Assert.assertEquals("set var default value", temp, ((JTextField) var.getCommonRep()).getText());
    }

    @Test
    public void testRailComTimeOnly() {
        String name = "Date Field";
        String comment = "";
        String cvName = "23:4";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "23:4";
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
        String base = "2000-01-01T00:00:00";  // The S9.3.2 RailCom epoch
        String factor = "1";
        String unit = "Seconds";
        String display = "timeOnly";
        SplitDateTimeVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, base, factor, unit, display);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue cv23 = v.get("23");
        CvValue cv24 = v.get("24");
        CvValue cv25 = v.get("25");
        CvValue cv26 = v.get("26");

        // change the CVs, expect to see a change in the variable value
        cv23.setValue(83);
        cv24.setValue(252);
        cv25.setValue(149);
        cv26.setValue(26);
        String temp = LocalDateTime.parse("2014-02-18T11:11:15").format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
        Assert.assertEquals("set var timeOnly value", temp, ((JTextField) var.getCommonRep()).getText());
    }

    @Test
    public void testRailComDateOnly() {
        String name = "Date Field";
        String comment = "";
        String cvName = "23:4";
        boolean readOnly = false;
        boolean infoOnly = false;
        boolean writeOnly = false;
        boolean opsOnly = false;
        String cvNum = "23:4";
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
        String base = "2000-01-01T00:00:00";  // The S9.3.2 RailCom epoch
        String factor = "1";
        String unit = "Seconds";
        String display = "dateOnly";
        SplitDateTimeVariableValue var = makeVar(name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cvNum, mask, minVal, maxVal,
                v, status, stdname,
                highCV, pFactor, pOffset, uppermask, base, factor, unit, display);
        Assert.assertNotNull("makeVar returned null", var);

        CvValue cv23 = v.get("23");
        CvValue cv24 = v.get("24");
        CvValue cv25 = v.get("25");
        CvValue cv26 = v.get("26");

        // change the CVs, expect to see a change in the variable value
        cv23.setValue(83);
        cv24.setValue(252);
        cv25.setValue(149);
        cv26.setValue(26);
        String temp = LocalDate.parse("2014-02-18").format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        Assert.assertEquals("set var dateOnly value", temp, ((JTextField) var.getCommonRep()).getText());
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
}
