package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Path class
 *
 * @author	Bob Jacobsen Copyright (C) 2016
 */
public class ConditionalVariableTest extends TestCase {

    public void testEquals() {
        ConditionalVariable c1 = new ConditionalVariable(false, 1, 2, "name", false);
        ConditionalVariable c2 = new ConditionalVariable(false, 1, 2, "name", false);
        
        Assert.assertTrue("identity", c1.equals(c1));
        Assert.assertFalse("object equals, not content equals", c1.equals(c2));
    }
    
    // from here down is testing infrastructure
    public ConditionalVariableTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ConditionalVariableTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConditionalVariableTest.class);
        return suite;
    }

    protected void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

}
