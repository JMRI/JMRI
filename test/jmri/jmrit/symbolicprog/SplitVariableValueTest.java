// SplitVariableValueTest.java

package jmri.jmrit.symbolicprog;

import java.util.Vector;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import jmri.*;
import jmri.progdebugger.*;
import junit.framework.*;

/**
 * SplitVariableValueTest.java
 *
 * @todo need a check of the MIXED state model for long address
 * @author	Bob Jacobsen Copyright 2001, 2002
 * @version $Revision: 1.6 $
 */


public class SplitVariableValueTest extends VariableValueTest {
    final int lowCV = 12;
    final int offset = 6;
    ProgDebugger p = new ProgDebugger();

    // abstract members invoked by tests in parent VariableValueTest class
    VariableValue makeVar(String label, String comment, boolean readOnly,
                          int cvNum, String mask, int minVal, int maxVal,
                          Vector v, JLabel status, String item) {
        // make sure next CV exists
        CvValue cvNext = new CvValue(cvNum+offset, p);
        cvNext.setValue(0);
        v.setElementAt(cvNext, cvNum+offset);
        return new SplitVariableValue(label, comment, readOnly,
                                      cvNum, mask, minVal, maxVal, v, status, item,
                                      cvNum+offset, 1, 0);
    }


    void setValue(VariableValue var, String val) {
        ((JTextField)var.getValue()).setText(val);
        ((JTextField)var.getValue()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((SplitVariableValue)var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JTextField)var.getValue()).getText() );
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JLabel)var.getValue()).getText() );
    }

    // end of abstract members

    // some of the premade tests don't quite make sense; override them here.

    public void testVariableValueCreate() {}// mask is ignored by splitAddre
    public void testVariableFromCV() {}     // low CV is upper part of address
    public void testVariableValueRead() {}	// due to multi-cv nature of SplitAddr
    public void testVariableValueWrite() {} // due to multi-cv nature of SplitAddr
    public void testVariableCvWrite() {}    // due to multi-cv nature of SplitAddr
    public void testWriteSynch2() {}        // programmer synch is different
    // can we create long address , then manipulate the variable to change the CV?
    public void testSplitAddressCreate() {
        Vector v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        cv1.setValue(2);
        cv2.setValue(3);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);
        // create a variable pointed at CVs, check name
        SplitVariableValue var = new SplitVariableValue("label", "comment", false,
                                                        lowCV, "XXVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0);
        Assert.assertTrue(var.label() == "label");
        // pretend you've edited the value, check its in same object
        ((JTextField)var.getValue()).setText(""+(17+189*64));
        Assert.assertEquals("text value", ""+(17+189*64), ((JTextField)var.getValue()).getText() );
        // manually notify
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        // see if the CV was updated
        Assert.assertEquals("low bits", 17, cv1.getValue());
        Assert.assertEquals("high bits", 189, cv2.getValue());
    }

    // can we change both CVs and see the result in the Variable?
    public void testSplitAddressFromCV() {
        Vector v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        cv1.setValue(2);
        cv2.setValue(3);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);
        // create a variable pointed at CVs
        SplitVariableValue var = new SplitVariableValue("name", "comment", false, lowCV,
                                                        "XXVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0);
        ((JTextField)var.getValue()).setText("1029");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        // change the CV, expect to see a change in the variable value
        cv1.setValue(21);
        Assert.assertEquals("low bits", 21, cv1.getValue());
        cv2.setValue(189);
        Assert.assertEquals("full value", ""+(21+189*64), ((JTextField)var.getValue()).getText());
        Assert.assertEquals("high bits", 189, cv2.getValue());
    }

    List evtList = null;  // holds a list of ParameterChange events

    // check a long address read operation
    public void testSplitAddressRead() {
        log.debug("testSplitAddressRead starts");

        Vector v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);

        SplitVariableValue var = new SplitVariableValue("name", "comment", false,
                                                        lowCV, "XXVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0);
        // register a listener for parameter changes
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    evtList.add(e);
                    if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                        log.debug("Busy false seen in test");
                }
            };
        evtList = new ArrayList();
        var.addPropertyChangeListener(listen);

        // set to specific value
        ((JTextField)var.getValue()).setText("5");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        System.out.println("start read");
        var.read();
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( var.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getValue()).getText()+" state="+var.getState());
        if (i==0) log.warn("testSplitAddressRead saw an immediate return from isBusy");
        Assert.assertTrue("wait satisfied ", i<100);

        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = (java.beans.PropertyChangeEvent) evtList.get(k);
            // System.out.println("name: "+e.getPropertyName()+" new value: "+e.getNewValue());
            if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE))
                nBusyFalse++;
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);

        Assert.assertEquals("text value ", ""+((123&0x3f)+123*64), ((JTextField)var.getValue()).getText() );  // 15227 = (1230x3f)*256+123
        Assert.assertEquals("Var state", AbstractValue.READ, var.getState() );
        Assert.assertEquals("CV 1 value ", 123&0x3f, cv1.getValue());  // 123 with 128 bit set
        Assert.assertEquals("CV 2 value ", 123, cv2.getValue());
    }

    // check a long address write operation
    public void testSplitAddressWrite() {

        Vector v = createCvVector();
        CvValue cv1 = new CvValue(lowCV, p);
        CvValue cv2 = new CvValue(lowCV+offset, p);
        v.setElementAt(cv1, lowCV);
        v.setElementAt(cv2, lowCV+offset);

        SplitVariableValue var = new SplitVariableValue("name", "comment",
                                                        false, lowCV, "XXVVVVVV", 0, 255, v, null, null,
                                                        lowCV+offset, 1, 0);
        ((JTextField)var.getValue()).setText("4797");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));

        var.write();
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( var.isBusy() && i++ < 100  )  {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+((JTextField)var.getValue()).getText()
                                            +" state="+var.getState()
                                            +" last write: "+p.lastWrite());
        if (i==0) log.warn("testSplitAddressWrite saw an immediate return from isBusy");

        Assert.assertTrue("wait satisfied ", i<100);

        Assert.assertEquals("CV 1 value ", 61, cv1.getValue());
        Assert.assertEquals("CV 2 value ", 74, cv2.getValue());
        Assert.assertEquals("text ", "4797", ((JTextField)var.getValue()).getText());
        Assert.assertEquals("Var state", AbstractValue.STORED, var.getState() );
        Assert.assertEquals("last write", 74,p.lastWrite());
        // how do you check separation of the two writes?  State model?
    }

    protected Vector createCvVector() {
        Vector v = new Vector(512);
        for (int i=0; i < 512; i++) v.addElement(null);
        return v;
    }

    // from here down is testing infrastructure

    public  SplitVariableValueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = { SplitVariableValueTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( SplitVariableValueTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( SplitVariableValueTest.class.getName());

}
