package jmri.jmrix.pricom.downloader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the PdiFile class
 *
 * @author	Bob Jacobsen Copyright 2005
  */
public class PdiFileTest extends TestCase {

    public void testCreate() {
        new PdiFile(null);
    }

    // create and show, with some data present
    public void testOpen() {
        PdiFile f = new PdiFile(null);
        Assert.assertNotNull("exists", f);
    }

    // from here down is testing infrastructure
    public PdiFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PdiFileTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PdiFileTest.class);
        return suite;
    }

}
