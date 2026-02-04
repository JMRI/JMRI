package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.jdom2.JDOMException;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutBlock
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutBlockTest {

    private LayoutBlock layoutBlock = null;

    @Test
    public void testCtor() {
        assertNotNull( layoutBlock, "exists");
        assertEquals("Test Block", layoutBlock.getUserName());
    }

    @Test
    public void testBlockRename() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // Get the referenced block and change its user name
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        assertNotNull(block);
        block.setUserName("New Test Block");

        // Verify that the block user name change propagated to the layout block
        assertEquals("New Test Block", layoutBlock.getUserName());
    }

    @Test
    public void testBlockSensor() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // Create an occupancy sensor
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        sm.provideSensor("IS123");

        // Get the referenced block and set its occupancy sensor
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        assertNotNull(block);
        block.setSensor("IS123");

        // Verify that the block sensor change propagated to the layout block
        assertEquals("IS123", layoutBlock.getOccupancySensorName());
    }

    @Test
    public void testSetMemoryFromStringBlockValue() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // get a memory and associate it with the layout block.
        Memory mem = jmri.InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("IM1");

        layoutBlock.setMemory(mem,"IM1");

        // verify the memory is associated
        assertEquals( mem, layoutBlock.getMemory(), "memory saved");

        // Get the referenced block
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        Assertions.assertNotNull(block);

        // change the value of the block.
        block.setValue("hello world");

        // and verify the value is in the memory
        assertEquals( block.getValue(), mem.getValue(), "memory content same as block value");

    }

    @Test
    public void testSetMemoryFromRosterEntryBlockValue() throws IOException, JDOMException {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // get a memory and associate it with the layout block.
        Memory mem = jmri.InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("IM1");

        layoutBlock.setMemory(mem,"IM1");

        // verify the memory is associated
        assertEquals( mem, layoutBlock.getMemory(), "memory saved");

        // Get the referenced block
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        Assertions.assertNotNull(block);

        // add a roster entry as the block value
        jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

        // change the value of the block.
        block.setValue(re);

        // and verify the value is in the memory
        assertEquals( block.getValue(), mem.getValue(), "memory content same as block value");
    }

    @Test
    public void testSetMemoryFromIdTagBlockValue() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // get a memory and associate it with the layout block.
        Memory mem = jmri.InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("IM1");

        layoutBlock.setMemory(mem,"IM1");

        // verify the memory is associated
        assertEquals( mem, layoutBlock.getMemory(), "memory saved");

        // Get the referenced block
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        Assertions.assertNotNull(block);

        jmri.IdTag tag = new jmri.implementation.DefaultIdTag("1234");

        // change the value of the block.
        block.setValue(tag);

        // and verify the value is in the memory
        assertEquals( block.getValue(), mem.getValue(), "memory content same as block value");
    }



    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        // Create layout block and the related automatic block
        layoutBlock = new LayoutBlock("ILB999", "Test Block");
    }

    @AfterEach
    public void tearDown() {
        layoutBlock.dispose();
        layoutBlock = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutBlockTest.class);
}
