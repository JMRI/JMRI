package jmri.jmrix.cmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CMRISystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.cmri.CMRISystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CMRISystemConnectionMemoTest {

    @Test public void constructorTest() {
        CMRISystemConnectionMemo m = new CMRISystemConnectionMemo();
        Assert.assertNotNull(m);
    }

    @Test public void systemPrefixTest() {
        CMRISystemConnectionMemo m = new CMRISystemConnectionMemo();
        Assert.assertEquals("Default System Prefix","C",m.getSystemPrefix());
    }

    @Test public void getNodeAddressFromSystemNameTest() {
        CMRISystemConnectionMemo m = new CMRISystemConnectionMemo();
        Assert.assertEquals("Node Address for CT4",0,m.getNodeAddressFromSystemName("CT4"));
        Assert.assertEquals("Node Address for CT1005",1,m.getNodeAddressFromSystemName("CT1005"));
        Assert.assertEquals("Node Address for CT5B5",5,m.getNodeAddressFromSystemName("CT5B5"));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
   
    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
