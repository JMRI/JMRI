package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the BlockManager class.
 * <p>
 * Based upon a stub by Bob Jacobsen Copyright (C) 2006
 *
 * @author Bob Coleman Copyright 2012
 * @author Bob Jacobsen Copyright 2014
 */
public class BlockManagerTest {

    @Test
    public void testCreate1() {
        // original create with systemname and username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("SystemName1", "UserName1");
        assertNotNull(b1);
        assertEquals( "SystemName1", b1.getSystemName(), "system name");
        assertEquals( "UserName1", b1.getUserName(), "user name");
    }

    @Test
    public void testCreate2() {
        // original create with systemname and empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("SystemName2", "");
        assertNotNull(b1);
        assertEquals( "SystemName2", b1.getSystemName(), "system name");
        assertEquals( "", b1.getUserName(), "user name");
    }

    @Test
    public void testCreate3() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("UserName3");
        assertNotNull(b1);
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name");
        assertEquals( "UserName3", b1.getUserName(), "user name");
    }

    @Test
    public void testCreate4() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("");
        assertNotNull(b1);
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name");
        assertEquals( "", b1.getUserName(), "user name");
    }

    @Test
    public void testNameIncrement() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock(null);
        assertNotNull(b1);
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name 1");
        assertNull( b1.getUserName(), "user name 1");

        Block b2 = InstanceManager.getDefault( BlockManager.class).createNewBlock(null);
        assertNotNull(b2);
        assertEquals( "IB:AUTO:0002", b2.getSystemName(), "system name 2");
        assertNull( b2.getUserName(), "user name 2");

        // and b1 still OK
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name 1");
        assertNull( b1.getUserName(), "user name 1");
    }

    @Test
    public void testProvideWorksTwice() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).provideBlock("IB12");
        assertEquals( "IB12", b1.getSystemName(), "system name 12");
        assertNull(b1.getUserName());
        b1 = InstanceManager.getDefault( BlockManager.class).provideBlock("!!");
        assertNotNull(b1);
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name !!");
        assertEquals( "!!", b1.getUserName(), "user name !!");
    }

    @Test
    public void testGet1() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("UserName4");
        assertNotNull(b1);
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name");
        assertEquals( "UserName4", b1.getUserName(), "user name");

        Block bget1 = InstanceManager.getDefault( BlockManager.class).getBlock("UserName4");
        assertNotNull(bget1);
        assertEquals( "IB:AUTO:0001", bget1.getSystemName(), "get system name by user name");
        assertEquals( "UserName4", bget1.getUserName(), "get user name by user name");

        Block bget2 = InstanceManager.getDefault( BlockManager.class).getBlock("IB:AUTO:0001");
        assertNotNull(bget2);
        assertEquals( "IB:AUTO:0001", bget2.getSystemName(), "get system name by system name");
        assertEquals( "UserName4", bget2.getUserName(), "get user name by system name");
    }

    @Test
    public void testProvide1() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("UserName5");
        assertNotNull(b1);
        assertEquals( "IB:AUTO:0001", b1.getSystemName(), "system name");
        assertEquals( "UserName5", b1.getUserName(), "user name");

        Block bprovide1 = InstanceManager.getDefault( BlockManager.class).provideBlock("UserName5");
        assertEquals( "IB:AUTO:0001", bprovide1.getSystemName(), "provide system name by user name");
        assertEquals( "UserName5", bprovide1.getUserName(), "provide user name by user name");

        Block bprovide2 = InstanceManager.getDefault( BlockManager.class).provideBlock("IB:AUTO:0001");
        assertEquals( "IB:AUTO:0001", bprovide2.getSystemName(), "provide system name by system name");
        assertEquals( "UserName5", bprovide2.getUserName(), "provide user name by system name");

        // auto create with prefixed systemname and no username
        Block bprovide3 = InstanceManager.getDefault( BlockManager.class).provideBlock("IBSystemName6");
        assertEquals( "IBSystemName6", bprovide3.getSystemName(), "provide system name by user name");
        assertNull( bprovide3.getUserName(), "provide user name by user name");

        // auto create with accepted systemname and no username
        Block bprovide4 = InstanceManager.getDefault( BlockManager.class).provideBlock("IB:AUTO:0002");
        assertEquals( "IB:AUTO:0002", bprovide4.getSystemName(), "provide system name by system name");
        assertNull( bprovide4.getUserName(), "provide user name by system name");
    }

    @Test
    public void testAutoSkip1() {
        Block bautoskip1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("IB:AUTO:0007", "UserName7");
        assertNotNull(bautoskip1);
        assertEquals( "IB:AUTO:0007", bautoskip1.getSystemName(), "autoskip system name");
        assertEquals( "UserName7", bautoskip1.getUserName(), "autoskip user name");

        Block bautoskip2 = InstanceManager.getDefault( BlockManager.class).provideBlock("UserName8");
        assertEquals( "IB:AUTO:0008", bautoskip2.getSystemName(), "autoskip system name skip");
        assertEquals( "UserName8", bautoskip2.getUserName(), "autoskip user name skip");
    }

    @Test
    public void testBlockSpeed1() {
        Block bspeed1 = InstanceManager.getDefault( BlockManager.class).provideBlock("UserName9");
        assertEquals( "IB:AUTO:0001", bspeed1.getSystemName(), "block speed system name");
        assertEquals( "UserName9", bspeed1.getUserName(), "block speed user name");
        assertEquals( "Use Global Normal", bspeed1.getBlockSpeed(), "block speed");
    }

    @Test
    public void testDefaultSpeed1() {
        assertEquals( "Normal", InstanceManager.getDefault( BlockManager.class).getDefaultSpeed(),
            "default block speed");

        // expect this to throw exception because no signal map loaded by default
        
        Exception exc = assertThrows(IllegalArgumentException.class, () -> {
            InstanceManager.getDefault( BlockManager.class).setDefaultSpeed("Faster"); });
        assertEquals( "Value of requested default block speed \"Faster\" is not valid", exc.getMessage());
        
        jmri.util.JUnitAppender.assertWarnMessage("attempting to get speed for invalid name: 'Faster'");

        assertDoesNotThrow( () ->
            InstanceManager.getDefault( BlockManager.class).setDefaultSpeed("Normal"));
        assertEquals( "Normal", InstanceManager.getDefault( BlockManager.class).getDefaultSpeed(),
            "block speed back to normal");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
