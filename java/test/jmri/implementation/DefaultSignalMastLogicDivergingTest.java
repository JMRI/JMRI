package jmri.implementation;

import jmri.*;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Check that a Signal Mast Logic built from a Layout Editor panel treats the
 * continuing route through a turnout as straight, also when the continuing
 * sense of that turnout is THROWN.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicDivergingTest {

    private static final float TOLERANCE = 0.0001f;

    @Test
    @DisabledIfHeadless
    public void testContinuingSenseThrownIsStraight() throws Exception {
        ConfigXmlManager cm = new ConfigXmlManager() {
        };
        Assertions.assertTrue(cm.load(new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml")));
        InstanceManager.getDefault(LayoutBlockManager.class).initializeLayoutBlockPaths();

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        SignalMast source = smm.getSignalMast("East End Throat");
        SignalMast cont = smm.getSignalMast("West End Cont");
        SignalMast div = smm.getSignalMast("West End Div");
        Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout("Station East End");
        assertNotNull(turnout);
        // make the two limits easy to tell apart
        turnout.setStraightSpeed("Normal");
        turnout.setDivergingSpeed("Restricted");
        assertNotEquals(turnout.getStraightLimit(), turnout.getDivergingLimit(), TOLERANCE);

        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalMastLogic(source);
        assertNotNull(sml);
        JUnitUtil.waitFor(() -> sml.getAutoTurnouts(cont).contains(turnout) && sml.getAutoTurnouts(div).contains(turnout),
            "routes set up from the Layout Editor");

        // with the usual continuing sense, the continuing route needs the turnout closed
        assertEquals(Turnout.CLOSED, sml.getAutoTurnoutState(turnout, cont));
        assertEquals(Turnout.THROWN, sml.getAutoTurnoutState(turnout, div));
        float contSpeed = sml.getMaximumSpeed(cont);
        float divSpeed = sml.getMaximumSpeed(div);
        assertEquals(turnout.getDivergingLimit(), divSpeed, TOLERANCE, "diverging route limited by the turnout");
        assertNotEquals(divSpeed, contSpeed, TOLERANCE, "continuing route not limited to the diverging speed");

        // now the turnout is wired the other way round: continuing sense is THROWN
        LayoutTurnout lt = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class).stream()
            .flatMap(le -> le.getLayoutTurnouts().stream())
            .filter(t -> t.getTurnout() == turnout)
            .findFirst().orElseThrow();
        lt.setContinuingSense(Turnout.THROWN);
        sml.setupLayoutEditorDetails();
        JUnitUtil.waitFor(() -> sml.getAutoTurnoutState(turnout, cont) == Turnout.THROWN, "continuing route now needs THROWN");
        assertEquals(Turnout.CLOSED, sml.getAutoTurnoutState(turnout, div));

        // the physical routes are unchanged, so the speeds must be too
        assertEquals(contSpeed, sml.getMaximumSpeed(cont), TOLERANCE, "continuing route speed");
        assertEquals(divSpeed, sml.getMaximumSpeed(div), TOLERANCE, "diverging route speed");
    }

    /**
     * A simple panel where the continuing route through turnout T1 needs it
     * THROWN: BT-A to BS-EB is the continuing route, BT-A to BM-EB the
     * diverging one. The path speed and the aspect of BT-A must follow the
     * route, not the turnout state.
     */
    @Test
    @DisabledIfHeadless
    public void testContinuingThrownPanelSpeedsAndAspects() throws Exception {
        ConfigXmlManager cm = new ConfigXmlManager() {
        };
        Assertions.assertTrue(cm.load(new java.io.File("java/test/jmri/implementation/SignalMastLogicContinuingThrown.xml")));
        InstanceManager.getDefault(LayoutBlockManager.class).initializeLayoutBlockPaths();

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        SignalMast source = smm.getSignalMast("BT-A");
        SignalMast cont = smm.getSignalMast("BS-EB");
        SignalMast div = smm.getSignalMast("BM-EB");
        Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout("T1");
        assertNotNull(turnout);
        assertNotEquals(turnout.getStraightLimit(), turnout.getDivergingLimit(), TOLERANCE);

        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalMastLogic(source);
        assertNotNull(sml);
        JUnitUtil.waitFor(() -> sml.getAutoTurnouts(cont).contains(turnout) && sml.getAutoTurnouts(div).contains(turnout),
            "routes set up from the Layout Editor");
        assertEquals(Turnout.THROWN, sml.getAutoTurnoutState(turnout, cont));
        assertEquals(Turnout.CLOSED, sml.getAutoTurnoutState(turnout, div));

        assertEquals(turnout.getStraightLimit(), sml.getMaximumSpeed(cont), TOLERANCE, "continuing route speed");
        assertEquals(turnout.getDivergingLimit(), sml.getMaximumSpeed(div), TOLERANCE, "diverging route speed");

        turnout.setCommandedState(Turnout.THROWN);
        JUnitUtil.waitFor(() -> sml.isActive(cont), "continuing route active");
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "Approach on the continuing route");

        turnout.setCommandedState(Turnout.CLOSED);
        JUnitUtil.waitFor(() -> sml.isActive(div), "diverging route active");
        JUnitUtil.waitFor(() -> "Diverging Approach".equals(source.getAspect()), "Diverging Approach on the diverging route");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.closeAllPanels();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
