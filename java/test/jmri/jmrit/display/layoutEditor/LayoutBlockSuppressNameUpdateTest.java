package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that the memory of a Layout Block follows its Block again once name
 * updates are no longer suppressed.
 *
 * @author Keith Bruce 2026
 */
@SuppressWarnings("deprecation") // setSuppressNameUpdate is deprecated, but still works until removed
public class LayoutBlockSuppressNameUpdateTest {

    private Block block;
    private LayoutBlock layoutBlock;
    private Memory memory;

    @Test
    public void testMemoryKeptWhileSuppressed() {
        block.setValue("Train");
        layoutBlock.setSuppressNameUpdate(true);
        block.setValue(null);
        assertEquals("Train", memory.getValue(), "memory not updated while suppressed");
    }

    @Test
    public void testMemoryFollowsBlockWhenResumed() {
        block.setValue("Train");
        layoutBlock.setSuppressNameUpdate(true);
        block.setValue(null);

        layoutBlock.setSuppressNameUpdate(false);
        assertNull(memory.getValue(), "memory set from the block when updates resume");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "Block 1");
        assertNotNull(layoutBlock);
        layoutBlock.initializeLayoutBlock();
        memory = InstanceManager.memoryManagerInstance().provideMemory("IMBLOCK1");
        layoutBlock.setMemory(memory, "IMBLOCK1");
        block = layoutBlock.getBlock();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
