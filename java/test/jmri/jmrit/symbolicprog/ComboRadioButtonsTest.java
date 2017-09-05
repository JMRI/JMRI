package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JComboBox;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * ComboRadioButtonsTest.java
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class ComboRadioButtonsTest extends TestCase {

    ProgDebugger p = new ProgDebugger();

    public void testAppearance() {
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
        ComboRadioButtons b = new ComboRadioButtons(combo, var);

        // check length
        Assert.assertEquals("expected item count ", 3, b.v.size());
    }

    public void testToOriginal() {
        HashMap<String, CvValue> v = createCvMap();
        CvValue cv = new CvValue("81", p);
        cv.setValue(3);
        v.put("81", cv);
        EnumVariableValue var = new EnumVariableValue("name", "comment", "", false, false, false, false, "81", "XXVVVVXX", 0, 255, v, null, null);
        addTestItems(var);
        @SuppressWarnings("unchecked")
        JComboBox<String> combo = (JComboBox<String>) (var.getCommonRep());

        // create object under test
        ComboRadioButtons b = new ComboRadioButtons(combo, var);

        // click middle button & test state
        ((b.v.elementAt(1))).doClick();
        Assert.assertEquals("1 click button on ", true, ((b.v.elementAt(1))).isSelected());
        Assert.assertEquals("1 click button 0 off ", false, ((b.v.elementAt(0))).isSelected());
        Assert.assertEquals("1 click button 2 off ", false, ((b.v.elementAt(2))).isSelected());
        Assert.assertEquals("1 click original state ", 1, combo.getSelectedIndex());

        // click top button & test state
        ((b.v.elementAt(0))).doClick();
        Assert.assertEquals("0 click button on ", true, ((b.v.elementAt(0))).isSelected());
        Assert.assertEquals("0 click button 1 off ", false, ((b.v.elementAt(1))).isSelected());
        Assert.assertEquals("0 click button 2 off ", false, ((b.v.elementAt(2))).isSelected());
        Assert.assertEquals("0 click original state ", 0, combo.getSelectedIndex());

        // click bottom button & test state
        ((b.v.elementAt(2))).doClick();
        Assert.assertEquals("2 click button on ", true, ((b.v.elementAt(2))).isSelected());
        Assert.assertEquals("2 click button 0 off ", false, ((b.v.elementAt(0))).isSelected());
        Assert.assertEquals("2 click button 1 off ", false, ((b.v.elementAt(1))).isSelected());
        Assert.assertEquals("2 click original state ", 2, combo.getSelectedIndex());

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
        ComboRadioButtons b = new ComboRadioButtons(combo, var);

        // set combo box to 1 and check state
        combo.setSelectedIndex(1);
        Assert.assertEquals("1 click button on ", true, ((b.v.elementAt(1))).isSelected());
        Assert.assertEquals("1 click button 0 off ", false, ((b.v.elementAt(0))).isSelected());
        Assert.assertEquals("1 click button 2 off ", false, ((b.v.elementAt(2))).isSelected());
        Assert.assertEquals("1 click original state ", 1, combo.getSelectedIndex());

        // set combo box to 2 and check state
        combo.setSelectedIndex(2);
        Assert.assertEquals("2 click button on ", true, ((b.v.elementAt(2))).isSelected());
        Assert.assertEquals("2 click button 0 off ", false, ((b.v.elementAt(0))).isSelected());
        Assert.assertEquals("2 click button 1 off ", false, ((b.v.elementAt(1))).isSelected());
        Assert.assertEquals("2 click original state ", 2, combo.getSelectedIndex());

        // set combo box to 0 and check state
        combo.setSelectedIndex(0);
        Assert.assertEquals("0 click button on ", true, ((b.v.elementAt(0))).isSelected());
        Assert.assertEquals("0 click button 1 off ", false, ((b.v.elementAt(1))).isSelected());
        Assert.assertEquals("0 click button 2 off ", false, ((b.v.elementAt(2))).isSelected());
        Assert.assertEquals("0 click original state ", 0, combo.getSelectedIndex());

    }

    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    protected void addTestItems(EnumVariableValue var) {
        var.nItems(3);
        var.addItem("Value0");
        var.addItem("Value1");
        var.addItem("Value2");
        var.lastItem();
    }

    // from here down is testing infrastructure
    public ComboRadioButtonsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ComboRadioButtonsTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ComboRadioButtonsTest.class);
        return suite;
    }

    // private final static Logger log = LoggerFactory.getLogger(ComboRadioButtonsTest.class);
}
