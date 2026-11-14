package jmri.implementation;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.SignalAppearanceMap;
import jmri.SignalMastLogic;
import jmri.SignalMastLogicManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Check that a Signal Mast Logic keeps working after the source mast throws
 * an exception while its held aspect is being set.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicHeldAspectTest {

    /**
     * A virtual mast that fails once when told to show a given aspect, as a
     * hardware mast can do when its connection reports an error.
     */
    private static class FailingMast extends VirtualSignalMast {

        volatile String failOn = null;

        FailingMast(String systemName) {
            super(systemName);
        }

        @Override
        public void setAspect(@Nonnull String aspect) {
            if (aspect.equals(failOn)) {
                failOn = null;
                throw new IllegalStateException("simulated failure setting " + aspect);
            }
            super.setAspect(aspect);
        }
    }

    @Test
    public void testMastRecoversAfterHeldAspectFails() {
        FailingMast source = new FailingMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");

        DefaultSignalMastLogic sml = (DefaultSignalMastLogic) InstanceManager.getDefault(SignalMastLogicManager.class)
            .newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source shows Approach behind a Stop");
        JUnitUtil.waitFor(() -> !sml.inWait, "initial aspect job finished");

        // holding the mast asks for the held aspect, which fails once
        source.failOn = "Stop";
        source.setHeld(true);
        JUnitUtil.waitFor(() -> source.failOn == null, "held aspect was requested");
        JUnitUtil.waitFor(() -> !sml.inWait, "aspect job finished after the failure");

        // the logic must still follow changes after the failure
        source.setHeld(false);
        JUnitUtil.waitFor(() -> !sml.inWait, "aspect job finished after release");
        dest.setAspect("Approach");
        JUnitUtil.waitFor(() -> "Clear".equals(source.getAspect()), "source follows destination after failure");
        assertEquals("Clear", source.getAspect());
        JUnitAppender.assertErrorMessageStartsWith("Could not set held aspect");

        sml.dispose();
    }

    @Test
    public void testDisabledHeldAspectFallsBackToDanger() {
        // this mast type has a held aspect that differs from its danger aspect
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:NYC-1956:SL-3-high($0001)");
        VirtualSignalMast dest = new VirtualSignalMast("IF$vsm:NYC-1956:SL-3-high($0002)");
        String held = source.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.HELD);
        String danger = source.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
        assertEquals("Held", held);
        assertEquals("Stop", danger);

        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        sml.initialise();
        JUnitUtil.waitFor(() -> !danger.equals(source.getAspect()), "source cleared");

        source.setAspectDisabled(held);
        source.setHeld(true);
        JUnitUtil.waitFor(() -> danger.equals(source.getAspect()), "held mast shows danger");

        // and still follows the destination once released
        source.setHeld(false);
        JUnitUtil.waitFor(() -> !danger.equals(source.getAspect()), "source cleared again after release");

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
        JUnitUtil.tearDown();
    }

}
