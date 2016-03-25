package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the IdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class IdTagTest extends TestCase {

    public void testStateConstants() {

        Assert.assertTrue("Seen and Unseen differ", (IdTag.SEEN != IdTag.UNSEEN));
        Assert.assertTrue("Seen and Unknown differ", (IdTag.SEEN != IdTag.UNKNOWN));
        Assert.assertTrue("Seen and Inconsistent differ", (IdTag.SEEN != IdTag.INCONSISTENT));

        Assert.assertTrue("Unseen and Unknown differ", (IdTag.UNSEEN != IdTag.UNKNOWN));
        Assert.assertTrue("Unseen and Inconsistent differ", (IdTag.UNSEEN != IdTag.INCONSISTENT));

        Assert.assertTrue("Unknown and Inconsistent differ", (IdTag.UNKNOWN != IdTag.INCONSISTENT));

    }

    // from here down is testing infrastructure
    public IdTagTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {IdTagTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IdTagTest.class);
        return suite;
    }

}
