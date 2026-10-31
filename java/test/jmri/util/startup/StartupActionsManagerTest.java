package jmri.util.startup;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StartupActionsManagerTest {

    @Test
    public void testCTor() {
        StartupActionsManager t = new StartupActionsManager();
        Assertions.assertNotNull( t, "exists");
    }

    @Test
    public void testSummary() {
        Assertions.assertEquals(java.util.List.of(
                "Start up actions took 11.5 s in all:",
                "    1.5 s  Load table content and panels \"Layout.xml\"",
                "   10.0 s  Pause \"Pause for 10 seconds\""),
            StartupActionsManager.summary(java.util.List.of("Load table content and panels \"Layout.xml\"",
                    "Pause \"Pause for 10 seconds\""),
                java.util.List.of(1_500_000_000L, 10_000_000_000L)));
    }

    /** A start up action that only records that it was performed. */
    private static class Recorded extends AbstractStartupModel {
        final java.util.List<String> log;

        Recorded(String name, java.util.List<String> log) {
            setName(name);
            this.log = log;
        }

        @Override
        public void performAction() {
            log.add(getName());
        }
    }

    @Test
    public void testPerformedActionsSummaryLogged() throws jmri.util.prefs.InitializationException {
        JUnitUtil.resetProfileManager();
        jmri.profile.Profile profile = jmri.profile.ProfileManager.getDefault().getActiveProfile();
        Assertions.assertNotNull(profile);
        java.util.List<String> performed = new java.util.ArrayList<>();
        StartupActionsManager t = new StartupActionsManager();
        t.addAction(new Recorded("first", performed));
        Recorded disabled = new Recorded("not run", performed);
        disabled.setEnabled(false);
        t.addAction(disabled);
        t.addAction(new Recorded("second", performed));
        t.initialize(profile);
        Assertions.assertEquals(java.util.List.of("first", "second"), performed, "in order, enabled only");
        // INFO from StartupActionsManager is kept for this test by tests_lcf.xml
        Assertions.assertNotNull(jmri.util.JUnitAppender.checkForMessageStartingWith("Start up actions took"),
            "total logged");
        // then each action performed, on a line of its own, in order
        for (String name : java.util.List.of("first", "second")) {
            org.apache.logging.log4j.core.LogEvent line = jmri.util.JUnitAppender.checkForMessageStartingWith("");
            String text = (line == null) ? "(nothing logged)" : line.getMessage().getFormattedMessage();
            Assertions.assertTrue(text.endsWith("\"" + name + "\""), name + " on a line of its own, not " + text);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(StartupActionsManagerTest.class);

}
