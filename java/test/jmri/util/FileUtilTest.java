package jmri.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import jmri.profile.ProfileManager;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the jmri.util.FileUtil class.
 *
 * Most tests use a deliberately non-existent filename since FileUtil appends a
 * / to the end of a portable directory name, and tests could fail if they
 * expect a file or non-existent filename and a directory exists at that path.
 *
 * These tests should return the same results as
 * {@link jmri.util.FileUtilSupportTest}.
 *
 * @author Bob Jacobsen Copyright 2003, 2009
 * @author Randall Wood Copyright 2016, 2017
 */
public class FileUtilTest {

    private File programTestFile;
    private File preferencesTestFile;

    // tests of internal to external mapping
    // relative file with no prefix: Leave relative in system-specific form
    @Test
    public void testGEFRel() {
        String name = FileUtil.getExternalFilename("resources/non-existant-file-foo");
        assertEquals("resources" + File.separator + "non-existant-file-foo", name);
    }

    // relative file with no prefix: Leave relative in system-specific form
    @Test
    public void testGEFAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename(f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    @Test
    public void testGEFProgramRel() {
        String name = FileUtil.getExternalFilename("program:jython");
        assertEquals(new File("jython").getAbsolutePath(), name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGEFProgramAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("program:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGEFPrefRel() {
        JUnitUtil.resetProfileManager();
        String name = FileUtil.getExternalFilename("preference:non-existant-file-foo");
        assertEquals(FileUtil.getUserFilesPath() + "non-existant-file-foo", name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGEFPrefAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("preference:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGEFHomeRel() {
        String name = FileUtil.getExternalFilename("home:non-existant-file-foo");
        assertEquals(System.getProperty("user.home") + File.separator + "non-existant-file-foo", name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGEFHomeAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("home:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // tests of external to internal mapping
    @Test
    public void testGetpfPreferenceF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        assertEquals("preference:non-existant-file-foo", name);
    }

    @Test
    public void testGetpfPreferenceS() {
        JUnitUtil.resetProfileManager();
        String name = FileUtil.getPortableFilename("preference:non-existant-file-foo");
        assertEquals("preference:non-existant-file-foo", name);
    }

    @Test
    public void testGetpfResourceF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        assertEquals("preference:resources/non-existant-file-foo", name);
    }

    @Test
    public void testGetpfPrefF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        assertEquals("preference:resources/non-existant-file-foo", name);
    }

    @Test
    public void testGetpfProgramF() throws IOException {
        File f = new File("resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        assertEquals("program:resources/non-existant-file-foo", name);
    }

    @Test
    public void testGetpfProgramS() {
        String name = FileUtil.getPortableFilename("program:resources/non-existant-file-foo");
        assertEquals("program:resources/non-existant-file-foo", name);
    }

    /*
     * Test a real directory. It should end with a separator.
     */
    @Test
    public void testGetpfProgramDirS() {
        String name = FileUtil.getPortableFilename("program:resources/icons");
        assertEquals("program:resources/icons/", name);
    }

    @Test
    public void testGetpfHomeS() {
        String name = FileUtil.getPortableFilename("home:non-existant-file-foo");
        assertEquals("home:non-existant-file-foo", name);
    }

    @Test
    public void testGetpfHomeF() throws IOException {
        File f = new File(System.getProperty("user.home") + File.separator + "resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        assertEquals("home:resources/non-existant-file-foo", name);
    }

    /*
     * test getAbsoluteFilename()
     */
    // relative file with no prefix: Should become null
    @Test
    public void testGAFRel() {
        String name = FileUtil.getAbsoluteFilename("resources/non-existant-file-foo");
        assertNull( name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    @Test
    public void testGAFProgRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.PROGRAM + "jython");
        assertEquals(new File("jython").getCanonicalPath(), name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGAFProgAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(FileUtil.PROGRAM + f.getAbsolutePath());
        assertEquals(f.getCanonicalPath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGAFPrefRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + "non-existant-file-foo");
        assertEquals(new File(FileUtil.getUserFilesPath() + "non-existant-file-foo").getCanonicalPath(), name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGAFPrefAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + f.getAbsolutePath());
        assertEquals(f.getCanonicalPath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGAFHomeRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.HOME + "non-existant-file-foo");
        assertEquals(new File(System.getProperty("user.home") + File.separator + "non-existant-file-foo").getCanonicalPath(), name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGAFHomeAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(FileUtil.HOME + f.getAbsolutePath());
        assertEquals(f.getCanonicalPath(), name);
    }

    @Test
    public void testCopyFile() throws FileNotFoundException {
        File src = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:default_lcf.xml"));
        File dest = new File(FileUtil.getAbsoluteFilename("program:fileUtilTest_lcf.xml"));
        ArrayList<String> sl = new ArrayList<>();
        ArrayList<String> dl = new ArrayList<>();
        try {
            FileUtil.copy(src, dest);
            Scanner s = new Scanner(src);
            while (s.hasNext()) {
                sl.add(s.next());
            }
            s.close();
            s = new Scanner(dest);
            while (s.hasNext()) {
                dl.add(s.next());
            }
            s.close();
        } catch (IOException ex) {
            FileUtil.delete(dest);
            fail("Unable to copy", ex);
        }
        FileUtil.delete(dest);
        assertTrue(sl.equals(dl));
    }

    @Test
    public void testCopyDirectoryToExistingDirectory() throws FileNotFoundException, IOException {
        File src = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:web/fonts"));
        File dest = Files.createTempDirectory("FileUtilTest").toFile();
        Assertions.assertNotNull(src);
        Assertions.assertNotNull(dest);
        FileUtil.copy(src, dest);
        String[] destFiles = dest.list();
        String[] srcFiles = src.list();
        Arrays.sort(destFiles);
        Arrays.sort(srcFiles);
        FileUtil.delete(dest);
        assertArrayEquals(srcFiles, destFiles);
    }

    @Test
    public void testDeleteFile() throws IOException {
        File file = File.createTempFile("FileUtilTest", null);
        FileUtil.copy(FileUtil.getFile(FileUtil.getAbsoluteFilename("program:default_lcf.xml")), file);
        FileUtil.delete(file);
        assertFalse(file.exists());
    }

    @Test
    public void testAppendTextToFile() throws IOException {
        File file = File.createTempFile("FileUtilTest", null);
        String text = "jmri.util.FileUtil#appendTextToFile";
        FileUtil.appendTextToFile(file, text);
        List<String> lines = Files.readAllLines(Paths.get(file.toURI()), StandardCharsets.UTF_8);
        assertEquals(text, lines.get(0));
    }

    @Test
    public void testFindURIPath() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findURI(this.programTestFile.getName()));
        assertEquals(uri, FileUtil.findURI(FileUtil.PROGRAM + this.programTestFile.getName()));
        assertNull(FileUtil.findURI(FileUtil.PREFERENCES + this.programTestFile.getName()));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName()));
        assertEquals(uri, FileUtil.findURI(FileUtil.PREFERENCES + this.preferencesTestFile.getName()));
        assertNull(FileUtil.findURI(FileUtil.PROGRAM + this.preferencesTestFile.getName()));
    }

    @Test
    public void testFindURIPathLocation() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), FileUtil.Location.INSTALLED));
        assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), FileUtil.Location.ALL));
        assertNull(FileUtil.findURI(this.programTestFile.getName(), FileUtil.Location.USER));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), FileUtil.Location.USER));
        assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), FileUtil.Location.ALL));
        assertNull(FileUtil.findURI(this.preferencesTestFile.getName(), FileUtil.Location.INSTALLED));
    }

    @Test
    public void testFindURIPathSearchPaths() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), new String[]{FileUtil.getProgramPath()}));
        assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), new String[]{FileUtil.getPreferencesPath(), FileUtil.getProgramPath()}));
        assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), new String[]{FileUtil.getPreferencesPath()}));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), new String[]{FileUtil.getPreferencesPath()}));
        assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), new String[]{FileUtil.getPreferencesPath(), FileUtil.getProgramPath()}));
        assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), new String[]{FileUtil.getProgramPath()}));
    }

    @Test
    public void testFindExternalFilename() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findExternalFilename(this.programTestFile.getName()));
        assertEquals(uri, FileUtil.findExternalFilename(FileUtil.PROGRAM + this.programTestFile.getName()));
        assertNull(FileUtil.findExternalFilename(FileUtil.PREFERENCES + this.programTestFile.getName()));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, FileUtil.findExternalFilename(this.preferencesTestFile.getName()));
        assertEquals(uri, FileUtil.findExternalFilename(FileUtil.PREFERENCES + this.preferencesTestFile.getName()));
        assertNull(FileUtil.findExternalFilename(FileUtil.PROGRAM + this.preferencesTestFile.getName()));
    }

    @Test
    public void testGetpfScriptsS() {
        String name = FileUtil.getPortableFilename("scripts:non-existant-file-foo");
        assertEquals("scripts:non-existant-file-foo", name);
    }

    @Test
    public void testGEFScriptsAbs() {

        File f = new File("jython/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("scripts:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);

    }

    @Test
    public void testLocateActualJythonFile() {

        File actualFile = null;
        try {
            actualFile = FileUtil.getFile(FileUtil.getProgramPath()+"jython/BackAndForth.py");
        }
        catch ( FileNotFoundException ex ) {
            fail("jython/BackAndForth.py not found, ", ex);
        }

        assertNotNull(actualFile);
        assertEquals("scripts:BackAndForth.py", FileUtil.getPortableFilename(actualFile));

        URI toFind = FileUtil.findURI("jython/BackAndForth.py");
        assertNotNull(toFind);
        assertFalse(toFind.getPath().isBlank());
 
    }

    @Test
    public void testLocateActualJythonByScripts() {

        File actualFile = null;
        try {
            actualFile = FileUtil.getFile("scripts:BackAndForth.py");
        }
        catch ( FileNotFoundException ex ) {
            fail("scripts:BackAndForth.py not found, ", ex);
        }
        assertNotNull(actualFile);

        URI toFind = FileUtil.findURI("scripts:BackAndForth.py");
        assertNotNull(toFind);
        assertFalse(toFind.getPath().isBlank());
        assertTrue(toFind.getPath().contains("jython"));

    }

    @Test
    public void testLocateActualJythonByScriptsFail() {
        URI toFind = FileUtil.findURI("scripts:thisisdefinitelynotascript.py");
        assertNull(toFind);
    }

    @Test
    public void testLogixNgExampleTableCsv() {

        String absPath = "Absolute Path Not Found";
        String relativePath = "Relative Path Not Found";

        try {
            File absoluteFile = FileUtil.getFile(FileUtil.getProgramPath() + "/jython/LogixNG/LogixNG_ExampleTable.csv");
            assertNotNull(absoluteFile);
            absPath = absoluteFile.getAbsolutePath();
        }
        catch (FileNotFoundException ex) {
            fail("absolute LogixNG_ExampleTable not found, ", ex);
        }

        try {
            File relativeFile = FileUtil.getFile("scripts:LogixNG/LogixNG_ExampleTable.csv");
            assertNotNull(relativeFile);
            relativePath = relativeFile.getAbsolutePath();
        }
        catch ( FileNotFoundException ex ) {
            fail("relative LogixNG_ExampleTable not found, ", ex);
        }

        assertEquals( absPath, relativePath);
    }

    @Test
    public void testScriptsPathPresent() {

        assertTrue( FileUtil.getScriptsPath().contains("jython"),
            "script path " + FileUtil.getScriptsPath());

        assertDoesNotThrow( () ->
            assertTrue( FileUtil.getFile("scripts:").getAbsolutePath().contains("jython"),
                "scripts: " + FileUtil.getFile("scripts:").getAbsolutePath()),
                "scripts: path does not contain jython"
        );

    }

    @Test
    public void testSetScriptsDirectory() {
        
        // most scripts: tests within this class assume default user profile
        // scripts directory.
        assertTrue( FileUtil.getScriptsPath().contains("jython"),
            "script path " + FileUtil.getScriptsPath());
        
        // though the user can change this,
        FileUtil.setScriptsPath( ProfileManager.getDefault().getActiveProfile(), 
            (FileUtil.getProfilePath() + "myScripts" ) );
        
        assertFalse( FileUtil.getScriptsPath().contains("jython"),"not jython " + FileUtil.getScriptsPath());
        assertEquals(FileUtil.getProfilePath() + "myScripts" + File.separator, FileUtil.getScriptsPath());
        
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {

        JUnitUtil.setUp();
        // The profile is setup with a temp directory to ensure no contamination
        // from previous tests when setting user script directories etc.
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));

        this.programTestFile = new File(UUID.randomUUID().toString());
        assertTrue(this.programTestFile.createNewFile());
        // programTestFile created in JMRI root directory
        JUnitUtil.waitFor(() -> {
            return this.programTestFile.exists();
        }, "Create program test file");
        File profile = new File(FileUtil.getProfilePath());
        assertFalse(profile.mkdir(),"temp directory should already exist");
        this.preferencesTestFile = new File(profile, UUID.randomUUID().toString());
        assertTrue(this.preferencesTestFile.createNewFile());
        JUnitUtil.waitFor(() -> {
            return this.preferencesTestFile.exists();
        }, "Create program test file");
    }

    @AfterEach
    public void tearDown() {
        assertTrue(this.programTestFile.delete());
        JUnitUtil.waitFor(() -> {
            return !this.programTestFile.exists();
        }, "Remove program test file");
        // no need to delete preferencesTestFile as created in temp directory
        JUnitUtil.tearDown();
    }
}
