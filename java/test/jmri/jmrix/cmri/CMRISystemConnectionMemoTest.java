package jmri.jmrix.cmri;

import jmri.Manager.NameValidity;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.cmri.CMRISystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CMRISystemConnectionMemoTest extends SystemConnectionMemoTestBase<CMRISystemConnectionMemo> {

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS2", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS21", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS2001", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS21001", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS127001", 'S'));

        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS999", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS2999", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS21999", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS127999", 'S'));

        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS21B1", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS21B001", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS21B1024", 'S'));

        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS127B1", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS127B001", 'S'));
        Assert.assertEquals(NameValidity.VALID, scm.validSystemNameFormat("CS127B1024", 'S'));

        Assert.assertEquals(NameValidity.INVALID, scm.validSystemNameFormat("CSx", 'S'));
//        jmri.util.JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CSx");

        Assert.assertEquals(NameValidity.VALID_AS_PREFIX_ONLY, scm.validSystemNameFormat("CS2000", 'S'));
//        jmri.util.JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CS2000");

        Assert.assertEquals(NameValidity.INVALID, scm.validSystemNameFormat("CS", 'S'));
//        jmri.util.JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CS");
    }

    @Test
    public void systemPrefixTest() {
        Assert.assertEquals("Default System Prefix", "C", scm.getSystemPrefix());
    }

    @Test
    public void getNodeAddressFromSystemNameTest() {
        Assert.assertEquals("Node Address for CT4", 0, scm.getNodeAddressFromSystemName("CT4"));
        Assert.assertEquals("Node Address for CT1005", 1, scm.getNodeAddressFromSystemName("CT1005"));
        Assert.assertEquals("Node Address for CT5B5", 5, scm.getNodeAddressFromSystemName("CT5B5"));
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        scm = new CMRISystemConnectionMemo();
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}
