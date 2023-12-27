package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Base for tests of classes inheriting from VariableValue abstract class
 *
 * @author Bob Jacobsen, Copyright 2002
 */
public abstract class AbstractVariableValueTestBase {

    ProgDebugger p;

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

    // Test the ValueState enum
    @Test
    public void testValueStateEnum() {
        Assert.assertEquals(Color.red.brighter(), AbstractValue.ValueState.UNKNOWN.getColor());
        Assert.assertEquals(Color.orange, AbstractValue.ValueState.EDITED.getColor());
        Assert.assertNull(AbstractValue.ValueState.READ.getColor());
        Assert.assertNull(AbstractValue.ValueState.STORED.getColor());
        Assert.assertEquals(Color.yellow, AbstractValue.ValueState.FROMFILE.getColor());
        Assert.assertNull(AbstractValue.ValueState.SAME.getColor());
        Assert.assertEquals(Color.red.brighter(), AbstractValue.ValueState.DIFFERENT.getColor());

        Assert.assertEquals("Unknown", AbstractValue.ValueState.UNKNOWN.getName());
        Assert.assertEquals("Edited", AbstractValue.ValueState.EDITED.getName());
        Assert.assertEquals("Read", AbstractValue.ValueState.READ.getName());
        Assert.assertEquals("Stored", AbstractValue.ValueState.STORED.getName());
        Assert.assertEquals("FromFile", AbstractValue.ValueState.FROMFILE.getName());
        Assert.assertEquals("Same", AbstractValue.ValueState.SAME.getName());
        Assert.assertEquals("Different", AbstractValue.ValueState.DIFFERENT.getName());
    }

