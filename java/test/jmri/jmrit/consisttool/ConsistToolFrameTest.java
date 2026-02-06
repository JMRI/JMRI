package jmri.jmrit.consisttool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.throttle.ThrottleOperator;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test simple functioning of ConsistToolFrame
 *
 * @author Paul Bender Copyright (C) 2015,2016
 */
@DisabledIfHeadless
public class ConsistToolFrameTest {

    @Test
    public void testCtor() {
        ConsistToolFrame frame = new ConsistToolFrame();
        assertNotNull(frame, "exists");
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testCtorWithCSpossible() {
        // overwrite the default consist manager set in setUp for this test
        // so that we can check initialization with CSConsists possible.
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager() {
            @Override
            public boolean isCommandStationConsistPossible() {
                return true;
            }
        });

        ConsistToolFrame frame = new ConsistToolFrame();
        assertNotNull(frame, "exists");
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testAdd() {
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        //Assert.assertTrue("Consists List empty",InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist.
        cs.setConsistAddressValue("1");
        cs.setLocoAddressValue("12");
        cs.pushAddButton();
        // check to see if a conist was added
        DccLocoAddress conAddr = new DccLocoAddress(1, false);
        assertFalse(InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty(),
            "Consists has at least one entry");
        assertTrue(InstanceManager.getDefault(ConsistManager.class).getConsistList().contains(conAddr),
            "Consists exists after add");
        // delete the consist
        cs.pushDeleteWithDismiss();
        assertFalse(InstanceManager.getDefault(ConsistManager.class).getConsistList().contains(conAddr),
            "Consists removed after delete");
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
    }

    @Test
    public void testReverseButton() {
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist with two addresses.
        cs.setConsistAddressValue("1");
        cs.setLocoAddressValue("12");
        cs.pushAddButton();
        cs.setLocoAddressValue("13");
        cs.pushAddButton();
        DccLocoAddress conAddr = new DccLocoAddress(1, false);
        Consist c = InstanceManager.getDefault(ConsistManager.class).getConsist(conAddr);
        DccLocoAddress addr12 = new DccLocoAddress(12, false);
        DccLocoAddress addr13 = new DccLocoAddress(13, false);
        assertEquals(Consist.POSITION_LEAD, c.getPosition(addr12), "12 position before reverse");
        assertNotEquals(Consist.POSITION_LEAD, c.getPosition(addr13), "13 position before reverse");
        cs.pushReverseButton();
        assertNotEquals(Consist.POSITION_LEAD, c.getPosition(addr12), "12 position after reverse");
        assertEquals(Consist.POSITION_LEAD, c.getPosition(addr13), "13 position after reverse");
        // delete the consist
        cs.pushDeleteWithDismiss();
        assertFalse(InstanceManager.getDefault(ConsistManager.class).getConsistList().contains(conAddr),
            "Consists removed after delete");
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
    }

    @Test
    public void testRestoreButton() {
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist with two addresses.
        cs.setConsistAddressValue("1");
        cs.setLocoAddressValue("12");
        cs.pushAddButton();
        cs.setLocoAddressValue("13");
        cs.pushAddButton();
        int preRestoreCalls
                = ((TestConsistManager) InstanceManager.getDefault(ConsistManager.class)).addCalls;
        // referesh the consist
        cs.pushRestoreButton();
        // need to check that the consist was "written" again.
        assertEquals(2 * preRestoreCalls,
                ((TestConsistManager) InstanceManager.getDefault(ConsistManager.class)).addCalls,
                "consist written twice");

        // delete the consist
        cs.pushDeleteWithDismiss();
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
    }

    @Test
    public void testThrottle() {
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        // set up a consist with one addresses.
        cs.setConsistAddressValue("1");
        cs.setLocoAddressValue("12");
        cs.pushAddButton();
        cs.pushThrottleButton();
        // need to verify throttle is setup with two addresses.

        ThrottleOperator to = new ThrottleOperator("1(S)");
        assertEquals(new DccLocoAddress(12, false),
                to.getAddressValue(), "Throttle has right visible address");
        assertEquals(new DccLocoAddress(1, false),
                to.getConsistAddressValue(), "Throttle has right consist address");

        to.pushReleaseButton();
        to.getQueueTool().waitEmpty();  // pause for Throttle to release

        to.requestClose();
        to.getQueueTool().waitEmpty();  // pause for frame to close

        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
    }

    @Test
    public void testDeleteNoConsistAddress() {
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        cs.pushDeleteButton();
        // this should trigger a warning dialog, which we want to dismiss.
        JemmyUtil.pressDialogButton("Message", "OK");
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
    }

    @Test
    public void testScanEmptyRoster() {
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        int numConsists = InstanceManager.getDefault(ConsistManager.class).getConsistList().size();
        cs.startRosterScan();
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
        assertEquals(numConsists, InstanceManager.getDefault(ConsistManager.class).getConsistList().size(),
            "No New Consists after scan");
    }

    @Test
    public void testScanRosterNoConsists() throws IOException, FileNotFoundException {
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        InstanceManager.setDefault(Roster.class, r);

        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        int numConsists = InstanceManager.getDefault(ConsistManager.class).getConsistList().size();
        cs.startRosterScan();
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
        assertEquals(numConsists, InstanceManager.getDefault(ConsistManager.class).getConsistList().size(),
            "No New Consists after scan");
    }

    @Test
    public void testScanRosterWithConsists() throws IOException, FileNotFoundException {
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        InstanceManager.setDefault(Roster.class, r);

        // set the consist address of one of the entries.
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);

        CvTableModel cvTable = new CvTableModel(null, null);  // will hold CV objects
        VariableTableModel varTable = new VariableTableModel(null, new String[]{"Name", "Value"}, cvTable);
        entry.readFile();  // read, but don’t yet process

        // load from decoder file
        jmri.util.RosterTestUtil.loadDecoderFromLoco(entry, varTable);

        entry.loadCvModel(varTable, cvTable);
        CvValue cv19Value = cvTable.getCvByNumber("19");
        cv19Value.setValue(0x02);

        entry.writeFile(cvTable, varTable);

        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        int numConsists = InstanceManager.getDefault(ConsistManager.class).getConsistList().size();
        cs.startRosterScan();
        cs.requestClose();
        cs.getQueueTool().waitEmpty();  // pause for frame to close
        assertEquals(numConsists + 1, InstanceManager.getDefault(ConsistManager.class).getConsistList().size(),
            "1 New Consists after scan");
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        jmri.profile.Profile profile = new jmri.profile.NullProfile(folder);
        JUnitUtil.resetProfileManager(profile);
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
