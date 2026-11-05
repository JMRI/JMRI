package jmri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests the value for the train a Block is allocated to, which is kept apart
 * from the value for the train in it.
 *
 * @author Keith Bruce 2026
 */
public class BlockAllocatedValueTest {

    private Block block;
    private final List<PropertyChangeEvent> events = new ArrayList<>();

    @Test
    public void testAllocatedValueIsSeparateFromValue() {
        block.setAllocatedValue("Train A");
        assertEquals("Train A", block.getAllocatedValue(), "allocated value");
        assertNull(block.getValue(), "the value still shows no train in the Block");
    }

    @Test
    public void testDisplayValue() {
        assertNull(block.getDisplayValue(), "nothing to show");
        block.setAllocatedValue("Train A");
        assertEquals("Train A", block.getDisplayValue(), "the allocated train while the Block is empty");
        block.setValue("Loco 47");
        assertEquals("Loco 47", block.getDisplayValue(), "the train in the Block");
        block.setValue(null);
        assertEquals("Train A", block.getDisplayValue(), "the allocated train again");
        block.setAllocatedValue(null);
        assertNull(block.getDisplayValue(), "nothing once released");
    }

    @Test
    public void testChangeFiredOnlyWhenChanged() {
        block.setAllocatedValue("Train A");
        block.setAllocatedValue("Train A");
        block.setAllocatedValue(null);
        assertEquals(2, events.size(), "one event for each change");
        assertEquals(Block.PROPERTY_ALLOCATED_VALUE, events.get(0).getPropertyName());
        assertNull(events.get(0).getOldValue());
        assertEquals("Train A", events.get(0).getNewValue());
        assertEquals("Train A", events.get(1).getOldValue());
        assertNull(events.get(1).getNewValue());
    }

    @Test
    public void testTrackingDoesNotUseAllocatedValue() {
        // an unidentified train in the Block before, which is allocated to Train A
        Block before = new Block("IB_BEFORE");
        Path p = new Path();
        p.setBlock(before);
        block.addPath(p);
        before.goingActive();
        before.setAllocatedValue("Train A");
        block.goingActive();
        assertNull(block.getValue(), "the allocated value is not passed on as the train in the Block");

        before.setValue("Loco 47");
        block.goingInactive();
        block.goingActive();
        assertEquals("Loco 47", block.getValue(), "the value is passed on");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        block = new Block("IB_TEST");
        block.addPropertyChangeListener(e -> {
            if (Block.PROPERTY_ALLOCATED_VALUE.equals(e.getPropertyName())) {
                events.add(e);
            }
        });
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
