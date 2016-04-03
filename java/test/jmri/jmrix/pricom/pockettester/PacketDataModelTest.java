package jmri.jmrix.pricom.pockettester;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the PacketDataModel class
 *
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class PacketDataModelTest extends TestCase {

    public void testCreate() {
        new PacketDataModel();
    }

    public void testGetPrefix() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("version", "PRICOM D", f.getPrefix(PocketTesterTest.version));
        Assert.assertEquals("speed012A", "ADR= 012", f.getPrefix(PocketTesterTest.speed012A));
    }

    public void testGetKey() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("version", null, f.getKey(PocketTesterTest.version));
        Assert.assertEquals("speed012A", "ADR= 012 CMD=Speed    ", f.getKey(PocketTesterTest.speed012A));
    }

    public void testGetType() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("speed012A", "CMD=Speed    ", f.getType(PocketTesterTest.speed012A));
        Assert.assertEquals("acc0222A", "CMD=Accessry ", f.getType(PocketTesterTest.acc0222A));
    }

    public void testinsert() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("no rows", 0, f.getRowCount());
        f.asciiFormattedMessage(PocketTesterTest.speed012A);
        Assert.assertEquals("one row", 1, f.getRowCount());
        f.asciiFormattedMessage(PocketTesterTest.speed0123A);
        Assert.assertEquals("two rows", 2, f.getRowCount());
        f.asciiFormattedMessage(PocketTesterTest.speed012A);
        Assert.assertEquals("Still two rows", 2, f.getRowCount());
    }

    // from here down is testing infrastructure
    public PacketDataModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PacketDataModelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PacketDataModelTest.class);
        return suite;
    }

}
