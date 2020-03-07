package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ImportRosterEngineActionTest extends OperationsTestCase {

    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(3);  // allow 3 retries

    @Rule // This test class was periodically stalling and causing the CI run to time out. Limit its duration.
    public org.junit.rules.Timeout globalTimeout = org.junit.rules.Timeout.seconds(20);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ImportRosterEngineAction t = new ImportRosterEngineAction("Test Action");
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testFailedImport() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction("Test Action");
        Assert.assertNotNull("exists", importRosterAction);
        importRosterAction.actionPerformed(new ActionEvent("Test Action", 0, null));

        Thread run = JUnitUtil.getThreadByName("Import Roster Engines");

        jmri.util.JUnitUtil.waitFor(() -> {
            return run.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ImportFailed"), Bundle.getMessage("ButtonOK"));

        if (run != null) {
            try {
                run.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    @Test
    public void testImport() throws IOException, FileNotFoundException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // copied from "RosterTest"
        // store files in random temp directory
        File rosterDir = folder.newFolder();
        FileUtil.createDirectory(rosterDir);

        File f = new File(rosterDir, "rosterTest.xml");
        // File should never be there is TemporaryFolder working
        if (f.exists()) {
            Assert.fail("rosterTest.xml in " + rosterDir + " already present: " + f);
        }

        // create a roster with known contents
        Roster r = Roster.getDefault();
        r.setRosterLocation(rosterDir.getAbsolutePath());
        r.setRosterIndexFileName("rosterTest.xml");

        RosterEntry e1 = new RosterEntry("file name Bob");
        e1.setId("Bob");
        e1.setDccAddress("123");
        e1.setRoadNumber("123");
        e1.setRoadName("SP");
        e1.ensureFilenameExists();
        e1.putAttribute("key a", "value a");
        e1.putAttribute("key b", "value b");
        r.addEntry(e1);

        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction("Test Action");
        Assert.assertNotNull("exists", importRosterAction);
        importRosterAction.actionPerformed(new ActionEvent("Test Action", 0, null));

        Thread run = JUnitUtil.getThreadByName("Import Roster Engines");

        jmri.util.JUnitUtil.waitFor(() -> {
            return run.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");

        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));

        if (run != null) {
            try {
                run.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        Engine e = InstanceManager.getDefault(EngineManager.class).getByRoadAndNumber("SP", "123");
        Assert.assertNotNull(e);
    }

    // private final static Logger log = LoggerFactory.getLogger(ImportRosterEngineActionTest.class);
}
