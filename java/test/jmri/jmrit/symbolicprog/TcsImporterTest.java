package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for TcsImporter class.
 *
 * @author Bob Jacobsen Copyright 2003, 2023
 */
public class TcsImporterTest {

    VariableTableModel model = new VariableTableModel(null, new String[0], null);

    public File makeTempFile(String contents) throws IOException {
        // create a file
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File f = new java.io.File(FileUtil.getUserFilesPath() + "temp" + File.separator + "PTcsImporter.test.xml");
        // recreate it
        if (f.exists()) {
            Assertions.assertTrue(f.delete());
        }
        try (PrintStream p = new PrintStream(new FileOutputStream(f))) {
            p.print(contents);
        }

        return f;
    }

    @Test
    public void testComment() throws IOException {
        // create a file
        String s =
            "Train.User Description=expected comment";
        File f = makeTempFile(s);

        var importer = new TcsImporter(f, model);

        var rosterEntry = new RosterEntry("no file");

        importer.setRosterEntry(rosterEntry);

        Assert.assertEquals("expected comment", rosterEntry.getComment());
    }

    @Test
    public void testANamedFunction() throws IOException {
        // create a file
        String s =
            "Train.User Description=expected comment\n"
           +"Train.Functions(2).Display=0\n"
           +"Train.Functions(2).Momentary=0\n"
           +"Train.Functions(2).Consist Behavior=1\n"
           +"Train.Functions(2).Description=Short Horn\n";

        File f = makeTempFile(s);

        var importer = new TcsImporter(f, model);

        var rosterEntry = new RosterEntry("no file");

        importer.setRosterEntry(rosterEntry);

        Assert.assertEquals("Short Horn", rosterEntry.getFunctionLabel(3));
        Assert.assertEquals(false, rosterEntry.getFunctionLockable(3));
    }

    @Test
    public void testANumberedFunction() throws IOException {
        // create a file
        String s =
            "Train.User Description=expected comment\n"
           +"Train.Functions(2).Display=14\n"
           +"Train.Functions(2).Momentary=1\n"
           +"Train.Functions(2).Consist Behavior=1\n"
           +"Train.Functions(2).Description=\n";

        File f = makeTempFile(s);

        var importer = new TcsImporter(f, model);

        var rosterEntry = new RosterEntry("no file");

        importer.setRosterEntry(rosterEntry);

        Assert.assertEquals("Horn", rosterEntry.getFunctionLabel(3));
        Assert.assertEquals(true, rosterEntry.getFunctionLockable(3));
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
