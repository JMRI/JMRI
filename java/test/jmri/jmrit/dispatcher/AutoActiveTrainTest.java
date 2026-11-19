package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.Transit;
import jmri.Turnout;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.mockito.Mockito;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoActiveTrainTest {

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCTor() {

        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class,df);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit,"Train",ActiveTrain.USER);
        AutoActiveTrain t = new AutoActiveTrain(at);
        Assertions.assertNotNull(t, "exists");
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testScaleCapFallbackWarnWhenNoProfile() {

        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.ROSTER);
        RosterEntry entry = new RosterEntry();
        entry.setId("noProfile");
        at.setRosterEntry(entry);

        AutoActiveTrain t = new AutoActiveTrain(at);
        t.setMaxSpeedScaleKmh(36.0f);

        t.warnScaleCapNotApplicable("unit test", true);
        JUnitAppender.assertWarnMessage(
                "Train Train: scale speed cap 36.0 km/h cannot be applied when running forward"
                + " (called from unit test): roster entry noProfile has no speed profile;"
                + " using throttle % cap 1.0 instead");

        // only once per direction
        int backlogAfterFirst = JUnitAppender.getBacklog().size();
        t.warnScaleCapNotApplicable("unit test", true);
        Assertions.assertEquals(backlogAfterFirst, JUnitAppender.getBacklog().size(),
                "no second warning for the same direction");
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testScaleCapFallbackWarnWhenProfileIsForwardOnly() {

        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.ROSTER);
        RosterEntry entry = new RosterEntry();
        entry.setId("fwdOnly");
        RosterSpeedProfile sp = new RosterSpeedProfile(entry);
        sp.setForwardSpeed(1.0f, 100.0f);
        entry.setSpeedProfile(sp);
        at.setRosterEntry(entry);

        AutoActiveTrain t = new AutoActiveTrain(at);
        t.setMaxSpeedScaleKmh(36.0f);

        // forward is covered by the profile: no warning
        int backlogBeforeForward = JUnitAppender.getBacklog().size();
        t.warnScaleCapNotApplicable("unit test", true);
        Assertions.assertEquals(backlogBeforeForward, JUnitAppender.getBacklog().size(),
                "forward direction resolves; no warning expected");

        // reverse is not covered: warn once, naming the forward-only profile
        t.warnScaleCapNotApplicable("unit test", false);
        JUnitAppender.assertWarnMessage(
                "Train Train: scale speed cap 36.0 km/h cannot be applied when running reverse"
                + " (called from unit test): roster entry fwdOnly has a forward-only speed profile"
                + " that does not cover this direction; using throttle % cap 1.0 instead");
    }

    // checkTurn: waiting for a route's turnouts to finish setting

    private AutoActiveTrain waitingTrain() {
        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);
        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        return new AutoActiveTrain(at);
    }

    private AllocatedSection sectionNeedingTurnouts() {
        AllocatedSection as = Mockito.mock(AllocatedSection.class);
        Mockito.when(as.getAutoTurnoutsResponse()).thenReturn(new java.util.ArrayList<>());
        return as;
    }

    private static int knownStateListeners(Turnout to) {
        return to.getPropertyChangeListeners("KnownState").length;
    }

    @Test
    public void testDisposeStopsWaitingForTurnout() {
        AutoActiveTrain t = waitingTrain();
        Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        int before = knownStateListeners(to);
        AutoTurnouts helper = Mockito.mock(AutoTurnouts.class);
        Mockito.when(helper.checkStateAgainstList(Mockito.any())).thenReturn(to);

        Assertions.assertFalse(t.checkTurn(sectionNeedingTurnouts(), helper), "turnout not yet set");
        Assertions.assertEquals(to, t.getTurnoutWaitedFor());
        Assertions.assertEquals(before + 1, knownStateListeners(to), "listening to the turnout");

        t.dispose();
        Assertions.assertEquals(before, knownStateListeners(to), "no longer listening after dispose");
        // the turnout setting later must not reach the disposed train (it used to throw NullPointerException)
        to.setCommandedState(Turnout.THROWN);
        Assertions.assertNull(t.getTurnoutWaitedFor());
    }

    @Test
    public void testTurnoutSetBeforeListenerAddedIsNotMissed() {
        AutoActiveTrain t = waitingTrain();
        Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        int before = knownStateListeners(to);
        AutoTurnouts helper = Mockito.mock(AutoTurnouts.class);
        // not set at the first check, set by the time the listener is in place
        Mockito.when(helper.checkStateAgainstList(Mockito.any())).thenReturn(to, (Turnout) null);

        Assertions.assertTrue(t.checkTurn(sectionNeedingTurnouts(), helper), "the turnout finished setting");
        Assertions.assertNull(t.getTurnoutWaitedFor(), "not waiting for a turnout that is set");
        Assertions.assertEquals(before, knownStateListeners(to), "not listening to it");
    }

    @Test
    public void testTurnoutChangeStopsWaiting() {
        AutoActiveTrain t = waitingTrain();
        Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        int before = knownStateListeners(to);
        AutoTurnouts helper = Mockito.mock(AutoTurnouts.class);
        Mockito.when(helper.checkStateAgainstList(Mockito.any())).thenReturn(to);

        Assertions.assertFalse(t.checkTurn(sectionNeedingTurnouts(), helper), "turnout not yet set");
        to.setCommandedState(Turnout.THROWN);
        Assertions.assertNull(t.getTurnoutWaitedFor(), "no longer waiting");
        Assertions.assertEquals(before, knownStateListeners(to), "no longer listening");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(AutoActiveTrainTest.class);


    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCanStopIn() {
        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, df);
        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setUseStopSensor(true);
        AutoActiveTrain t = new AutoActiveTrain(at);
        t.setMaxTrainLength(1.0, 1.0); // 1000 mm

        jmri.Section shortSection = Mockito.mock(jmri.Section.class);
        Mockito.when(shortSection.getActualLength()).thenReturn(160);
        Assertions.assertFalse(t.canStopIn(shortSection, jmri.Section.FORWARD),
                "shorter than the train, no stopping sensor");

        jmri.Section longSection = Mockito.mock(jmri.Section.class);
        Mockito.when(longSection.getActualLength()).thenReturn(1230);
        Assertions.assertTrue(t.canStopIn(longSection, jmri.Section.FORWARD), "the train fits");

        jmri.Section unknownLength = Mockito.mock(jmri.Section.class);
        Assertions.assertTrue(t.canStopIn(unknownLength, jmri.Section.FORWARD), "length not known");

        jmri.Sensor stop = Mockito.mock(jmri.Sensor.class);
        Mockito.when(shortSection.getReverseStoppingSensor()).thenReturn(stop);
        Assertions.assertTrue(t.canStopIn(shortSection, jmri.Section.REVERSE),
                "stopped by the stopping sensor for its direction");
        Assertions.assertFalse(t.canStopIn(shortSection, jmri.Section.FORWARD),
                "the other direction's sensor does not help");
        at.setUseStopSensor(false);
        Assertions.assertFalse(t.canStopIn(shortSection, jmri.Section.REVERSE),
                "stopping sensors not used by this train");

        t.setUseSpeedProfile(true);
        t.setStopBySpeedProfile(true);
        Assertions.assertTrue(t.canStopIn(shortSection, jmri.Section.FORWARD), "stopped by speed profile");

        t.setUseSpeedProfile(false);
        t.setMaxTrainLength(0.0, 1.0);
        Assertions.assertTrue(t.canStopIn(shortSection, jmri.Section.FORWARD), "train length not known");
    }

}
