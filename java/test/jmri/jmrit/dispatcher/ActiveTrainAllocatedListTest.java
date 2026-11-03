package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * A train's Sections are released on the Dispatcher's thread while its Block
 * changes are handled on another, which reads the Blocks of the train's
 * Sections. A Section released while they are being read must not lose the
 * train any of the Sections it still holds.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ActiveTrainAllocatedListTest {

    private ActiveTrain at;
    private final List<AllocatedSection> sections = new ArrayList<>();

    /**
     * A Section that, when its Blocks are next read, has its allocation
     * released first: what a release on another thread does at that moment.
     */
    private class ReleasedWhileReadSection extends DefaultSection {

        private AllocatedSection releaseNext = null;

        ReleasedWhileReadSection(String name) {
            super(name);
        }

        @Override
        public List<Block> getBlockList() {
            AllocatedSection as = releaseNext;
            if (as != null) {
                releaseNext = null;
                at.removeAllocatedSection(as);
            }
            return super.getBlockList();
        }
    }

    @Test
    public void testBlockListKeepsSectionsStillAllocated() {
        Block first = block("B1");
        Block second = block("B2");
        ReleasedWhileReadSection s1 = new ReleasedWhileReadSection("TS1");
        AllocatedSection as1 = allocate(s1, 1, first);
        allocate(new DefaultSection("TS2"), 2, second);
        assertEquals(Arrays.asList(first, second), at.getBlockList(), "both Sections' Blocks");

        s1.releaseNext = as1;
        List<Block> blocks = at.getBlockList();
        assertEquals(1, at.getAllocatedSectionList().size(), "the first Section was released while the Blocks were read");
        assertEquals(Arrays.asList(first, second), blocks,
                "the Blocks of the Sections allocated when the read began, the second Section's too");
        assertEquals(Arrays.asList(second), at.getBlockList(), "once released, only the second Section's Block");
    }

    private Block block(String name) {
        Block b = new Block("IB" + name, name);
        b.setState(Block.UNOCCUPIED);
        return b;
    }

    private AllocatedSection allocate(Section s, int seq, Block b) {
        s.addBlock(b);
        s.setState(Section.FORWARD);
        AllocatedSection as = new AllocatedSection(s, at, seq, null, 0);
        at.addAllocatedSection(as);
        sections.add(as);
        return as;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DispatcherFrame dispatcher = mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);
        at = new ActiveTrain(new DefaultTransit("TT1"), "Train", ActiveTrain.USER);
        at.setDispatcher(dispatcher);
    }

    @AfterEach
    public void tearDown() {
        for (AllocatedSection as : sections) {
            as.dispose();
        }
        sections.clear();
        at.dispose();
        JUnitUtil.tearDown();
    }

}
