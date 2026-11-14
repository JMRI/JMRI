package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Check that a LayoutBlock only sends a routing update to its neighbours
 * when the status it advertises changes. Routing updates are sent on the
 * layout thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LayoutBlockRoutingUpdateTest {

    private LayoutBlock layoutBlock = null;
    private final List<Object> updates = new ArrayList<>();

    @Test
    public void testRoutingUpdateOnlyOnStatusChange() throws JmriException {
        InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
        layoutBlock.initializeLayoutBlock();
        Block block = layoutBlock.getBlock();
        layoutBlock.addPropertyChangeListener(e -> {
            if (LayoutBlock.PROPERTY_ROUTING.equals(e.getPropertyName())) {
                updates.add(e.getNewValue());
            }
        });

        ThreadingUtil.runOnLayout(() -> block.setState(Block.UNOCCUPIED));
        assertEquals(1, updates.size(), "update when the block becomes empty");

        for (int i = 0; i < 10; i++) {
            String value = "Train " + i;
            ThreadingUtil.runOnLayout(() -> block.setValue(value));
        }
        ThreadingUtil.runOnLayout(() -> block.setDirection(jmri.Path.EAST));
        assertEquals(1, updates.size(), "no update for value or direction changes");

        ThreadingUtil.runOnLayout(() -> layoutBlock.setUseExtraColor(true));
        assertEquals(2, updates.size(), "one update when the block is allocated");

        ThreadingUtil.runOnLayout(() -> layoutBlock.setUseExtraColor(true));
        assertEquals(2, updates.size(), "no update when the allocation is unchanged");

        ThreadingUtil.runOnLayout(() -> block.setState(Block.OCCUPIED));
        assertEquals(3, updates.size(), "update when the block becomes occupied");

        ThreadingUtil.runOnLayout(() -> layoutBlock.setUseExtraColor(false));
        assertEquals(3, updates.size(), "no update, an occupied block is advertised as occupied");

        // after routing is set up again the status is sent with the next change
        ThreadingUtil.runOnLayout(() -> layoutBlock.initializeLayoutBlockRouting());
        ThreadingUtil.runOnLayout(() -> block.setValue("Another train"));
        assertEquals(4, updates.size(), "update after routing is set up again");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        layoutBlock = new LayoutBlock("ILB997", "Routing Update Block");
    }

    @AfterEach
    public void tearDown() {
        layoutBlock.dispose();
        layoutBlock = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
