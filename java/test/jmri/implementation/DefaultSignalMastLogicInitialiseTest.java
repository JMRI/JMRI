package jmri.implementation;

import java.util.Hashtable;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Check that initialising a Signal Mast Logic gives the same result as the
 * checks made when one of its beans changes, and that initialising it again
 * does not add more listeners.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicInitialiseTest {

    private VirtualSignalMast source;
    private VirtualSignalMast dest;
    private SignalMastLogic sml;

    @Test
    public void testAnyStateUserBlockAllowsClearOnInitialise() {
        Block block = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        block.setState(Block.OCCUPIED);
        Hashtable<Block, Integer> blocks = new Hashtable<>();
        blocks.put(block, 0x03); // any state
        sml.setBlocks(blocks, dest);

        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared with an any-state block");
        assertEquals("Approach", source.getAspect());
    }

    @Test
    public void testUserMastSettingOverridesAutoMastOnInitialise() {
        VirtualSignalMast other = new VirtualSignalMast("IF$vsm:basic:one-searchlight($3)");
        other.setAspect("Approach");
        Hashtable<SignalMast, String> auto = new Hashtable<>();
        auto.put(other, "Stop");
        sml.setAutoMasts(auto, dest);
        // the user setting for the same mast overrides the automatic one
        Hashtable<SignalMast, String> user = new Hashtable<>();
        user.put(other, "Approach");
        sml.setMasts(user, dest);

        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared with user mast setting met");
        assertEquals("Approach", source.getAspect());
    }

    @Test
    public void testReinitialiseDoesNotAddListenersAgain() {
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Block block = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        VirtualSignalMast other = new VirtualSignalMast("IF$vsm:basic:one-searchlight($3)");
        int sensorBefore = sensor.getNumPropertyChangeListeners();
        int turnoutBefore = turnout.getNumPropertyChangeListeners();
        int blockBefore = block.getNumPropertyChangeListeners();
        int mastBefore = other.getNumPropertyChangeListeners();

        sml.addSensor("IS1", Sensor.ACTIVE, dest);
        Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts = new Hashtable<>();
        turnouts.put(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IT1", turnout), Turnout.CLOSED);
        sml.setTurnouts(turnouts, dest);
        Hashtable<Block, Integer> blocks = new Hashtable<>();
        blocks.put(block, Block.UNOCCUPIED);
        sml.setBlocks(blocks, dest);
        Hashtable<SignalMast, String> masts = new Hashtable<>();
        masts.put(other, "Stop");
        sml.setMasts(masts, dest);

        sml.initialise();
        assertEquals(sensorBefore + 1, sensor.getNumPropertyChangeListeners(), "sensor listeners after first initialise");
        assertEquals(turnoutBefore + 1, turnout.getNumPropertyChangeListeners(), "turnout listeners after first initialise");
        assertEquals(blockBefore + 1, block.getNumPropertyChangeListeners(), "block listeners after first initialise");
        assertEquals(mastBefore + 1, other.getNumPropertyChangeListeners(), "mast listeners after first initialise");

        // changing one list and initialising again, as the Signalling panel does on Apply
        sml.setTurnouts(turnouts, dest);
        sml.initialise();
        assertEquals(sensorBefore + 1, sensor.getNumPropertyChangeListeners(), "sensor listeners after second initialise");
        assertEquals(turnoutBefore + 1, turnout.getNumPropertyChangeListeners(), "turnout listeners after second initialise");
        assertEquals(blockBefore + 1, block.getNumPropertyChangeListeners(), "block listeners after second initialise");
        assertEquals(mastBefore + 1, other.getNumPropertyChangeListeners(), "mast listeners after second initialise");

        // and dispose removes them all
        sml.dispose();
        assertEquals(sensorBefore, sensor.getNumPropertyChangeListeners(), "sensor listeners after dispose");
        assertEquals(turnoutBefore, turnout.getNumPropertyChangeListeners(), "turnout listeners after dispose");
        assertEquals(blockBefore, block.getNumPropertyChangeListeners(), "block listeners after dispose");
        assertEquals(mastBefore, other.getNumPropertyChangeListeners(), "mast listeners after dispose");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(20);
        source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(source);
        sml.setDestinationMast(dest);
    }

    @AfterEach
    public void tearDown() {
        sml.dispose();
        source = null;
        dest = null;
        sml = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
