package jmri.implementation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Signal masts generated from the Layout Editor for a pair (allow automatic
 * signal mast generation): the masts of other logics on the same route are
 * added, but never the pair's own source mast, which would have to show
 * danger for its own route to clear. A mast with itself in that list cleared,
 * went back to danger and cleared again, over and over (issue #15780).
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DefaultSignalMastLogicAutoMastsTest {

    private SignalMastLogicManager smlm;
    private SignalMastManager smm;

    /** Load the Dispatcher SML layout and generate the auto masts of every pair. */
    private void loadWithAutoMastGeneration() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        assertTrue(cm.load(new File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml")));
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        lbm.initializeLayoutBlockPaths();
        JUnitUtil.waitFor(lbm::routingStablised, "routing stable");
        smlm = InstanceManager.getDefault(SignalMastLogicManager.class);
        smm = InstanceManager.getDefault(SignalMastManager.class);
        ThreadingUtil.runOnLayout(() -> {
            for (SignalMastLogic l : smlm.getSignalMastLogicList()) {
                for (SignalMast d : l.getDestinationList()) {
                    l.allowAutoMaticSignalMastGeneration(true, d);
                }
            }
            for (SignalMastLogic l : smlm.getSignalMastLogicList()) {
                l.setupLayoutEditorDetails();
            }
        });
    }

    private List<String> autoMastNames(SignalMastLogic l, SignalMast dest) {
        List<String> names = new ArrayList<>();
        for (SignalMast m : l.getAutoMasts(dest)) {
            names.add(m.getDisplayName());
        }
        return names;
    }

    @Test
    public void testOwnSourceMastIsNotAnAutoMast() throws Exception {
        loadWithAutoMastGeneration();
        for (SignalMastLogic l : smlm.getSignalMastLogicList()) {
            for (SignalMast d : l.getDestinationList()) {
                assertFalse(l.getAutoMasts(d).contains(l.getSourceMast()),
                    l.getSourceMast().getDisplayName() + " to " + d.getDisplayName() + " has its own source mast: "
                    + autoMastNames(l, d));
            }
        }
    }

    @Test
    public void testOtherMastOnTheRouteIsStillAnAutoMast() throws Exception {
        loadWithAutoMastGeneration();
        // the East End Cont and East End Div routes join at the Station East End turnout,
        // and both go on to East To South, so each must hold the other at danger
        SignalMast dest = smm.provideSignalMast("East To South");
        SignalMastLogic cont = smlm.getSignalMastLogic(smm.provideSignalMast("East End Cont"));
        SignalMastLogic div = smlm.getSignalMastLogic(smm.provideSignalMast("East End Div"));
        assertEquals(List.of("East End Div"), autoMastNames(cont, dest));
        assertEquals(List.of("East End Cont"), autoMastNames(div, dest));
    }

    @Test
    public void testMastStaysClearOnceCleared() throws Exception {
        loadWithAutoMastGeneration();
        for (Sensor s : InstanceManager.getDefault(SensorManager.class).getNamedBeanSet()) {
            ThreadingUtil.runOnLayoutWithJmriException(() -> s.setKnownState(Sensor.INACTIVE));
        }
        for (Turnout t : InstanceManager.turnoutManagerInstance().getNamedBeanSet()) {
            ThreadingUtil.runOnLayout(() -> t.setCommandedState(Turnout.CLOSED));
        }
        SignalMast source = smm.provideSignalMast("East End Cont");
        String danger = source.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
        JUnitUtil.waitFor(() -> {
            String aspect = source.getAspect();
            return aspect != null && !aspect.equals(danger);
        }, "East End Cont clears with the turnout closed and the blocks clear");

        List<String> changes = new java.util.concurrent.CopyOnWriteArrayList<>();
        source.addPropertyChangeListener(SignalMast.PROPERTY_ASPECT, e -> changes.add(String.valueOf(e.getNewValue())));
        // With its own mast in the list it went back to danger within one
        // aspect job and one check (750 ms with the default signal logic delay)
        // and then kept changing; four times that is allowed here.
        int window = 4 * (InstanceManager.getDefault(SignalMastLogicManager.class).getSignalLogicDelay() * 3 / 2);
        JUnitUtil.waitFor(window);
        assertEquals(List.of(), changes, "East End Cont stays clear once it has cleared");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initSectionManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

}
