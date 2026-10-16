package jmri.implementation;

import java.util.Hashtable;

import jmri.*;
import jmri.jmrit.dispatcher.DispatcherFrame;
import jmri.jmrit.dispatcher.OptionsFile;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Check that a Signal Mast Logic with Dispatcher running shows danger when a
 * user defined block in its path is reserved for another train.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicReservedBlockTest {

    @Test
    @DisabledIfHeadless
    public void testUserBlockReservedByDispatcherGivesDanger() {
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");

        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "Block One");
        lb.initializeLayoutBlock();
        Block block = lb.getBlock();
        block.setState(Block.UNOCCUPIED);

        Section section = InstanceManager.getDefault(SectionManager.class).createNewSection("Source To Dest");

        // options without automatic allocation, so no train handling starts
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        Hashtable<Block, Integer> blocks = new Hashtable<>();
        blocks.put(block, Block.UNOCCUPIED);
        sml.setBlocks(blocks, dest);
        sml.setAssociatedSection(section, dest);
        JUnitAppender.assertWarnMessageStartsWith("This Logic");
        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared");

        // Dispatcher marks the block as part of a section allocated to another train
        lb.setUseExtraColor(true);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop for reserved block");
        assertEquals("Stop", source.getAspect());

        // and clears again once the reservation is released
        lb.setUseExtraColor(false);
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared after release");

        sml.dispose();
        JUnitUtil.dispose(d);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initSectionManager();
        JUnitUtil.initDebugThrottleManager();
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(20);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
