package jmri.jmrit.consisttool;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of ConsistDataModel
 *
 * @author	Paul Bender Copyright (C) 2015
 * @version	$Revision$
 */
public class ConsistDataModelTest extends TestCase {

    public void testCtor() {
        ConsistDataModel model = new ConsistDataModel(1,4);
        Assert.assertNotNull("exists", model);
    }

    // from here down is testing infrastructure
    public ConsistDataModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConsistDataModelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConsistDataModelTest.class);
        return suite;
    }

}
