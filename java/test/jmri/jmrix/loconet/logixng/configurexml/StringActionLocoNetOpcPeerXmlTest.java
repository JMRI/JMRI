package jmri.jmrix.loconet.logixng.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Bundle
 * 
 * @author Daniel Bergqvist 2018
 */
public class StringActionLocoNetOpcPeerXmlTest {

    @Test
    public void testCtor() {
        StringActionLocoNetOpcPeerXml obj = new StringActionLocoNetOpcPeerXml();
        Assert.assertNotNull("object exists", obj);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
