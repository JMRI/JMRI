package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PortalManagerTest {

    private PortalManager _portalMgr;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",_portalMgr);
    }

    @Test
    public void testCreatandGetPortal() {
        // the is was originally part of Warrant test, but none of the asserts
        // are testing anything in the warrant.
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        OBlock bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        OBlock bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        OBlock bSouth = _OBlockMgr.createNewOBlock("OB4", "South");
        
        Portal pNorthWest = _portalMgr.createNewPortal(null, "NorthWest");
        pNorthWest.setToBlock(bWest, false);
        pNorthWest.setFromBlock(bNorth, false);
        Portal pSouthWest = _portalMgr.createNewPortal(null, "SouthWest");
        pSouthWest.setToBlock(bWest, false);
        pSouthWest.setFromBlock(bSouth, false);        
        Assert.assertEquals("Portal", pNorthWest, _portalMgr.getPortal("NorthWest"));
        Assert.assertEquals("Portal Block", bSouth, _portalMgr.getPortal("SouthWest").getFromBlock());
        Assert.assertEquals("Portal", pSouthWest, bSouth.getPortalByName("SouthWest"));        
        Assert.assertEquals("Portal Block", "West", _portalMgr.getPortal("NorthWest").getToBlockName());
        Assert.assertEquals("Portal Block", "North", _portalMgr.getPortal("NorthWest").getFromBlockName());

        Portal pNorthEast = _portalMgr.createNewPortal(null, "NorthEast");
        pNorthEast.setToBlock(_OBlockMgr.getOBlock("OB2"), false);
        pNorthEast.setFromBlock(_OBlockMgr.getOBlock("North"), false);
        Portal pSouthEast = _portalMgr.createNewPortal(null, "SouthEast");
        OBlock east = _OBlockMgr.getOBlock("OB2");
        pSouthEast.setToBlock(east, false);
        pSouthEast.setFromBlock(_OBlockMgr.getOBlock("South"), false);
        
        Assert.assertEquals("Portal Block", east, _portalMgr.getPortal("SouthEast").getToBlock());
        Assert.assertEquals("Portal Block", "West", _portalMgr.getPortal("NorthWest").getToBlockName());
        Assert.assertEquals("Portal Block", _OBlockMgr.getOBlock("South"), _portalMgr.getPortal("SouthWest").getFromBlock());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        _portalMgr = InstanceManager.getDefault(PortalManager.class);        
    }

    @After
    public void tearDown() {
        _portalMgr = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalManagerTest.class);

}
