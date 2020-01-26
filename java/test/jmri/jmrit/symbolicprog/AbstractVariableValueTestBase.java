package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Base for tests of classes inheriting from VariableValue abstract class
 *
 * @author	Bob Jacobsen, Copyright 2002
 */
public abstract class AbstractVariableValueTestBase {

    ProgDebugger p = new ProgDebugger();

    abstract VariableValue makeVar(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String item);

    abstract void setValue(VariableValue var, String value);

    abstract void checkValue(VariableValue var, String comment, String value);

    // we have separate fns for ReadOnly, as they may have different "value" object types
    abstract void setReadOnlyValue(VariableValue var, String value);

    abstract void checkReadOnlyValue(VariableValue var, String comment, String value);

    // start of base tests
    // check label, item from ctor
    @Test
    public void testVariableNaming() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");
        Assert.assertEquals("label", "label check", variable.label());
        Assert.assertEquals("item", "item check", variable.item());
    }

    // can we create one, then manipulate the variable to change the CV?
    @Test
    public void testVariableValueCreate() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assert.assertEquals("label", "label", variable.label());
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "5");

        // check value
        checkValue(variable, "value object contains ", "5");

        // see if the CV was updated
        Assert.assertEquals("cv value", 5 * 4 + 3, cv.getValue());
    }

    //  check create&manipulate for large values
    @Test
    public void testVariableValueCreateLargeValue() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "VVVVVVVVVVVVVVVV", 0, 60000, v, null, null);
        Assert.assertEquals("label", "label", variable.label());
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "40000");

        // check value
        checkValue(variable, "value object contains ", "40000");

        // see if the CV was updated
        Assert.assertEquals("cv value", 40000, cv.getValue());
    }

    //  check create&manipulate for large mask values
    @Test
    public void testVariableValueCreateLargeMaskValue() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(32768 + 3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXVVXXXXXXXXXXX", 0, 60000, v, null, null);
        Assert.assertEquals("label", "label", variable.label());
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");

        // check value
        checkValue(variable, "value object contains ", "2");

        // see if the CV was updated
        Assert.assertEquals("cv value", 2 * 8 * 256 + 32768 + 3, cv.getValue());
    }

    @Test
    public void testVariableValueCreateLargeMaskValue256() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(32768 + 3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXXXXXVXXXXXXXX", 0, 60000, v, null, null);
        Assert.assertEquals("label", "label", variable.label());
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "1");

        // check value
        checkValue(variable, "value object contains ", "1");

        // see if the CV was updated
        Assert.assertEquals("cv value", 256 + 32768 + 3, cv.getValue());
    }

    @Test
    public void testVariableValueCreateLargeMaskValue2up16() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "VXXXXXXXXXXXXXXX", 0, 60000, v, null, null);
        Assert.assertEquals("label", "label", variable.label());
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "1");

        // check value
        checkValue(variable, "value object contains ", "1");

        // see if the CV was updated
        Assert.assertEquals("cv value", 256 * 128 + 3, cv.getValue());
    }

    // can we change the CV and see the result in the Variable?
    @Test
    public void testVariableFromCV() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assert.assertTrue("getValue not null ", variable.getCommonRep() != null);
        setValue(variable, "5");
        checkValue(variable, "variable value", "5");

        // change the CV, expect to see a change in the variable value
        cv.setValue(7 * 4 + 1);
        checkValue(variable, "value after CV set", "7");
        Assert.assertEquals("cv after CV set ", 7 * 4 + 1, cv.getValue());
    }

    // Do we get the right return from a readOnly == true DecVariable?
    @Test
    public void testVariableReadOnly() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5
        VariableValue variable = makeVar("label", "comment", "", true, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assert.assertTrue(variable.getCommonRep() != null);
        setReadOnlyValue(variable, "5");
        checkReadOnlyValue(variable, "value", "5");
    }

    // check a read operation
    @Test
    public void testVariableValueRead() {
        log.debug("testVariableValueRead base starts");

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        variable.readAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !variable.isBusy();}, "variable.isBusy");
        
        checkValue(variable, "text var value ", "14");
        Assert.assertEquals("var state ", AbstractValue.READ, variable.getState());
        Assert.assertEquals("cv value", 123, cv.getValue());
        Assert.assertEquals("CV state ", AbstractValue.READ, cv.getState());
    }

    // check a write operation to the variable
    @Test
    public void testVariableValueWrite() {
        log.debug("testVariableValueWrite base starts");

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        cv.setValue(128 + 1);

        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        variable.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !variable.isBusy();}, "variable.isBusy");
        
        checkValue(variable, "value ", "5");
        Assert.assertEquals("var state ", AbstractValue.STORED, variable.getState());
        Assert.assertEquals("cv state ", AbstractValue.STORED, cv.getState());
        Assert.assertEquals("last program write ", 5 * 4 + 128 + 1, p.lastWrite()); // include checking original bits
    }

    // check synch during a write operation to the CV
    @Test
    public void testVariableCvWrite() {
        if (log.isDebugEnabled()) {
            log.debug("start testVariableCvWrite test");
        }

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        JLabel statusLabel = new JLabel("nothing");
        cv.write(statusLabel);  // JLabel is for reporting status, ignored here
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !cv.isBusy();}, "cv.isBusy");
        
        checkValue(variable, "value ", "5");
        Assert.assertEquals("variable state ", AbstractValue.STORED, variable.getState());
        Assert.assertEquals("cv state ", AbstractValue.STORED, cv.getState());
        Assert.assertEquals("value written ", 5 * 4 + 3, p.lastWrite()); // includes initial value bits
        Assert.assertEquals("status label ", "OK", statusLabel.getText());
        if (log.isDebugEnabled()) {
            log.debug("end testVariableCvWrite test");
        }
    }

    // check the state diagram
    @Test
    public void testVariableValueStates() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assert.assertEquals("initial state", VariableValue.FROMFILE, variable.getState());
        cv.setState(CvValue.UNKNOWN);
        Assert.assertEquals("after CV set unknown", VariableValue.UNKNOWN, variable.getState());
        setValue(variable, "5");
        Assert.assertEquals("state after setValue", VariableValue.EDITED, variable.getState());
    }

    // check the state <-> color connection for value
    @Test
    public void testVariableValueStateColor() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assert.assertEquals("FROM_FILE color", VariableValue.COLOR_FROMFILE, variable.getCommonRep().getBackground());

        cv.setState(CvValue.UNKNOWN);
        Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, variable.getCommonRep().getBackground());
    }

    // check the state <-> color connection for rep when var changes
    @Test
    public void testVariableRepStateColor() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        // get a representation
        JComponent rep = (JComponent) variable.getNewRep("");

        Assert.assertEquals("FROMFILE color", VariableValue.COLOR_FROMFILE, variable.getCommonRep().getBackground());
        Assert.assertEquals("FROMFILE color", VariableValue.COLOR_FROMFILE, rep.getBackground());

        cv.setState(CvValue.UNKNOWN);

        Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, variable.getCommonRep().getBackground());
        Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, rep.getBackground());

        setValue(variable, "5");

        Assert.assertEquals("EDITED color", VariableValue.COLOR_EDITED, variable.getCommonRep().getBackground());
        Assert.assertEquals("EDITED color", VariableValue.COLOR_EDITED, rep.getBackground());
    }

    // check the state <-> color connection for var when rep changes
    @SuppressWarnings("unchecked")
    @Test
    public void testVariableVarChangeColorRep() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        // get a representation
        JComponent rep = (JComponent) variable.getNewRep("");

        Assert.assertEquals("FROMFILE color", VariableValue.COLOR_FROMFILE, variable.getCommonRep().getBackground());
        Assert.assertEquals("FROMFILE color", VariableValue.COLOR_FROMFILE, rep.getBackground());

        cv.setState(CvValue.UNKNOWN);
        Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, variable.getCommonRep().getBackground());
        Assert.assertEquals("UNKNOWN color", VariableValue.COLOR_UNKNOWN, rep.getBackground());

        try {   // might be either of two reps?
            ((JComboBox<String>) rep).setSelectedItem("9");
        } catch (java.lang.ClassCastException e) {
            ((JTextField) rep).setText("9");
            ((JTextField) rep).postActionEvent();
            Assert.assertEquals("EDITED color", VariableValue.COLOR_EDITED, variable.getCommonRep().getBackground());
            Assert.assertEquals("EDITED color", VariableValue.COLOR_EDITED, rep.getBackground());
        }
    }

    // check synchonization of value, representations
    @Test
    public void testVariableSynch() {

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        // now get value, check
        checkValue(variable, "first value check ", "5");
        Component val1 = variable.getCommonRep();
        // now get rep, check
        JTextField rep1 = (JTextField) variable.getNewRep("");
        Assert.assertEquals("initial rep ", "5", rep1.getText());

        // update via value
        setValue(variable, "2");

        // check again with existing reference
        Assert.assertEquals("same value object ", val1, variable.getCommonRep());
        Assert.assertEquals("1 saved rep ", "2", rep1.getText());
        // pick up new references and check
        checkValue(variable, "1 new value ", "2");
        Assert.assertEquals("1 new rep ", "2", ((JTextField) variable.getNewRep("")).getText());

        // update via rep
        rep1.setText("9");
        rep1.postActionEvent();

        // check again with existing references
        Assert.assertEquals("2 saved value ", "9", ((JTextField) val1).getText());
        Assert.assertEquals("2 saved rep ", "9", rep1.getText());
        // pick up new references and check
        checkValue(variable, "2 new value ", "9");
        Assert.assertEquals("2 new rep ", "9", ((JTextField) variable.getNewRep("")).getText());
    }

    // check synchronization of two vars during a write
    @Test
    public void testWriteSynch2() {
        if (log.isDebugEnabled()) {
            log.debug("start testWriteSynch2 test");
        }

        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue var1 = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        VariableValue var2 = makeVar("alternate", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(var1, "5");

        var1.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()->{return !var1.isBusy();}, "var1.isBusy");

        checkValue(var1, "var 1 value", "5");
        checkValue(var2, "var 2 value", "5");
        Assert.assertEquals("1st variable state ", AbstractValue.STORED, var1.getState());
        Assert.assertEquals("2nd variable state ", AbstractValue.STORED, var2.getState());
        Assert.assertEquals("value written to programmer ", 5 * 4 + 3, p.lastWrite()); // includes initial value bits
        if (log.isDebugEnabled()) {
            log.debug("end testWriteSynch2 test");
        }
    }

    // end of common tests
    // this next is just preserved here; note not being invoked.
    // test that you're not using too much space when you call for a value
    @Test
    @Ignore("Disabled in JUnit 3")
    public void testSpaceUsage() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5
        DecVariableValue var = new DecVariableValue("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        Assert.assertNotNull("exists", var);
        System.out.println("free, total memory at start = " + Runtime.getRuntime().freeMemory()
                + " " + Runtime.getRuntime().totalMemory());
        Runtime.getRuntime().gc();
        long usedStart = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("free, total memory after gc = " + Runtime.getRuntime().freeMemory()
                + " " + Runtime.getRuntime().totalMemory());
        JTextField master = new JTextField(3);
        javax.swing.text.Document doc = master.getDocument();
        // loop to repeat getting value
        for (int i = 0; i < 10; i++) {
            JTextField j = new JTextField(doc, "", 3);
            //JTextField temp = ((JTextField)var.getValue());
            //Assert.assertTrue(temp != null);
            Assert.assertNotNull("exists", j);
        }
        long freeAfter = Runtime.getRuntime().freeMemory();
        Assert.assertNotNull("exists", freeAfter);
        System.out.println("free, total memory after loop = " + Runtime.getRuntime().freeMemory()
                + " " + Runtime.getRuntime().totalMemory());
        long usedAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Runtime.getRuntime().gc();
        long usedAfterGC = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("free, total memory after gc = " + Runtime.getRuntime().freeMemory()
                + " " + Runtime.getRuntime().totalMemory());
        System.out.println("used & kept = " + (usedAfterGC - usedStart) + " used before reclaim = " + (usedAfter - usedStart));
    }

    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    public void setUp() {
        JUnitUtil.setUp();
    }

    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static  org.slf4j.Logger log =  org.slf4j.LoggerFactory.getLogger(AbstractVariableValueTestBase.class);

}
