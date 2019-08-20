package jmri.jmrix.cmri;

import jmri.Manager.NameValidity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.cmri.CMRISystemConnectionMemo class.
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CMRISystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    public void testValidSystemNameFormat() {
        CMRISystemConnectionMemo m = (CMRISystemConnectionMemo)scm;

        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS2",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS21",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS2001",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS21001",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS127001",'S'));

        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS999",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS2999",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS21999",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS127999",'S'));

        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS21B1",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS21B001",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS21B1024",'S'));
          
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS127B1",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS127B001",'S'));
        Assert.assertEquals(NameValidity.VALID, m.validSystemNameFormat("CS127B1024",'S'));

        Assert.assertEquals(NameValidity.INVALID, m.validSystemNameFormat("CSx",'S'));
//        jmri.util.JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CSx");

        Assert.assertEquals(NameValidity.VALID_AS_PREFIX_ONLY, m.validSystemNameFormat("CS2000",'S'));
//        jmri.util.JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CS2000");

        Assert.assertEquals(NameValidity.INVALID, m.validSystemNameFormat("CS",'S'));
//        jmri.util.JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CS");
    }

    @Test
    public void systemPrefixTest() {
        CMRISystemConnectionMemo m = (CMRISystemConnectionMemo)scm;
        Assert.assertEquals("Default System Prefix", "C", m.getSystemPrefix());
    }

    @Test
    public void getNodeAddressFromSystemNameTest() {
        CMRISystemConnectionMemo m = (CMRISystemConnectionMemo)scm;
        Assert.assertEquals("Node Address for CT4",0,m.getNodeAddressFromSystemName("CT4"));
        Assert.assertEquals("Node Address for CT1005",1,m.getNodeAddressFromSystemName("CT1005"));
        Assert.assertEquals("Node Address for CT5B5",5,m.getNodeAddressFromSystemName("CT5B5"));
    }

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        scm = new CMRISystemConnectionMemo();
    }
   
    @After
    @Override
    public void tearDown() {
        scm = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
