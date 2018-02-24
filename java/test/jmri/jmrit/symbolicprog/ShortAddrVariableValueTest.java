package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShortAddrVariableValueTest {

    // After AbstractVariableValueTest, is converted to JUnit4, this
    // test class should be re-written as a derived class.
    protected HashMap<String, CvValue> createCvMap() {
        HashMap<String, CvValue> m = new HashMap<String, CvValue>();
        return m;
    }

    @Test
    public void testCTor() {
        HashMap<String, CvValue> v = createCvMap();
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        CvValue cv1 = new CvValue("1", p);
        cv1.setValue(42);
        v.put("1", cv1);
        // create a variable pointed at CV 1, check name
        ShortAddrVariableValue var = new ShortAddrVariableValue("label", "comment", "", false, false, false, false, "1", "VVVVVVVV", v, new JLabel(), "");
        Assert.assertTrue(var.label() == "label");
        // pretend you've edited the value, check its in same object
        ((JTextField) var.getCommonRep()).setText("25");
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("25"));
        // manually notify
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        // see if the CV was updated
        Assert.assertTrue(cv1.getValue() == 25);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShortAddrVariableValueTest.class.getName());

}
