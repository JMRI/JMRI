package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(20)
public class ImportRosterEngineActionTest extends OperationsTestCase {

    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ImportRosterEngineAction t = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testFailedImport() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", importRosterAction);
        importRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

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
    public void testImport(@TempDir File rosterDir) throws IOException, FileNotFoundException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // copied from "RosterTest"
        // store files in random temp directory
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

        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", importRosterAction);
        importRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

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
        
        jmri.util.JUnitAppender.assertWarnMessage("Roster Id: Bob hasn't been assigned a model name");
    }

    // private final static Logger log = LoggerFactory.getLogger(ImportRosterEngineActionTest.class);
}
