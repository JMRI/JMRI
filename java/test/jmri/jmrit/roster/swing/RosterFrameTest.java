package jmri.jmrit.roster.swing;

import jmri.*;
import jmri.jmrit.roster.*;
import jmri.util.*;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of RosterFrame
 *
 * @author Paul Bender Copyright (C) 2015, 2016
 */
@jmri.util.junit.annotations.DisabledIfHeadless
@Timeout(20) // This test class was periodically stalling and causing the CI run to time out. Limit its duration.
public class RosterFrameTest {

    private RosterFrame frame = null;

    @Test
    public void testCtor() {
        Assertions.assertNotNull( frame, "exists");
    }

    @Test
    public void testIdentifyEnabled() {
        ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Assertions.assertTrue( operator.isIdentifyButtonEnabled(), "Identify Button Enabled");
    }

    @Test
    public void testIdentify3NotPresent() {
        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());

        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger) InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        Assertions.assertNotNull(prog);
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);

        operator.pushIdentifyButton();
        // and wait for message that will come at end because nothing is found
        JUnitUtil.waitFor(() -> {
            return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
        }, "error message at end");

    }

    @Test
    public void testIdentify3Present() {

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry");
        re1.setDccAddress("3");
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry");
        re3.setDccAddress("5");
        roster.addEntry(re3);

        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());

        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger) InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        Assertions.assertNotNull(prog);
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);

        operator.pushIdentifyButton();

        JUnitUtil.waitFor(() -> {
            return frame.getSelectedRosterEntries().length == 1;
        }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assertions.assertEquals( 1, selected.length, "selected ");
        Assertions.assertEquals( re1, selected[0], "selected ");

    }

    @Test
    public void testIdentify3WithDecoderTypeMismatch() {
        // match on address if unique, even if decoder type not right

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry is 3; mismatched types accepted");
        re1.setDccAddress("3");
        re1.setDecoderModel("Four Function Dual Mode");  // CV8 = 127 Atlas, CV7 = 46
        re1.setDecoderFamily("Four Function Dual Mode");
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry");
        re3.setDccAddress("5");
        roster.addEntry(re3);

        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());

        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger) InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        Assertions.assertNotNull(prog);
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
        prog.resetCv(7, 45); // Dual Mode (not Four Function Dual Mode)
        prog.resetCv(8, 127); // Atlas

        operator.pushIdentifyButton();
        // and wait for message that will come at end:
        //JUnitUtil.waitFor(() ->{
        //        return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
        //    }, "error message at end");

        JUnitUtil.waitFor(() -> {
            return frame.getSelectedRosterEntries().length == 1;
        }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assertions.assertEquals( re1, selected[0], "selected ");
        Assertions.assertTrue( frame.checkIfEntrySelected(), "entry selected ");

    }

    @Test
    @Disabled("RosterFrame doesn't do multiple selection properly yet")
    public void testIdentify3Multiple() {

        // this is a test of what happens when multiples are selectable

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry is 3");
        re1.setDccAddress("3");
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry is not 3");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry is 3");
        re3.setDccAddress("3");
        roster.addEntry(re3);

        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());

        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger) InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        Assertions.assertNotNull(prog);
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);

        operator.pushIdentifyButton();

        // right now, nothing is ever selected, because multiple selection
        // is not working.  See @Ignore above
        JUnitUtil.waitFor(() -> {
            return frame.getSelectedRosterEntries().length == 2;
        }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assertions.assertEquals( re1, selected[0], "selected ");
        Assertions.assertEquals( re3, selected[1], "selected ");
        Assertions.assertTrue( frame.checkIfEntrySelected(), "entry selected");

    }

    @Test
    public void testIdentify3ViaDecoderId() {

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry is 3 Four Function Dual Mode");
        re1.setDccAddress("3");
        re1.setDecoderModel("Four Function Dual Mode");  // CV8 = 127 Atlas, CV7 = 46
        re1.setDecoderFamily("Four Function Dual Mode");
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry in not 3");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry is 3 Dual Mode");
        re3.setDccAddress("3");
        re3.setDecoderModel("Dual Mode");  // CV8 = 127 Atlas, CV7 = 45
        re3.setDecoderFamily("Dual Mode");
        roster.addEntry(re3);

        ThreadingUtil.runOnGUI( () -> {
            frame.pack();
            frame.setVisible(true);
        });
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());

        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger) InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        Assertions.assertNotNull(prog);
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
        prog.resetCv(7, 45); // Dual Mode (not Four Function Dual Mode)
        prog.resetCv(8, 127); // Atlas

        operator.pushIdentifyButton();

        JUnitUtil.waitFor(() -> {
            return frame.getSelectedRosterEntries().length == 1;
        }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assertions.assertEquals( 1, selected.length, "selected "); // failed here with 0
        Assertions.assertEquals( re3, selected[0], "selected ");  // 2nd address=3 selected by decoder match
        Assertions.assertTrue( frame.checkIfEntrySelected(), "entry selected");

    }

    @Test
    public void testCheckIfEntrySelected() {

        ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Thread t = new Thread(() -> {
            jmri.util.swing.JemmyUtil.confirmJOptionPane(operator, "Message", "", "OK");
        });
        t.setName("Error Dialog Close Thread");
        t.start();
        // the return true case happens in the identify methods above, so
        // we only check the return false case here.
        Assertions.assertFalse( frame.checkIfEntrySelected(), "entry not selected");
        JUnitUtil.waitThreadTerminated(t);
    }

    @Test
    public void testGetandSetAllowQuit() {
        ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        frame.allowQuit(false);
        Assertions.assertFalse( frame.isAllowQuit(), "Quit Not Allowed");
        frame.allowQuit(true);
        Assertions.assertTrue( frame.isAllowQuit(), "Quit Allowed");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initGuiLafPreferencesManager();
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        InstanceManager.setDefault(jmri.jmrit.symbolicprog.ProgrammerConfigManager.class, new jmri.jmrit.symbolicprog.ProgrammerConfigManager());
        JUnitUtil.initDebugProgrammerManager();
        JUnitUtil.initRosterConfigManager();
        Roster.getDefault(); // ensure exists
        frame = new RosterFrame();

    }

    @AfterEach
    public void tearDown() {
        if (frame != null) {
            JUnitUtil.dispose(frame);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RosterFrameTest.class);
}
