package jmri.jmrit.display.layoutEditor;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("exists", layoutBlock);
        Assert.assertEquals("Test Block", layoutBlock.getUserName());
    }

    @Test
    public void testBlockRename() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // Get the referenced block and change its user name
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        Assert.assertNotNull(block);
        block.setUserName("New Test Block");

        // Verify that the block user name change propagated to the layout block
        Assert.assertEquals("New Test Block", layoutBlock.getUserName());
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
        Assert.assertNotNull(block);
        block.setSensor("IS123");

        // Verify that the block sensor change propagated to the layout block
        Assert.assertEquals("IS123", layoutBlock.getOccupancySensorName());
    }

    @Test
    public void testSetMemoryFromStringBlockValue() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // get a memory and associate it with the layout block.
        Memory mem = jmri.InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("IM1");

        layoutBlock.setMemory(mem,"IM1");

        // verify the memory is associated
        Assert.assertEquals("memory saved",mem,layoutBlock.getMemory());

        // Get the referenced block
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");

        // change the value of the block.
        block.setValue("hello world");

        // and verify the value is in the memory
        Assert.assertEquals("memory content same as block value",block.getValue(),mem.getValue());

    }

    @Test
    public void testSetMemoryFromRosterEntryBlockValue() throws Exception {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // get a memory and associate it with the layout block.
        Memory mem = jmri.InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("IM1");

        layoutBlock.setMemory(mem,"IM1");

        // verify the memory is associated
        Assert.assertEquals("memory saved",mem,layoutBlock.getMemory());

        // Get the referenced block
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");

        // add a roster entry as the block value
        jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

        // change the value of the block.
        block.setValue(re);

        // and verify the value is in the memory
        Assert.assertEquals("memory content same as block value",block.getValue(),mem.getValue());
    }

    @Test
    public void testSetMemoryFromIdTagBlockValue() {
        // initialize the layout block and the related automatic block
        layoutBlock.initializeLayoutBlock();

        // get a memory and associate it with the layout block.
        Memory mem = jmri.InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("IM1");

        layoutBlock.setMemory(mem,"IM1");

        // verify the memory is associated
        Assert.assertEquals("memory saved",mem,layoutBlock.getMemory());

        // Get the referenced block
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");

        jmri.IdTag tag = new jmri.implementation.DefaultIdTag("1234");

        // change the value of the block.
        block.setValue(tag);

        // and verify the value is in the memory
        Assert.assertEquals("memory content same as block value",block.getValue(),mem.getValue());
    }



    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        // Create layout block and the related automatic block
        layoutBlock = new LayoutBlock("ILB999", "Test Block");
    }

    @AfterEach
    public void tearDown() throws Exception {
        layoutBlock = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutBlockTest.class);
}
