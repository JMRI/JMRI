// BundleTest.java

package jmri.jmrix;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Bundle class
 * @author      Bob Jacobsen  Copyright (C) 2012
 * @version     $Revision: 17977 $
 */
public class BundleTest extends TestCase {

    public void testGoodKeys() {
        Assert.assertEquals("(none)", Bundle.getMessage("none"));        
        Assert.assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));        
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));        
    } 
    public void testBadKey() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT");   
        } catch (java.util.MissingResourceException e) { return;} // OK
        Assert.fail("No exception thrown");     
    }

    // from here down is testing infrastructure

    public BundleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {BundleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BundleTest.class);
        return suite;
    }

}