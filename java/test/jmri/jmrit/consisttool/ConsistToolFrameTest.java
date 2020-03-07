package jmri.jmrit.consisttool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.GraphicsEnvironment;
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
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 * Test simple functioning of ConsistToolFrame
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class ConsistToolFrameTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testCtorWithCSpossible() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // overwrite the default consist manager set in setUp for this test
        // so that we can check initilization with CSConsists possible.
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager() {
            @Override
            public boolean isCommandStationConsistPossible() {
                return true;
            }
        });

        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        Assert.assertFalse("Consists has at least one entry", InstanceManager.getDefault(ConsistManager.class).getConsistList().isEmpty());
        Assert.assertTrue("Consists exists after add", InstanceManager.getDefault(ConsistManager.class).getConsistList().contains(conAddr));
        // delete the consist
        cs.pushDeleteWithDismiss();
        Assert.assertFalse("Consists removed after delete", InstanceManager.getDefault(ConsistManager.class).getConsistList().contains(conAddr));
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
    }

    @Test
    public void testReverseButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        Assert.assertEquals("12 position before reverse", jmri.Consist.POSITION_LEAD, c.getPosition(addr12));
        Assert.assertNotEquals("13 position before reverse", jmri.Consist.POSITION_LEAD, c.getPosition(addr13));
        cs.pushReverseButton();
        Assert.assertNotEquals("12 position after reverse", jmri.Consist.POSITION_LEAD, c.getPosition(addr12));
        Assert.assertEquals("13 position after reverse", jmri.Consist.POSITION_LEAD, c.getPosition(addr13));
        // delete the consist
        cs.pushDeleteWithDismiss();
        Assert.assertFalse("Consists removed after delete", InstanceManager.getDefault(ConsistManager.class).getConsistList().contains(conAddr));
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
    }

    @Test
    public void testRestoreButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        Assert.assertEquals("consist written twice", 2 * preRestoreCalls,
                ((TestConsistManager) InstanceManager.getDefault(ConsistManager.class)).addCalls);

        // delete the consist
        cs.pushDeleteWithDismiss();
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
    }

    @Test
    public void testThrottle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        ThrottleOperator to = new ThrottleOperator("12(S)");
        Assert.assertEquals("Throttle has right visible address",
                new DccLocoAddress(12, false),
                to.getAddressValue());
        Assert.assertEquals("Throttle has right consist address",
                new DccLocoAddress(1, false),
                to.getConsistAddressValue());
        to.pushReleaseButton();
        to.requestClose();

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
    }

    @Test
    public void testDeleteNoConsistAddress() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        cs.pushDeleteButton();
        // this should trigger a warning dialog, which we want to dismiss.
        JemmyUtil.pressDialogButton("Message", "OK");
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
    }

    @Test
    public void testScanEmptyRoster() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        int numConsists = InstanceManager.getDefault(ConsistManager.class).getConsistList().size();
        cs.startRosterScan();
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
        Assert.assertEquals("No New Consists after scan", numConsists, InstanceManager.getDefault(ConsistManager.class).getConsistList().size());
    }

    @Test
    public void testScanRosterNoConsists() throws IOException, FileNotFoundException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        InstanceManager.setDefault(Roster.class, r);

        ConsistToolFrame frame = new ConsistToolFrame();
        frame.setVisible(true);
        // get a ConsistToolScaffold
        ConsistToolScaffold cs = new ConsistToolScaffold();
        int numConsists = InstanceManager.getDefault(ConsistManager.class).getConsistList().size();
        cs.startRosterScan();
        cs.requestClose();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
        Assert.assertEquals("No New Consists after scan", numConsists, InstanceManager.getDefault(ConsistManager.class).getConsistList().size());
    }

    @Test
    public void testScanRosterWithConsists() throws IOException, FileNotFoundException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Roster r = jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()), "rosterTest.xml");
        InstanceManager.setDefault(Roster.class, r);

        // set the consist address of one of the entries.
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");

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
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for frame tot close
        Assert.assertEquals("1 New Consists after scan", numConsists + 1, InstanceManager.getDefault(ConsistManager.class).getConsistList().size());
    }

    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        jmri.profile.Profile profile = new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE));
        JUnitUtil.resetProfileManager(profile);
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
