package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JComboBox;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author	Bob Jacobsen Copyright 2005
 */
public class ComboCheckBoxTest {

    ProgDebugger p = new ProgDebugger();

    @Test
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

    @Test
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(ComboCheckBoxTest.class);

}
