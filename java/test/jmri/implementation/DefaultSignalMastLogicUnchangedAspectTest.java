package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Check that a Signal Mast Logic does not send the source mast aspect to the
 * layout again, or unlock and relock its turnouts, when a re-check of a pair
 * finds nothing has changed.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicUnchangedAspectTest {

    /**
     * A virtual mast that counts how often its aspect is set, as each call
     * on a hardware mast sends a command to the layout.
     */
    private static class CountingMast extends VirtualSignalMast {

        final AtomicInteger sets = new AtomicInteger();

        CountingMast(String systemName) {
            super(systemName);
        }

        @Override
        public void setAspect(@Nonnull String aspect) {
            sets.incrementAndGet();
            super.setAspect(aspect);
        }
    }

    @Test
    public void testRecheckWithNoChangeDoesNotResendAspect() {
        CountingMast source = new CountingMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($3)");
        VirtualSignalMast otherDest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        Block block = bm.provideBlock("IB1");
        block.setState(Block.UNOCCUPIED);
        Block otherBlock = bm.provideBlock("IB2");
        otherBlock.setState(Block.UNOCCUPIED);
        Sensor otherSensor = InstanceManager.sensorManagerInstance().provideSensor("IS2");
        otherSensor.setCommandedState(Sensor.INACTIVE);
        Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        turnout.setCommandedState(Turnout.CLOSED);

        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(source);

        // the route to dest is clear: a block and a locked turnout
        sml.setDestinationMast(dest);
        Hashtable<Block, Integer> blocks = new Hashtable<>();
        blocks.put(block, Block.UNOCCUPIED);
        sml.setBlocks(blocks, dest);
        Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts = new Hashtable<>();
        turnouts.put(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IT1", turnout), Turnout.CLOSED);
        sml.setTurnouts(turnouts, dest);
        sml.allowTurnoutLock(true, dest);

        // the route to otherDest is not clear, as its sensor is inactive
        sml.setDestinationMast(otherDest);
        Hashtable<Block, Integer> otherBlocks = new Hashtable<>();
        otherBlocks.put(otherBlock, Block.UNOCCUPIED);
        sml.setBlocks(otherBlocks, otherDest);
        sml.addSensor("IS2", Sensor.ACTIVE, otherDest);

        AtomicInteger stateEvents = new AtomicInteger();
        sml.addPropertyChangeListener(e -> {
            if (SignalMastLogic.PROPERTY_STATE.equals(e.getPropertyName())) {
                stateEvents.incrementAndGet();
            }
        });
        List<PropertyChangeEvent> activeEvents = new CopyOnWriteArrayList<>();
        sml.addPropertyChangeListener(e -> {
            if (SignalMastLogic.PROPERTY_DESTINATION_ACTIVE.equals(e.getPropertyName())) {
                activeEvents.add(e);
            }
        });
        AtomicInteger lockEvents = new AtomicInteger();
        turnout.addPropertyChangeListener(e -> {
            if (Turnout.PROPERTY_LOCKED.equals(e.getPropertyName())) {
                lockEvents.incrementAndGet();
            }
        });

        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared");
        JUnitUtil.waitFor(100); // let the initial settings complete
        int setsBefore = source.sets.get();
        int stateBefore = stateEvents.get();
        int locksBefore = lockEvents.get();
        int activeBefore = activeEvents.size();

        // property changes that make both pairs check again without changing anything
        block.setAllocated(true);
        otherBlock.setAllocated(true);
        JUnitUtil.waitFor(500);

        assertEquals("Approach", source.getAspect());
        assertEquals(setsBefore, source.sets.get(), "aspect not sent again");
        assertEquals(stateBefore, stateEvents.get(), "no state change reported");
        assertEquals(locksBefore, lockEvents.get(), "turnout not unlocked and locked again");
        assertEquals(activeBefore, activeEvents.size(), "no destination change reported");

        // a real change is still followed
        dest.setAspect("Approach");
        JUnitUtil.waitFor(() -> "Clear".equals(source.getAspect()), "source follows destination");
        assertEquals(setsBefore + 1, source.sets.get(), "aspect sent once for the change");

        // A change of another pair is reported as that destination becoming active or inactive,
        // while the source mast keeps showing the aspect for dest (dest is checked first),
        // so the state, which is the destination shown, does not change.
        int stateBeforeOther = stateEvents.get();
        activeEvents.clear();
        otherSensor.setCommandedState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> sml.isActive(otherDest), "other pair becomes active");
        JUnitUtil.waitFor(() -> activeEvents.size() == 1, "other pair reported active");
        assertNull(activeEvents.get(0).getOldValue());
        assertSame(otherDest, activeEvents.get(0).getNewValue());
        assertEquals(dest, sml.getActiveDestination(), "source mast still shows dest");

        activeEvents.clear();
        otherSensor.setCommandedState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> !sml.isActive(otherDest), "other pair becomes inactive");
        JUnitUtil.waitFor(() -> activeEvents.size() == 1, "other pair reported inactive");
        assertSame(otherDest, activeEvents.get(0).getOldValue());
        assertNull(activeEvents.get(0).getNewValue());
        assertEquals(dest, sml.getActiveDestination(), "source mast still shows dest");
        assertEquals("Clear", source.getAspect());
        JUnitUtil.waitFor(100);
        assertEquals(stateBeforeOther, stateEvents.get(), "state not fired, as the destination shown did not change");

        // when the route to dest is no longer clear, the destination shown changes
        activeEvents.clear();
        block.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop when block occupied");
        JUnitUtil.waitFor(() -> stateEvents.get() > stateBeforeOther, "state fired, as no destination is shown now");
        assertNull(sml.getActiveDestination());
        JUnitUtil.waitFor(() -> activeEvents.size() == 1, "dest reported inactive");
        assertSame(dest, activeEvents.get(0).getOldValue());
        assertNull(activeEvents.get(0).getNewValue());

        sml.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(20);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
