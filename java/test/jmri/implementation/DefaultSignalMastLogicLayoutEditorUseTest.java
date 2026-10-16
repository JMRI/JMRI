package jmri.implementation;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMastLogic;
import jmri.SignalMastLogicManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Check that turning off Layout Editor use for one destination of a Signal
 * Mast Logic does not affect the other destinations of the same source mast.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicLayoutEditorUseTest {

    @Test
    public void testFacingBlockKeptWhileAnotherDestinationUsesLayoutEditor() throws JmriException {
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast dest1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        VirtualSignalMast dest2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($3)");
        LayoutBlock facing = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "Facing");

        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(source);
        sml.setDestinationMast(dest1);
        sml.setDestinationMast(dest2);
        sml.useLayoutEditor(true, dest1);
        sml.useLayoutEditor(true, dest2);
        sml.setFacingBlock(facing);
        assertTrue(sml.useLayoutEditor(dest1));

        // dest1 stops using the Layout Editor, dest2 still does
        sml.useLayoutEditor(false, dest1);
        assertFalse(sml.useLayoutEditor(dest1));
        assertTrue(sml.useLayoutEditor(dest2));
        assertSame(facing, sml.getFacingBlock(), "facing block of the source mast is kept");

        // once no destination uses the Layout Editor it is cleared
        sml.useLayoutEditor(false, dest2);
        assertNull(sml.getFacingBlock(), "facing block cleared when no destination uses Layout Editor");

        sml.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
