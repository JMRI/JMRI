package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.Block;
import jmri.Section;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that Dispatcher's train name, or roster entry, is put into the Blocks
 * from the train to the end of a Section in its direction of travel.
 * <p>
 * The Section has Blocks B1, B2 and B3, in that order. The train is in B2.
 */
public class DefaultSectionBlockNamesTest {

    private DefaultSection section;
    private Block b1;
    private Block b2;
    private Block b3;

    @Test
    public void testForward() {
        section.setState(Section.FORWARD);
        section.setNameFromActiveBlock("Train");
        assertNull(b1.getValue(), "B1 is behind the train");
        assertEquals("Train", b2.getValue(), "B2 holds the train");
        assertEquals("Train", b3.getValue(), "B3 is ahead");
    }

    @Test
    public void testReverse() {
        section.setState(Section.REVERSE);
        section.setNameFromActiveBlock("Train");
        assertEquals("Train", b1.getValue(), "B1 is ahead");
        assertEquals("Train", b2.getValue(), "B2 holds the train");
        assertNull(b3.getValue(), "B3 is behind the train");
    }

    @Test
    public void testFree() {
        section.setState(Section.FREE);
        section.setNameFromActiveBlock("Train");
        assertEquals("Train", b1.getValue(), "B1");
        assertEquals("Train", b2.getValue(), "B2");
        assertEquals("Train", b3.getValue(), "B3");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initLayoutBlockManager();
        b1 = new Block("IB1", "B1");
        b2 = new Block("IB2", "B2");
        b3 = new Block("IB3", "B3");
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
