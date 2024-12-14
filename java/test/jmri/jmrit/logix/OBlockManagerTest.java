package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the OBlockManager class.
 *
 * @author Bob Coleman Copyright 2012
 * @author Bob Jacobsen Copyright 2014
 */
public class OBlockManagerTest {

    private OBlockManager l;
    
    @Test
    public void testProvide() {
        // original create with systemname
        OBlock b1 = l.provide("OB101");
        assertNotNull(b1);
        assertEquals("OB101", b1.getSystemName(), "system name");
    }

    @Test
    public void testProvideWorksTwice() {
        Block b1 = l.provide("OB102");
        Block b2 = l.provide("OB102");
        assertNotNull(b1);
        assertNotNull(b2);
        assertEquals(b1, b2);
    }

    @Test
    public void testProvideFailure() {
        
        Exception ex = assertThrows( IllegalArgumentException.class,
            () -> { l.provide("");}, "Exception thrown");
        assertNotNull(ex);
        assertTrue( ex.getMessage().toLowerCase().contains("oblock") , "no oblock string in " + ex.getMessage() );
    }

    @Test
    public void testCreateNewOBlock() {
        assertNull( l.createNewOBlock("", "user"), "createNewOBlock no sysname");
        assertNull( l.createNewOBlock("OB", "user"), "createNewOBlock OB");
    }

    @Test
    public void testGetOBlock() {
        // the is was originally part of Warrant test, but none of the asserts
        // are testing anything in the warrant.
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        OBlock bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        OBlock bEast = _OBlockMgr.createNewOBlock("OB2", "East");
        OBlock bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        OBlock bSouth = _OBlockMgr.createNewOBlock("OB4", "South");
        assertEquals( bWest, _OBlockMgr.getOBlock("West"), "OBlock W");
        assertEquals( bEast, _OBlockMgr.getOBlock("OB2"), "OBlock 2");
        assertEquals( bNorth, _OBlockMgr.getOBlock("North"), "OBlock N");
        assertEquals( bSouth, _OBlockMgr.getOBlock("OB4"), "OBlock 4");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        JUnitUtil.tearDown();
    }

}
