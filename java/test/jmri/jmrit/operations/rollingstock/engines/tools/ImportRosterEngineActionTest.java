package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(20)
public class ImportRosterEngineActionTest extends OperationsTestCase {
    
    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCTor() {
        ImportRosterEngineAction t = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", t);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testFailedImport() {
        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", importRosterAction);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(rb.getString("SelectRosterGroup"), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testFailedImport SelectRosterGroup click OK Thread");
        t1.start();
        
        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("ImportFailed"), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("testFailedImport ImportFailed click OK Thread");
        t2.start();
        
        importRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        Thread run = JUnitUtil.getThreadByName("Import Roster Engines");
        
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "SelectRosterGroup click OK did not happen");

        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "ImportFailed click OK did not happen");

        if (run != null) {
            try {
                run.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testImport(@TempDir File rosterDir) throws IOException, FileNotFoundException {

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
        e1.setRoadName("SPABCDEFGHIJKLM"); // string length exceeds Control.max_len_string_attibute
        e1.setModel("NewModelABCDEFGH"); // string length exceeds Control.max_len_string_attibute
        e1.setOwner("OwnerNameABCD"); // string length exceeds Control.max_len_string_attibute
        e1.ensureFilenameExists();
        e1.putAttribute("key a", "value a");
        e1.putAttribute("key b", "value b");
        r.addEntry(e1);

        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", importRosterAction);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(rb.getString("SelectRosterGroup"), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testFailedImport SelectRosterGroup click OK Thread");
        t1.start();
        
        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("testFailedImport SuccessfulImport click OK Thread");
        t2.start();

        importRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        Thread run = JUnitUtil.getThreadByName("Import Roster Engines");

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "SelectRosterGroup click OK did not happen");
        
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "SuccessfulImport click OK did not happen");
        
        if (run != null) {
            try {
                run.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        Engine e = InstanceManager.getDefault(EngineManager.class).getByRoadAndNumber("SPABCDEFGHIJ", "123");
        Assert.assertNotNull(e);

        Assert.assertEquals("model", "NewModelABCD", e.getModel());
        Assert.assertEquals("road", "SPABCDEFGHIJ", e.getRoadName());
        Assert.assertEquals("owner", "OwnerNameABC", e.getOwner());
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testImportNoModel(@TempDir File rosterDir) throws IOException, FileNotFoundException {

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

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(rb.getString("SelectRosterGroup"), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testImportNoModel SelectRosterGroup click OK Thread");
        t1.start();

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("testImportNoModel SuccessfulImport click OK Thread");
        t2.start();

        importRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        Thread run = JUnitUtil.getThreadByName("Import Roster Engines");

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "SelectRosterGroup click OK did not happen");

        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "SuccessfulImport click OK did not happen");

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

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testImportNoRoadNumber(@TempDir File rosterDir) throws IOException, FileNotFoundException {

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
        // no road number
        e1.setRoadName("SP");
        e1.ensureFilenameExists();
        e1.putAttribute("key a", "value a");
        e1.putAttribute("key b", "value b");
        r.addEntry(e1);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(rb.getString("SelectRosterGroup"), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("testImportNoRoadNumber SelectRosterGroup click OK Thread");
        t1.start();

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("ImportFailed"), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("testImportNoRoadNumber ImportFailed click OK Thread");
        t2.start();

        ImportRosterEngineAction importRosterAction = new ImportRosterEngineAction();
        Assert.assertNotNull("exists", importRosterAction);
        importRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        Thread run = JUnitUtil.getThreadByName("Import Roster Engines");

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "SelectRosterGroup click OK did not happen");

        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "ImportFailed click OK did not happen");

        if (run != null) {
            try {
                run.join();
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        jmri.util.JUnitAppender.assertErrorMessage("Roster Id: Bob doesn't have a road name and road number");
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(ImportRosterEngineActionTest.class);
}
