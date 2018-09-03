package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of AddressPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AddressPanelTest extends TestCase {

    public void testCtor() {
        AddressPanel panel = new AddressPanel();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public AddressPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AddressPanelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AddressPanelTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        jmri.util.JUnitUtil.tearDown();

    }
}
