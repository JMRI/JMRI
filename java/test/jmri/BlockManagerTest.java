package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "SystemName1", b1.getSystemName());
        Assert.assertEquals("user name", "UserName1", b1.getUserName());
    }

    @Test
    public void testCreate2() {
        // original create with systemname and empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("SystemName2", "");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "SystemName2", b1.getSystemName());
        Assert.assertEquals("user name", "", b1.getUserName());
    }

    @Test
    public void testCreate3() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("UserName3");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name", "UserName3", b1.getUserName());
    }

    @Test
    public void testCreate4() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name", "", b1.getUserName());
    }

    @Test
    public void testNameIncrement() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock(null);
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name 1", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name 1", null, b1.getUserName());

        Block b2 = InstanceManager.getDefault( BlockManager.class).createNewBlock(null);
        Assert.assertNotNull(b2);
        Assert.assertEquals("system name 2", "IB:AUTO:0002", b2.getSystemName());
        Assert.assertEquals("user name 2", null, b2.getUserName());

        // and b1 still OK
        Assert.assertEquals("system name 1", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name 1", null, b1.getUserName());
    }

    @Test
    public void testProvideWorksTwice() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.getDefault( BlockManager.class).provideBlock("IB12");
        Assert.assertEquals("system name 12", "IB12", b1.getSystemName());
        Assert.assertNull(b1.getUserName());
        b1 = InstanceManager.getDefault( BlockManager.class).provideBlock("!!");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name !!", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name !!", "!!", b1.getUserName());
    }

    @Test
    public void testGet1() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("UserName4");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name", "UserName4", b1.getUserName());

        Block bget1 = InstanceManager.getDefault( BlockManager.class).getBlock("UserName4");
        Assert.assertNotNull(bget1);
        Assert.assertEquals("get system name by user name", "IB:AUTO:0001", bget1.getSystemName());
        Assert.assertEquals("get user name by user name", "UserName4", bget1.getUserName());

        Block bget2 = InstanceManager.getDefault( BlockManager.class).getBlock("IB:AUTO:0001");
        Assert.assertNotNull(bget2);
        Assert.assertEquals("get system name by system name", "IB:AUTO:0001", bget2.getSystemName());
        Assert.assertEquals("get user name by system name", "UserName4", bget2.getUserName());
    }

    @Test
    public void testProvide1() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("UserName5");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name", "UserName5", b1.getUserName());

        Block bprovide1 = InstanceManager.getDefault( BlockManager.class).provideBlock("UserName5");
        Assert.assertEquals("provide system name by user name", "IB:AUTO:0001", bprovide1.getSystemName());
        Assert.assertEquals("provide user name by user name", "UserName5", bprovide1.getUserName());

        Block bprovide2 = InstanceManager.getDefault( BlockManager.class).provideBlock("IB:AUTO:0001");
        Assert.assertEquals("provide system name by system name", "IB:AUTO:0001", bprovide2.getSystemName());
        Assert.assertEquals("provide user name by system name", "UserName5", bprovide2.getUserName());

        // auto create with prefixed systemname and no username
        Block bprovide3 = InstanceManager.getDefault( BlockManager.class).provideBlock("IBSystemName6");
        Assert.assertEquals("provide system name by user name", "IBSystemName6", bprovide3.getSystemName());
        Assert.assertEquals("provide user name by user name", null, bprovide3.getUserName());

        // auto create with accepted systemname and no username
        Block bprovide4 = InstanceManager.getDefault( BlockManager.class).provideBlock("IB:AUTO:0002");
        Assert.assertEquals("provide system name by system name", "IB:AUTO:0002", bprovide4.getSystemName());
        Assert.assertEquals("provide user name by system name", null, bprovide4.getUserName());
    }

    @Test
    public void testAutoSkip1() {
        Block bautoskip1 = InstanceManager.getDefault( BlockManager.class).createNewBlock("IB:AUTO:0007", "UserName7");
        Assert.assertNotNull(bautoskip1);
        Assert.assertEquals("autoskip system name", "IB:AUTO:0007", bautoskip1.getSystemName());
        Assert.assertEquals("autoskip user name", "UserName7", bautoskip1.getUserName());

        Block bautoskip2 = InstanceManager.getDefault( BlockManager.class).provideBlock("UserName8");
        Assert.assertEquals("autoskip system name skip", "IB:AUTO:0008", bautoskip2.getSystemName());
        Assert.assertEquals("autoskip user name skip", "UserName8", bautoskip2.getUserName());
    }

    @Test
    public void testBlockSpeed1() {
        Block bspeed1 = InstanceManager.getDefault( BlockManager.class).provideBlock("UserName9");
        Assert.assertEquals("block speed system name", "IB:AUTO:0001", bspeed1.getSystemName());
        Assert.assertEquals("block speed user name", "UserName9", bspeed1.getUserName());
        Assert.assertEquals("block speed", "Use Global Normal", bspeed1.getBlockSpeed());
    }

    @Test
    public void testDefaultSpeed1() {
        Assert.assertEquals("default block speed", "Normal", InstanceManager.getDefault( BlockManager.class).getDefaultSpeed());

        // expect this to throw exception because no signal map loaded by default
        
        Exception exc = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            InstanceManager.getDefault( BlockManager.class).setDefaultSpeed("Faster"); });
        Assertions.assertEquals( "Value of requested default block speed \"Faster\" is not valid", exc.getMessage());
        
        jmri.util.JUnitAppender.assertWarnMessage("attempting to get speed for invalid name: 'Faster'");

        try {
            InstanceManager.getDefault( BlockManager.class).setDefaultSpeed("Normal");
        } catch (IllegalArgumentException ex) {
            Assert.fail("failed to reset speed due to: " + ex);
        }
        Assert.assertEquals("block speed back to normal", "Normal", InstanceManager.getDefault( BlockManager.class).getDefaultSpeed());
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
