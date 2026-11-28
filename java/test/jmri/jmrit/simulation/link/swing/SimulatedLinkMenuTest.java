package jmri.jmrit.simulation.link.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.JRadioButtonMenuItem;

import jmri.jmrit.simulation.link.ArbitrationPolicy;
import jmri.jmrit.simulation.link.LinkPreset;
import jmri.jmrit.simulation.link.ManualClock;
import jmri.jmrit.simulation.link.SimulatedLink;
import jmri.jmrit.simulation.link.WireTiming;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SimulatedLinkMenu.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedLinkMenuTest {

    @Test
    public void testOneChoicePerPresetAndChoosingSetsIt() {
        ManualClock clock = new ManualClock(0);
        LinkPreset preset = new LinkPreset("Gateway", List::of, List::of);
        SimulatedLink link = new SimulatedLink(new WireTiming() {
            @Override
            public long nanosOnWire(@Nonnull Object message) {
                return 0;
            }

            @Override
            public int priority(@Nonnull Object message) {
                return 0;
            }

            @Override
            @Nonnull
            public ArbitrationPolicy policy() {
                return ArbitrationPolicy.FIFO;
            }
        }, List.of(preset), () -> clock);
        SimulatedLinkMenu menu = new SimulatedLinkMenu(link);
        assertEquals(2, menu.getItemCount());
        JRadioButtonMenuItem none = (JRadioButtonMenuItem) menu.getItem(0);
        JRadioButtonMenuItem gateway = (JRadioButtonMenuItem) menu.getItem(1);
        assertTrue(none.isSelected(), "None is in use to start with");
        assertFalse(gateway.isSelected());
        gateway.doClick();
        assertSame(preset, link.getPreset());
        assertTrue(link.isOn());
        none.doClick();
        assertFalse(link.isOn());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
