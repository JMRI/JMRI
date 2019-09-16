package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test LongAddrVariableValue class.
 *
 * @todo need a check of the MIXED state model for long address
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LongAddrVariableValueTest extends AbstractVariableValueTestBase {

    ProgDebugger p = new ProgDebugger();

    // abstract members invoked by tests in parent AbstractVariableValueTestBase class
    @Override
    VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item) {
        // make sure next CV exists
        CvValue cvNext = new CvValue("18", p);
        cvNext.setValue(0);
        v.put(cvNum + 1, cvNext);
        return new LongAddrVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item, cvNext);
    }

    @Override
    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    @Override
    void setReadOnlyValue(VariableValue var, String val) {
        ((LongAddrVariableValue) var).setValue(Integer.valueOf(val).intValue());
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
    }// mask is ignored by LongAddr

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

    @Override
    @Test
    public void testVariableFromCV() {
    }     // low CV is upper part of address

    @Override
    @Test
    public void testVariableValueRead() {
    }	// due to multi-cv nature of LongAddr

    @Override
    @Test
    public void testVariableValueWrite() {
    } // due to multi-cv nature of LongAddr

    @Override
    @Test
    public void testVariableCvWrite() {
    }    // due to multi-cv nature of LongAddr

    @Override
    @Test
    public void testWriteSynch2() {
    }        // programmer synch is different
    // can we create long address , then manipulate the variable to change the CV?

    @Test
    public void testLongAddressCreate() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv17 = new CvValue("17", p);
        CvValue cv18 = new CvValue("18", p);
        cv17.setValue(2);
        cv18.setValue(3);
        v.put("17", cv17);
        v.put("18", cv18);
        // create a variable pointed at CV 17&18, check name
        LongAddrVariableValue var = new LongAddrVariableValue("label", "comment", "", false, false, false, false, "17", "VVVVVVVV", 0, 255, v, null, null, cv18);
        Assert.assertTrue(var.label() == "label");
        // pretend you've edited the value, check its in same object
        ((JTextField) var.getCommonRep()).setText("4797");
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("4797"));
        // manually notify
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        // see if the CV was updated
        Assert.assertTrue(cv17.getValue() == 210);
        Assert.assertTrue(cv18.getValue() == 189);
    }

    // can we change both CVs and see the result in the Variable?
    @Test
    public void testLongAddressFromCV() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv17 = new CvValue("17", p);
        CvValue cv18 = new CvValue("18", p);
        cv17.setValue(2);
        cv18.setValue(3);
        v.put("17", cv17);
        v.put("18", cv18);
        // create a variable pointed at CV 17 & 18
        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", "", false, false, false, false, "17", "VVVVVVVV", 0, 255, v, null, null, cv18);
        ((JTextField) var.getCommonRep()).setText("1029");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        // change the CV, expect to see a change in the variable value
        cv17.setValue(210);
        Assert.assertTrue(cv17.getValue() == 210);
        cv18.setValue(189);
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("4797"));
        Assert.assertTrue(cv18.getValue() == 189);
    }

    List<java.beans.PropertyChangeEvent> evtList = null;  // holds a list of ParameterChange events

    // check a long address read operation
    @Test
    public void testLongAddressRead() {
        log.debug("testLongAddressRead starts");
        // initialize the system

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv17 = new CvValue("17", p);
        CvValue cv18 = new CvValue("18", p);
        v.put("17", cv17);
        v.put("18", cv18);

        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", "", false, false, false, false, "17", "XXVVVVXX", 0, 255, v, null, null, cv18);
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
        evtList = new ArrayList<java.beans.PropertyChangeEvent>();
        var.addPropertyChangeListener(listen);

        // set to specific value
        ((JTextField) var.getCommonRep()).setText("5");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        var.readAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !var.isBusy();}, "var.isBusy");

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = evtList.get(k);
            if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                nBusyFalse++;
            }
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("text value ", "15227", ((JTextField) var.getCommonRep()).getText());  // 15227 = (1230x3f)*256+123
        Assert.assertEquals("Var state", AbstractValue.READ, var.getState());
        Assert.assertEquals("CV 17 value ", 251, cv17.getValue());  // 123 with 128 bit set
        Assert.assertEquals("CV 18 value ", 123, cv18.getValue());
    }

    // check a long address write operation
    @Test
    public void testLongAddressWrite() {
        // initialize the system

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv17 = new CvValue("17", p);
        CvValue cv18 = new CvValue("18", p);
        v.put("17", cv17);
        v.put("18", cv18);

        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", "", false, false, false, false, "17", "XXVVVVXX", 0, 255, v, null, null, cv18);
        ((JTextField) var.getCommonRep()).setText("4797");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        var.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !var.isBusy();}, "var.isBusy");

        Assert.assertEquals("CV 17 value ", 210, cv17.getValue());
        Assert.assertEquals("CV 18 value ", 189, cv18.getValue());
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("4797"));
        Assert.assertEquals("Var state", AbstractValue.STORED, var.getState());
        Assert.assertTrue(p.lastWrite() == 189);
        // how do you check separation of the two writes?  State model?
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

    private final static Logger log = LoggerFactory.getLogger(LongAddrVariableValueTest.class);

}
