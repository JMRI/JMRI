package jmri.jmrix.powerline;

import jmri.util.JUnitAppender;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author	Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007, 2008
 * @version	$Revision$
 */
public class SerialAddressTest extends TestCase {

    SerialTrafficControlScaffold tc = null;

    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - PLA1", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA1", 'L'));
        Assert.assertTrue("valid format - PLA16", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLA16", 'L'));
        Assert.assertTrue("valid format - PLK3", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLK3", 'L'));
        Assert.assertTrue("valid format - PTA1", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA1", 'T'));
        Assert.assertTrue("valid format - PTA16", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTA16", 'T'));
        Assert.assertTrue("valid format - PTK3", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PTK3", 'T'));

        Assert.assertTrue("invalid format - PL2", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2");
        Assert.assertTrue("invalid format - PL0B2", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL0B2", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL0B2");
        Assert.assertTrue("invalid format - PL", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL");

        Assert.assertTrue("valid format - PLB2", tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PLB2", 'L'));

        Assert.assertTrue("invalid format - PY2005", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PY2005", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: PY2005");

        Assert.assertTrue("invalid format - PY2B5", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PY2B5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: PY2B5");

        Assert.assertTrue("invalid format - PL22001", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22001", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL22001");
        Assert.assertTrue("invalid format - PL22B1", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22B1", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL22B1");

        Assert.assertTrue("invalid format - PL22000", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22000", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL22000");

        Assert.assertTrue("invalid format - PL22B0", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL22B0", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL22B0");

        Assert.assertTrue("invalid format - PL2999", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2999", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2999");
        Assert.assertTrue("invalid format - PL2B2048", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B2048", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2B2048");

        Assert.assertTrue("invalid format - PL2B2049", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B2049", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2B2049");

        Assert.assertTrue("invalid format - PL2B33", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B33", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2B33");

        Assert.assertTrue("invalid format - PL127032", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127032", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL127032");

        Assert.assertTrue("invalid format - PL127001", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127001", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL127001");

        Assert.assertTrue("invalid format - PL127000", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127000", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL127000");

        Assert.assertTrue("invalid format - PL127B7", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL127B7", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL127B7");

        Assert.assertTrue("invalid format - PL128B7", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL128B7", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL128B7");

        Assert.assertTrue("invalid format - PL2oo5", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2oo5", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2oo5");

        Assert.assertTrue("invalid format - PL2aB5", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2aB5", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2aB5");

        Assert.assertTrue("invalid format - PL2B5x", !tc.getAdapterMemo().getSerialAddress().validSystemNameFormat("PL2B5x", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL2B5x");
    }

    public void testValidSystemNameConfig() {
        Assert.assertTrue("valid config PLA1", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLA1", 'L'));
        Assert.assertTrue("valid config PLB7", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PLB7", 'L'));

        Assert.assertTrue("invalid config PL4007", !tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL4007", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL4007");
        JUnitAppender.assertWarnMessage("PL4007 invalid; bad format");

        Assert.assertTrue("invalid config PL10033", !tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PL10033", 'L'));
        JUnitAppender.assertErrorMessage("address did not match any valid forms: PL10033");
        JUnitAppender.assertWarnMessage("PL10033 invalid; bad format");

        Assert.assertTrue("valid config PSK16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSK16", 'S'));
        Assert.assertTrue("valid config PSP16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PSP16", 'S'));

        Assert.assertTrue("valid config PTK16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTK16", 'T'));
        Assert.assertTrue("valid config PTP16", tc.getAdapterMemo().getSerialAddress().validSystemNameConfig("PTP16", 'T'));

    }

    public void testIsInsteonTrue() {
        Assert.assertTrue("PL01.02.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.03"));
        Assert.assertTrue("PLA1.02.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1.02.03"));
        Assert.assertTrue("PLA1.A2.03", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.A2.03"));
        Assert.assertTrue("PLA1.02.A3", tc.getAdapterMemo().getSerialAddress().isInsteon("PL01.02.A3"));
    }

    public void testIsInsteonFalse() {
        Assert.assertFalse("PLA1", tc.getAdapterMemo().getSerialAddress().isInsteon("PLA1"));
    }

    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize PLB7", "PLB7", tc.getAdapterMemo().getSerialAddress().normalizeSystemName("PLB7"));
    }

    // from here down is testing infrastructure
    public SerialAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialAddressTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialAddressTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
