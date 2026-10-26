package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Section;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that Dispatcher's train name, or roster entry, and the alternate
 * colour of allocated Blocks, are put into the Blocks from the train to the
 * end of a Section in its direction of travel.
 * <p>
 * The Section has Blocks B1, B2 and B3, in that order. The train is in B2.
 */
public class DefaultSectionBlockNamesTest {

    private DefaultSection section;
    private Block b1;
    private Block b2;
    private Block b3;
    private LayoutBlock lb1;
    private LayoutBlock lb2;
    private LayoutBlock lb3;

    @Test
    public void testForward() {
        section.setState(Section.FORWARD);
        section.setNameFromActiveBlock("Train");
        assertNull(b1.getValue(), "B1 is behind the train");
        assertEquals("Train", b2.getValue(), "B2 holds the train");
        assertEquals("Train", b3.getValue(), "B3 is ahead");
        JUnitAppender.assertWarnMessage("Section.setNameFromActiveBlock is deprecated, please remove references to it");
    }

    @Test
    public void testReverse() {
        section.setState(Section.REVERSE);
        section.setNameFromActiveBlock("Train");
        assertEquals("Train", b1.getValue(), "B1 is ahead");
        assertEquals("Train", b2.getValue(), "B2 holds the train");
        assertNull(b3.getValue(), "B3 is behind the train");
        JUnitAppender.assertWarnMessage("Section.setNameFromActiveBlock is deprecated, please remove references to it");
    }

    @Test
    public void testFree() {
        section.setState(Section.FREE);
        section.setNameFromActiveBlock("Train");
        assertEquals("Train", b1.getValue(), "B1");
        assertEquals("Train", b2.getValue(), "B2");
        assertEquals("Train", b3.getValue(), "B3");
        JUnitAppender.assertWarnMessage("Section.setNameFromActiveBlock is deprecated, please remove references to it");
    }

    @Test
    public void testSuppressNameUpdateIsDeprecated() {
        section.suppressNameUpdate(true);
        JUnitAppender.assertWarnMessage("Section.suppressNameUpdate is deprecated, please remove references to it");
        JUnitAppender.assertWarnMessage("LayoutBlock.setSuppressNameUpdate is deprecated, please remove references to it");
    }

    @Test
    public void testAlternateColorForward() {
        section.setState(Section.FORWARD);
        section.setAlternateColorFromActiveBlock(true);
        assertFalse(lb1.getUseExtraColor(), "B1 is behind the train");
        assertTrue(lb2.getUseExtraColor(), "B2 holds the train");
        assertTrue(lb3.getUseExtraColor(), "B3 is ahead");
    }

    @Test
    public void testAlternateColorReverse() {
        section.setState(Section.REVERSE);
        section.setAlternateColorFromActiveBlock(true);
        assertTrue(lb1.getUseExtraColor(), "B1 is ahead");
        assertTrue(lb2.getUseExtraColor(), "B2 holds the train");
        assertFalse(lb3.getUseExtraColor(), "B3 is behind the train");
    }

    @Test
    public void testAlternateColorFree() {
        section.setState(Section.FREE);
        section.setAlternateColorFromActiveBlock(true);
        assertTrue(lb1.getUseExtraColor(), "B1");
        assertTrue(lb2.getUseExtraColor(), "B2");
        assertTrue(lb3.getUseExtraColor(), "B3");
    }

    private LayoutBlock layoutBlock(String systemName, String userName) {
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(systemName, userName);
        assertNotNull(lb, userName);
        lb.initializeLayoutBlock();
        return lb;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initLayoutBlockManager();
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        b1 = bm.provideBlock("IB1");
        b1.setUserName("B1");
        b2 = bm.provideBlock("IB2");
        b2.setUserName("B2");
        b3 = bm.provideBlock("IB3");
        b3.setUserName("B3");
        // the Layout Blocks find the Blocks by their user names
        lb1 = layoutBlock("ILB1", "B1");
        lb2 = layoutBlock("ILB2", "B2");
        lb3 = layoutBlock("ILB3", "B3");
        b2.goingActive();
        section = new DefaultSection("IY1", "S1");
        section.addBlock(b1);
        section.addBlock(b2);
        section.addBlock(b3);
    }

    @AfterEach
    public void tearDown() {
        section.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
