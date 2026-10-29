package jmri.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmri.*;
import jmri.implementation.DefaultSignalMastLogic;
import jmri.implementation.VirtualSignalMast;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for Section generation in DefaultSignalMastLogicManager.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicManagerSectionTest {

    /**
     * A logic with a fixed facing block, fixed blocks and destinations in a
     * known order, standing in for one built from a Layout Editor panel.
     */
    private static class FixedPathLogic extends DefaultSignalMastLogic {

        private final LayoutBlock facing;
        private final List<Block> blocks;
        private final List<SignalMast> destinations;
        final Map<SignalMast, Section> associated = new HashMap<>();

        FixedPathLogic(SignalMast source, LayoutBlock facing, List<Block> blocks, SignalMast... destinations) {
            super(source);
            this.facing = facing;
            this.blocks = blocks;
            this.destinations = Arrays.asList(destinations);
        }

        @Override
        public List<SignalMast> getDestinationList() {
            return new ArrayList<>(destinations);
        }

        @Override
        public LayoutBlock getFacingBlock() {
            return facing;
        }

        @Override
        public List<Block> getAutoBlocksBetweenMasts(SignalMast destination) {
            return new ArrayList<>(blocks);
        }

        @Override
        public void setAssociatedSection(Section sec, SignalMast destination) {
            associated.put(destination, sec);
        }
    }

    @Test
    public void testUserSectionDoesNotStopLaterDestinations() {
        SectionManager secm = InstanceManager.getDefault(SectionManager.class);
        SignalMast src = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "Src");
        SignalMast d1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)", "D1");
        SignalMast d2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($3)", "D2");

        // a user defined section that happens to have the name the logic would use for Src to D1
        Section userSection = secm.createNewSection("Src:D1");
        // and an unrelated user section carrying a forwardMast property
        Section otherSection = secm.createNewSection("Other");
        otherSection.setProperty("forwardMast", "D9");

        LayoutBlock facing = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "Facing");
        Block block = InstanceManager.getDefault(BlockManager.class).provideBlock("IB2");

        FixedPathLogic sml = new FixedPathLogic(src, facing, List.of(block), d1, d2);
        DefaultSignalMastLogicManager smlm = (DefaultSignalMastLogicManager) InstanceManager.getDefault(SignalMastLogicManager.class);
        smlm.register(sml);

        smlm.generateSection();

        assertNull(sml.associated.get(d1), "user section is not used for the logic");
        assertEquals(Section.USERDEFINED, userSection.getSectionType(), "user section left alone");

        Section created = secm.getSection("Src:D2");
        assertNotNull(created, "section generated for the destination after the user section");
        assertEquals(Section.SIGNALMASTLOGIC, created.getSectionType());
        assertSame(created, sml.associated.get(d2));
        assertEquals("D2", created.getProperty("forwardMast"));

        assertEquals("D9", otherSection.getProperty("forwardMast"), "property on a user section is kept");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initSectionManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
