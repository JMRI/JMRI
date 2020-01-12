package jmri.progdebugger;

import jmri.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Test the DebugProgrammerManager class.
 *
 * @author	Bob Jacobsen Copyright 2002
 */
public class DebugProgrammerManagerTest {

    /**
     * Service mode request returns a programmer
     */
    @Test
    public void testServiceModeRequest() {
        InstanceManager.setDefault(GlobalProgrammerManager.class,
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                .getGlobalProgrammer();
        Assert.assertTrue("got service mode", p != null);
        Assert.assertTrue("correct type", (p instanceof ProgDebugger));
    }

    /**
     * Any service mode request gets the same object
     */
    @Test
    public void testServiceModeUnique() {
        InstanceManager.setDefault(GlobalProgrammerManager.class,
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                .getGlobalProgrammer();
        Assert.assertTrue("same service mode programmer",
                InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                        .getGlobalProgrammer() == p);
    }

    /**
     * ops mode request returns a programmer
     */
    @Test
    public void testOpsModeRequest() {
        InstanceManager.store(new DebugProgrammerManager(), AddressedProgrammerManager.class);
        Programmer p = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        Assert.assertTrue("got ops mode", p != null);
        Assert.assertTrue("correct type", (p instanceof ProgDebugger));
    }

    /**
     * Any identical ops mode request gets the same object
     */
    @Test
    public void testOpsModeUnique() {
        InstanceManager.store(new DebugProgrammerManager(), AddressedProgrammerManager.class);
        Programmer p = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        Assert.assertTrue("same ops mode programmer",
                InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                        .getAddressedProgrammer(true, 777) == p);
    }

    /**
     * Any identical ops mode request gets the same object
     */
    @Test
    public void testOpsModeDistinct() {
        InstanceManager.store(new DebugProgrammerManager(), AddressedProgrammerManager.class);
        Programmer p = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        Assert.assertTrue("different ops mode programmer",
                InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                        .getAddressedProgrammer(true, 888) != p);
        Assert.assertTrue("same ops mode programmer",
                InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                        .getAddressedProgrammer(true, 777) == p);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }

}
