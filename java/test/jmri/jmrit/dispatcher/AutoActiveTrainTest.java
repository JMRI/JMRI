package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.Transit;
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(AutoActiveTrainTest.class);

}
