package jmri.jmrit.consisttool;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of ConsistFile
 *
 * @author	Paul Bender Copyright (C) 2015
 * @version	$Revision$
 */
public class ConsistFileTest extends TestCase {

    public void testCtor() {
        ConsistFile file = new ConsistFile();
        Assert.assertNotNull("exists", file);
    }

    // from here down is testing infrastructure
    public ConsistFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConsistFileTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConsistFileTest.class);
        return suite;
    }

}
