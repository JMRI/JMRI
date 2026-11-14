package jmri.jmrit.logix;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check that an OBlock sends one "state" property change for each change of
 * state.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class OBlockStateEventTest {

    private OBlock block;
    private final List<PropertyChangeEvent> events = new ArrayList<>();

    @Test
    public void testSetStateFiresOnce() {
        block.setState(OBlock.UNOCCUPIED | OBlock.RUNNING);
        assertEquals(1, events.size(), "one state event for one setState");
        assertEquals(OBlock.UNOCCUPIED | OBlock.RUNNING, events.get(0).getNewValue());
        assertSame(block, events.get(0).getSource(), "event comes from the OBlock");
    }

    @Test
    public void testAllocateAndDeAllocateFireOnce() {
        Warrant w = new Warrant("IW1", "StateEventWarrant");
        assertNull(block.allocate(w));
        assertEquals(1, events.size(), "one state event for allocate");
        assertEquals(OBlock.UNOCCUPIED | OBlock.ALLOCATED, events.get(0).getNewValue());

        events.clear();
        assertTrue(block.deAllocate(w));
        assertEquals(1, events.size(), "one state event for deAllocate");
        assertEquals(OBlock.UNOCCUPIED, events.get(0).getNewValue());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        block = new OBlock("OB1", "StateEventBlock");
        block.setState(OBlock.UNOCCUPIED);
        block.addPropertyChangeListener(e -> {
            if (OBlock.PROPERTY_STATE.equals(e.getPropertyName())) {
                events.add(e);
            }
        });
    }

    @AfterEach
    public void tearDown() {
        block = null;
        events.clear();
        JUnitUtil.tearDown();
    }

}
