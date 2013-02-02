// SplitVariableValueTest.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import javax.swing.*;

import java.util.*;
import jmri.progdebugger.*;
import junit.framework.*;

/**
 * SplitVariableValueTest.java
 *
 * @todo need a check of the MIXED state model for long address
 * @author	Bob Jacobsen Copyright 2001, 2002
 * @version $Revision$
 */


public class SplitVariableValueTest extends VariableValueTest {
    final int lowCV = 12;
    final int offset = 6;
    ProgDebugger p = new ProgDebugger();

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, String cvName,
                          boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                          int cvNum, String mask, int minVal, int maxVal,
                          Vector<CvValue> v, JLabel status, String item) {
        // make sure next CV exists
        CvValue cvNext = new CvValue(cvNum+offset, p);
        cvNext.setValue(0);
        v.setElementAt(cvNext, cvNum+offset);
        return new SplitVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                      cvNum, "XXXXVVVV", minVal, maxVal, v, status, item,
                                      cvNum+offset, 1, 0, "VVVVVVVV");
    }


    void setValue(VariableValue var, String val) {
        ((JTextField)var.getCommonRep()).setText(val);
        ((JTextField)var.getCommonRep()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((SplitVariableValue)var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JTextField)var.getCommonRep()).getText() );
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JLabel)var.getCommonRep()).getText() );
    }

    // end of abstract members

    // some of the premade tests don't quite make sense; override them here.

    public void testVariableValueCreate() {}// mask is ignored by splitAddre
    public void testVariableFromCV() {}     // low CV is upper part of address
    public void testVariableValueRead() {}	// due to multi-cv nature of SplitAddr
    // public void testVariableReadOnly() {}	// due to multi-cv nature of SplitAddr
    public void testVariableValueWrite() {} // due to multi-cv nature of SplitAddr
    public void testVariableCvWrite() {}    // due to multi-cv nature of SplitAddr
    public void testWriteSynch2() {}        // programmer synch is different

    public void testSplitAddressFromCV1() {
        Vector<CvValue> v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        cv1.setValue(2);
        cv2.setValue(3);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                                                        "VVVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0, "VVVVVVVV");

        ((JTextField)var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        Assert.assertEquals("set var full value", ""+(1029), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 5, cv1.getValue());
        Assert.assertEquals("set var high bits", 4, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", ""+(189*256+21), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    public void testSplitAddressFromCV2() {
        Vector<CvValue> v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        cv1.setValue(0xFF);
        cv2.setValue(0xFF);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                                                        "XXXXVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0, "VVVVVVVV");

        ((JTextField)var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        Assert.assertEquals("set var full value", ""+(1029), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 0xF5, cv1.getValue());
        Assert.assertEquals("set var high bits", 4*16, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", ""+(189*16+(21&0xF)), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    public void testSplitAddressFromCV3() {
        Vector<CvValue> v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        cv1.setValue(0xFF);
        cv2.setValue(0xFF);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                                                        "VVVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0, "XXVVVVXX");

        ((JTextField)var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        Assert.assertEquals("set var full value", ""+(1029), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 5, cv1.getValue());
        Assert.assertEquals("set var high bits", 0xC3+4*4, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", ""+((189&0x3C)/4*256+(21)), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }

    public void testSplitAddressFromCV4() {
        Vector<CvValue> v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        cv1.setValue(0xFF);
        cv2.setValue(0xFF);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false, lowCV,
                                                        "XVVVVVVX", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0, "XVVVVVXX");

        ((JTextField)var.getCommonRep()).setText("1029");  // to tell if changed
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        Assert.assertEquals("set var full value", ""+(1029), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set var low bits", 5*2+0x81, cv1.getValue());
        Assert.assertEquals("set var high bits", 0x83+0x40, cv2.getValue());

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        cv2.setValue(189);
        Assert.assertEquals("set cv low bits", 21, cv1.getValue());
        Assert.assertEquals("set cv full value", ""+((189&0x3C)/4*64+(10)), ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("set cv high bits", 189, cv2.getValue());
    }




    List<java.beans.PropertyChangeEvent> evtList = null;  // holds a list of ParameterChange events

    // check a long address read operation
    public void testSplitAddressRead1() {
        log.debug("testSplitAddressRead starts");

        Vector<CvValue> v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);

        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false,
                                                        lowCV, "XXVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0, "VVVVVVVV");
        // register a listener for parameter changes
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    evtList.add(e);
                    if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                        log.debug("Busy false seen in test");
                }
            };
        evtList = new ArrayList<java.beans.PropertyChangeEvent>();
        var.addPropertyChangeListener(listen);

        // set to specific value
        ((JTextField)var.getCommonRep()).setText("5");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        // read should get 123, 123 from CVs
        var.readAll();

        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( var.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getCommonRep()).getText()+" state="+var.getState());
        Assert.assertTrue("wait satisfied ", i<100);

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = evtList.get(k);
            if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                nBusyFalse++;
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("text value ", ""+((123&0x3f)+(123)*64), ((JTextField)var.getCommonRep()).getText() );  // 15227 = (1230x3f)*256+123
        Assert.assertEquals("Var state", AbstractValue.READ, var.getState() );
        Assert.assertEquals("CV 1 value ", 123, cv1.getValue());  // 123 with 128 bit set
        Assert.assertEquals("CV 2 value ", 123, cv2.getValue());
    }

    // check a long address write operation
    public void testSplitAddressWrite1() {

        Vector<CvValue> v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);

        SplitVariableValue var = new SplitVariableValue("name", "comment", "", false, false, false, false,
                                                        lowCV, "XXVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0, "VVVVVVVV");
        ((JTextField)var.getCommonRep()).setText("4797");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        var.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( var.isBusy() && i++ < 100  )  {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getCommonRep()).getText()
                                            +" state="+var.getState()
                                            +" last write: "+p.lastWrite());

        Assert.assertTrue("wait satisfied ", i<100);

        Assert.assertEquals("CV 1 value ", 61, cv1.getValue());
        Assert.assertEquals("CV 2 value ", 74, cv2.getValue());
        Assert.assertEquals("text ", "4797", ((JTextField)var.getCommonRep()).getText());
        Assert.assertEquals("Var state", AbstractValue.STORED, var.getState() );
        Assert.assertEquals("last write", 74,p.lastWrite());
        // how do you check separation of the two writes?  State model?
    }

    protected Vector<CvValue> createCvVector() {
        Vector<CvValue> v = new Vector<CvValue>(512);
        for (int i=0; i < 512; i++) v.addElement(null);
        return v;
    }

    // from here down is testing infrastructure

    public  SplitVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SplitVariableValueTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( SplitVariableValueTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger( SplitVariableValueTest.class.getName());

}
