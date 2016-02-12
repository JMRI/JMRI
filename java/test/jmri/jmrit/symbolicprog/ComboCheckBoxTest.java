// ComboCheckBoxTest.java
package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JComboBox;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class ComboCheckBoxTest extends TestCase {

    ProgDebugger p = new ProgDebugger();

    public void testToOriginal() {
        // create an enum variable pointed at CV 81 and connect
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        if (log.isDebugEnabled()) {
            log.debug("Enum variable created, loaded");
        }

        EnumVariableValue var = new EnumVariableValue("name", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        addTestItems(var);
        if (log.isDebugEnabled()) {
            log.debug("Enum variable created");
        }

        @SuppressWarnings("unchecked")
        JComboBox<String> combo = (JComboBox<String>) (var.getCommonRep());

        // create object under test
        ComboCheckBox b = new ComboCheckBox(combo, var);
        if (log.isDebugEnabled()) {
            log.debug("ComboCheckBox created");
        }

        // set it to "checked" & test state
        b.doClick();
        Assert.assertEquals("1 click checkbox state ", true, b.isSelected());
        Assert.assertEquals("1 click original state ", 1, combo.getSelectedIndex());

        // set it to unchecked & test state
        b.doClick();
        Assert.assertEquals("2 click checkbox state ", false, b.isSelected());
        Assert.assertEquals("2 click original state ", 0, combo.getSelectedIndex());

        // set it to "checked" again & test state
        b.doClick();
        Assert.assertEquals("3 click checkbox state ", true, b.isSelected());
        Assert.assertEquals("3 click original state ", 1, combo.getSelectedIndex());

    }

    public void testFromOriginal() {
        // create an enum variable pointed at CV 81 and connect
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        EnumVariableValue var = new EnumVariableValue("name", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        addTestItems(var);
        @SuppressWarnings("unchecked")
        JComboBox<String> combo = (JComboBox<String>) (var.getCommonRep());

        // create object under test
        ComboCheckBox b = new ComboCheckBox(combo, var);

        // set combo box to 1 and check state
        combo.setSelectedIndex(1);
        Assert.assertEquals("index 1 checkbox state ", true, b.isSelected());
        Assert.assertEquals("index 1 original state ", 1, combo.getSelectedIndex());

        // set it to unchecked & test state
        combo.setSelectedIndex(0);
        Assert.assertEquals("index 0 checkbox state ", false, b.isSelected());
        Assert.assertEquals("index 0 original state ", 0, combo.getSelectedIndex());

        // set it to "checked" again & test state
        combo.setSelectedIndex(1);
        Assert.assertEquals("2nd index 1 checkbox state ", true, b.isSelected());
        Assert.assertEquals("2nd index 1 original state ", 1, combo.getSelectedIndex());

    }

    protected void addTestItems(EnumVariableValue var) {
        var.nItems(2);
        var.addItem("Value0");
        var.addItem("Value1");
        var.lastItem();
    }

    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    // from here down is testing infrastructure
    public ComboCheckBoxTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ComboCheckBoxTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ComboCheckBoxTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(ComboCheckBoxTest.class.getName());

}
