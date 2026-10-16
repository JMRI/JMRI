package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.implementation.DefaultSection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Tests for {@link AllocatedSection#getLengthRemaining(Block)}, the distance
 * used to stop or change speed within a Section.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AllocatedSectionLengthTest {

    private Block b1;
    private Block b2;
    private Block b3;
    private Block outside;
    private Section section;
    private AllocatedSection as;

    private Block block(String sysName, String userName, float lengthMm) {
        Block b = new Block(sysName, userName);
        b.setLength(lengthMm);
        b.setState(Block.UNOCCUPIED);
        return b;
    }

    @Test
    public void testForward() {
        section.setState(Section.FORWARD);
        assertEquals(600.0f, as.getLengthRemaining(b1), 0.01f, "from B1, all of the Section");
        assertEquals(500.0f, as.getLengthRemaining(b2), 0.01f, "from B2, B2 and B3");
        assertEquals(300.0f, as.getLengthRemaining(b3), 0.01f, "from B3, B3 only");
        assertEquals(600.0f, as.getLengthRemaining(outside), 0.01f, "not yet in the Section");
    }

    @Test
    public void testReverse() {
        section.setState(Section.REVERSE);
        assertEquals(600.0f, as.getLengthRemaining(b3), 0.01f, "from B3, all of the Section");
        assertEquals(300.0f, as.getLengthRemaining(b2), 0.01f, "from B2, B2 and B1");
        assertEquals(100.0f, as.getLengthRemaining(b1), 0.01f, "from B1, B1 only");
        assertEquals(600.0f, as.getLengthRemaining(outside), 0.01f, "not yet in the Section");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);

        b1 = block("IB1", "B1", 100.0f);
        b2 = block("IB2", "B2", 200.0f);
        b3 = block("IB3", "B3", 300.0f);
        outside = block("IB4", "B4", 400.0f);
        section = new DefaultSection("TS1");
        section.addBlock(b1);
        section.addBlock(b2);
        section.addBlock(b3);

        jmri.Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        as = new AllocatedSection(section, at, 1, null, 0);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