    // check label, item from ctor
    @Test
    public void testVariableNaming() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label check", "comment", "", false, false, false, false, "81", "XXVVVVVV", 0, 255, v, null, "item check");
        Assertions.assertEquals("label check", variable.label(), "label");
        Assertions.assertEquals("item check", variable.item(), "item");
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
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "5");

        // check value
        checkValue(variable, "value object contains ", "5");

        // see if the CV was updated
        Assertions.assertEquals(5 * 4 + 3, cv.getValue(), "cv value");
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
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "40000");

        // check value
        checkValue(variable, "value object contains ", "40000");

        // see if the CV was updated
        Assertions.assertEquals(40000, cv.getValue(), "cv value");
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
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "2");

        // check value
        checkValue(variable, "value object contains ", "2");

        // see if the CV was updated
        Assertions.assertEquals(2 * 8 * 256 + 32768 + 3, cv.getValue(), "cv value");
    }

    @Test
    public void testVariableValueCreateLargeMaskValue256() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(32768 + 3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXXXXXXVXXXXXXXX", 0, 60000, v, null, null);
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "1");

        // check value
        checkValue(variable, "value object contains ", "1");

        // see if the CV was updated
        Assertions.assertEquals(256 + 32768 + 3, cv.getValue(), "cv value");
    }

    @Test
    public void testVariableValueCreateLargeMaskValue2up16() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "VXXXXXXXXXXXXXXX", 0, 60000, v, null, null);
        Assertions.assertEquals("label", variable.label(), "label");
        checkValue(variable, "value object initially contains ", "0");

        // pretend you've edited the value & manually notify
        setValue(variable, "1");

        // check value
        checkValue(variable, "value object contains ", "1");

        // see if the CV was updated
        Assertions.assertEquals(256 * 128 + 3, cv.getValue(), "cv value");
    }

    @Test
    public void testVariableValueTwinMask() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        // create a variable pointed at CV 81, check name
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "VXXXXXXX XXXXVVVV", 0, 64, v, null, null);

        Assertions.assertEquals("VXXXXXXX", variable.getMask(), "mask at start");
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
        Assertions.assertNotNull(variable.getCommonRep(), "getValue not null ");
        setValue(variable, "5");
        checkValue(variable, "variable value", "5");

        // change the CV, expect to see a change in the variable value
        cv.setValue(7 * 4 + 1);
        checkValue(variable, "value after CV set", "7");
        Assertions.assertEquals(7 * 4 + 1, cv.getValue(), "cv after CV set ");
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
        Assertions.assertNotNull(variable.getCommonRep());
        setReadOnlyValue(variable, "5");
        checkReadOnlyValue(variable, "value", "5");
    }

    // check a read operation
    @Test
    public void testVariableValueRead() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false,
                false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        variable.readAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()-> !variable.isBusy(), "variable.isBusy");

        checkValue(variable, "text var value ", "14");
        Assertions.assertEquals(AbstractValue.ValueState.READ, variable.getState(), "var state ");
        Assertions.assertEquals(123, cv.getValue(), "cv value");
        Assertions.assertEquals(AbstractValue.ValueState.READ, cv.getState(), "CV state ");
    }

    // check a write operation to the variable
    @Test
    public void testVariableValueWrite() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        v.put("81", cv);
        cv.setValue(128 + 1);

        // create a variable pointed at CV 81, loaded as 5, manually notified
        VariableValue variable = makeVar("label", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        setValue(variable, "5");

        variable.writeAll();
        // wait for reply (normally, done by callback; will check that later)
        JUnitUtil.waitFor(()-> !variable.isBusy(), "variable.isBusy");

        checkValue(variable, "value ", "5");
        Assertions.assertEquals(AbstractValue.ValueState.STORED, variable.getState(), "var state ");
        Assertions.assertEquals(AbstractValue.ValueState.STORED, cv.getState(), "cv state ");
        Assertions.assertEquals(5 * 4 + 128 + 1, p.lastWrite(), "last program write "); // include checking original bits
    }

    // check synch during a write operation to the CV
    @Test
    public void testVariableCvWrite() {
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
        JUnitUtil.waitFor(()-> !cv.isBusy(), "cv.isBusy");

        checkValue(variable, "value ", "5");
        Assertions.assertEquals(AbstractValue.ValueState.STORED, variable.getState(), "variable state ");
        Assertions.assertEquals(AbstractValue.ValueState.STORED, cv.getState(), "cv state ");
        Assertions.assertEquals(5 * 4 + 3, p.lastWrite(), "value written "); // includes initial value bits
        Assertions.assertEquals("OK", statusLabel.getText(), "status label ");
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
        Assertions.assertEquals(VariableValue.ValueState.FROMFILE, variable.getState(), "initial state");
        cv.setState(AbstractValue.ValueState.UNKNOWN);
        Assertions.assertEquals(VariableValue.ValueState.UNKNOWN, variable.getState(), "after CV set unknown");
        setValue(variable, "5");
        Assertions.assertEquals(VariableValue.ValueState.EDITED, variable.getState(), "state after setValue");
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
        Assertions.assertEquals(VariableValue.ValueState.FROMFILE.getColor(), variable.getCommonRep().getBackground(), "FROM_FILE color");

        cv.setState(AbstractValue.ValueState.UNKNOWN);
        Assertions.assertEquals(VariableValue.ValueState.UNKNOWN.getColor(), variable.getCommonRep().getBackground(), "UNKNOWN color");
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

        Assertions.assertEquals(VariableValue.ValueState.FROMFILE.getColor(), variable.getCommonRep().getBackground(), "FROMFILE color");
        Assertions.assertEquals(VariableValue.ValueState.FROMFILE.getColor(), rep.getBackground(), "FROMFILE color");

        cv.setState(AbstractValue.ValueState.UNKNOWN);

        Assertions.assertEquals(VariableValue.ValueState.UNKNOWN.getColor(), variable.getCommonRep().getBackground(), "UNKNOWN color");
        Assertions.assertEquals(VariableValue.ValueState.UNKNOWN.getColor(), rep.getBackground(), "UNKNOWN color");

        setValue(variable, "5");

        Assertions.assertEquals(VariableValue.ValueState.EDITED.getColor(), variable.getCommonRep().getBackground(), "EDITED color");
        Assertions.assertEquals(VariableValue.ValueState.EDITED.getColor(), rep.getBackground(), "EDITED color");
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

        Assertions.assertEquals(VariableValue.ValueState.FROMFILE.getColor(), variable.getCommonRep().getBackground(), "FROMFILE color");
        Assertions.assertEquals(VariableValue.ValueState.FROMFILE.getColor(), rep.getBackground(), "FROMFILE color");

        cv.setState(AbstractValue.ValueState.UNKNOWN);
        Assertions.assertEquals(VariableValue.ValueState.UNKNOWN.getColor(), variable.getCommonRep().getBackground(), "UNKNOWN color");
        Assertions.assertEquals(VariableValue.ValueState.UNKNOWN.getColor(), rep.getBackground(), "UNKNOWN color");

        try {   // might be either of two reps?
            ((JComboBox<String>) rep).setSelectedItem("9");
        } catch (java.lang.ClassCastException e) {
            ((JTextField) rep).setText("9");
            ((JTextField) rep).postActionEvent();
            Assertions.assertEquals(VariableValue.ValueState.EDITED.getColor(), variable.getCommonRep().getBackground(), "EDITED color");
            Assertions.assertEquals(VariableValue.ValueState.EDITED.getColor(), rep.getBackground(), "EDITED color");
        }
    }

    // check synchronization of value, representations
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
        Assertions.assertEquals("5", rep1.getText(), "initial rep ");

        // update via value
        setValue(variable, "2");

        // check again with existing reference
        Assertions.assertEquals(val1, variable.getCommonRep(), "same value object ");
        Assertions.assertEquals("2", rep1.getText(), "1 saved rep ");
        // pick up new references and check
        checkValue(variable, "1 new value ", "2");
        Assertions.assertEquals("2", ((JTextField) variable.getNewRep("")).getText(), "1 new rep ");

        // update via rep
        rep1.setText("9");
        rep1.postActionEvent();

        // check again with existing references
        Assertions.assertEquals("9", ((JTextField) val1).getText(), "2 saved value ");
        Assertions.assertEquals("9", rep1.getText(), "2 saved rep ");
        // pick up new references and check
        checkValue(variable, "2 new value ", "9");
        Assertions.assertEquals("9", ((JTextField) variable.getNewRep("")).getText(), "2 new rep ");
    }

    // check synchronization of two vars during a write
    @Test
    public void testWriteSynch2() {

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
        Assertions.assertEquals(AbstractValue.ValueState.STORED, var1.getState(), "1st variable state ");
        Assertions.assertEquals(AbstractValue.ValueState.STORED, var2.getState(), "2nd variable state ");
        Assertions.assertEquals(5 * 4 + 3, p.lastWrite(), "value written to programmer "); // includes initial value bits
    }

    // end of common tests
    // this next is just preserved here; note not being invoked.
    // test that you're not using too much space when you call for a value
    @Test
    @Disabled("Disabled in JUnit 3")
    public void testSpaceUsage() {
        /* // Avoid being picked up by code linting tools
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
        */
    }

    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<>();
        return m;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        p = new ProgDebugger();
    }

    @AfterEach
    public void tearDown() {
        if ( p != null ) {
            p.dispose();
            p = null;
        }
        JUnitUtil.tearDown();
    }

    // private final static  org.slf4j.Logger log =  org.slf4j.LoggerFactory.getLogger(AbstractVariableValueTestBase.class);

}
