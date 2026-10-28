package jmri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that a Block whose sensor briefly goes UNKNOWN or INCONSISTENT gets
 * its value back when the sensor goes active again, as it does after a brief
 * INACTIVE.
 */
public class BlockUnknownStateTest {

    private Block b;

    @Test
    public void testValueRestoredAfterBriefInactive() {
        b.goingInactive();
        assertNull(b.getValue(), "value cleared");
        b.goingActive();
        assertEquals("TrainA", b.getValue(), "value restored");
    }

    @Test
    public void testValueRestoredAfterBriefUnknown() {
        b.goingUnknown();
        assertNull(b.getValue(), "value cleared");
        b.goingActive();
        assertEquals("TrainA", b.getValue(), "value restored");
    }

    @Test
    public void testValueRestoredAfterBriefInconsistent() {
        b.goingInconsistent();
        assertNull(b.getValue(), "value cleared");
        b.goingActive();
        assertEquals("TrainA", b.getValue(), "value restored");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        b = new Block("IB_Y");
        b.goingActive();
        // TrainA has been in the Block since the start of the session
        b.setValue("TrainA");
        assertEquals("TrainA", b.getValue(), "TrainA in the Block");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
