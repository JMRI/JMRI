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
 * block in its path is reserved for another train, and not when it is reserved
 * for the train approaching the mast.
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

    private VirtualSignalMast source;
    private VirtualSignalMast dest;
    private LayoutBlock facingLb;
    private LayoutBlock routeLb;
    private DispatcherFrame d;
    private DefaultSignalMastLogic sml;

    /*
     * A mast logic from source to dest over one user defined block, with the
     * source mast facing another block, and Dispatcher running with no trains.
     */
    private void setUpLogic() {
        source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($11)");
        dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($12)");
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        facingLb = lbm.createNewLayoutBlock("ILB11", "Facing Block");
        facingLb.initializeLayoutBlock();
        routeLb = lbm.createNewLayoutBlock("ILB12", "Route Block");
        routeLb.initializeLayoutBlock();
        facingLb.getBlock().setState(Block.UNOCCUPIED);
        routeLb.getBlock().setState(Block.UNOCCUPIED);
        Section section = InstanceManager.getDefault(SectionManager.class).createNewSection("Source To Dest 2");

        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        d = InstanceManager.getDefault(DispatcherFrame.class);

        sml = (DefaultSignalMastLogic) InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(source);
        sml.facingBlock = facingLb;
        sml.setDestinationMast(dest);
        Hashtable<Block, Integer> blocks = new Hashtable<>();
        blocks.put(routeLb.getBlock(), Block.UNOCCUPIED);
        sml.setBlocks(blocks, dest);
        sml.setAssociatedSection(section, dest);
        JUnitAppender.assertWarnMessageStartsWith("This Logic");
        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared");
    }

    /*
     * A train holding one allocated Section with the given blocks, as
     * Dispatcher records it.
     */
    private Section addTrain(String name, Block... held) {
        Section s = InstanceManager.getDefault(SectionManager.class).createNewSection("Held by " + name);
        for (Block b : held) {
            s.addBlock(b);
        }
        s.setState(Section.FORWARD);
        s.setAllocatedTo(name);
        return s;
    }

    private void tearDownLogic() {
        sml.dispose();
        JUnitUtil.dispose(d);
    }

    @Test
    @DisabledIfHeadless
    public void testBlockReservedForApproachingTrainStaysClear() {
        setUpLogic();
        // the train approaching the mast holds both the facing block and the route ahead
        addTrain("Approaching", facingLb.getBlock(), routeLb.getBlock());
        routeLb.setUseExtraColor(true);
        JUnitUtil.waitFor(200); // allow for the signal logic delay
        assertEquals("Approach", source.getAspect(), "own train's reservation does not set danger");
        tearDownLogic();
    }

    @Test
    @DisabledIfHeadless
    public void testBlockReservedForOtherTrainGivesDanger() {
        setUpLogic();
        addTrain("Approaching", facingLb.getBlock());
        addTrain("Other", routeLb.getBlock());
        routeLb.setUseExtraColor(true);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop for another train's block");
        routeLb.setUseExtraColor(false);
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared after release");
        tearDownLogic();
    }

    @Test
    @DisabledIfHeadless
    public void testBlockReservedWithNoTrainApproachingGivesDanger() {
        setUpLogic();
        // nobody holds the facing block, for example a train not run by Dispatcher
        addTrain("Other", routeLb.getBlock());
        routeLb.setUseExtraColor(true);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop for a reserved block");
        tearDownLogic();
    }

    @Test
    @DisabledIfHeadless
    public void testHolderRecordedAfterMarkingClears() {
        setUpLogic();
        addTrain("Approaching", facingLb.getBlock());
        // the block is marked before Dispatcher records who holds it
        Section ahead = InstanceManager.getDefault(SectionManager.class).createNewSection("Ahead");
        ahead.addBlock(routeLb.getBlock());
        ahead.setState(Section.FORWARD);
        routeLb.setUseExtraColor(true);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop while nobody holds the block");
        // only the holder changes: the logic must look again
        ahead.setAllocatedTo("Approaching");
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared once the approaching train holds it");
        tearDownLogic();
    }

    @Test
    @DisabledIfHeadless
    public void testBlockPassedToApproachingTrainClears() {
        setUpLogic();
        addTrain("Approaching", facingLb.getBlock());
        Section ahead = addTrain("Other", routeLb.getBlock());
        routeLb.setUseExtraColor(true);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop for another train's block");
        // the block stays marked while it passes from one train to the next
        ahead.setState(Section.FREE);
        ahead.setState(Section.FORWARD);
        ahead.setAllocatedTo("Approaching");
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared for the approaching train");
        tearDownLogic();
    }

    @Test
    @DisabledIfHeadless
    public void testListenersRemovedOnDispose() {
        setUpLogic();
        Section held = addTrain("Other", routeLb.getBlock());
        assertEquals(1, held.getPropertyChangeListeners(Section.PROPERTY_ALLOCATED_TO).length,
                "a Section added after the logic started is listened to");
        JUnitUtil.waitFor(200); // let the check started by the new holder finish
        tearDownLogic();
        assertEquals(0, held.getPropertyChangeListeners(Section.PROPERTY_ALLOCATED_TO).length,
                "listener removed on dispose");
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
