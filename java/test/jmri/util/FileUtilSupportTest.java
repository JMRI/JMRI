package jmri.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.util.FileUtilSupport class.
 *
 * Most tests use a deliberately non-existent filename since FileUtil appends a
 * / to the end of a portable directory name, and tests could fail if they
 * expect a file or non-existent filename and a directory exists at that path.
 *
 * @author Bob Jacobsen Copyright 2003, 2009
 * @author Randall Wood Copyright 2016, 2017
 */
public class FileUtilSupportTest {

    private File programTestFile;
    private File preferencesTestFile;
    private FileUtilSupport instance;

    // tests of internal to external mapping
    // relative file with no prefix: Leave relative in system-specific form
    @Test
    public void testGEFRel() {
        String name = instance.getExternalFilename("resources/non-existant-file-foo");
        assertEquals("resources" + File.separator + "non-existant-file-foo", name);
    }

    // relative file with no prefix: Leave relative in system-specific form
    @Test
    public void testGEFAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getExternalFilename(f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    @Test
    public void testGEFProgramRel() {
        String name = instance.getExternalFilename("program:jython");
        assertEquals(new File("jython").getAbsolutePath(), name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGEFProgramAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getExternalFilename("program:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGEFPrefRel() {
        String name = instance.getExternalFilename("preference:non-existant-file-foo");
        assertEquals(instance.getUserFilesPath() + "non-existant-file-foo", name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGEFPrefAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getExternalFilename("preference:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGEFHomeRel() {
        String name = instance.getExternalFilename("home:non-existant-file-foo");
        assertEquals(System.getProperty("user.home") + File.separator + "non-existant-file-foo", name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGEFHomeAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getExternalFilename("home:" + f.getAbsolutePath());
        assertEquals(f.getAbsolutePath(), name);
    }

    // tests of external to internal mapping
    @Test
    public void testGetpfPreferenceF() throws IOException {
        File f = new File(instance.getUserFilesPath() + "non-existant-file-foo");
        String name = instance.getPortableFilename(f);
        assertEquals("preference:non-existant-file-foo", name);
    }

    @Test
    public void testGetpfPreferenceS() {
        String name = instance.getPortableFilename("preference:non-existant-file-foo");
        assertEquals("preference:non-existant-file-foo", name);
    }

    @Test
    public void testGetpfResourceF() throws IOException {
        File f = new File(instance.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo");
        String name = instance.getPortableFilename(f);
        assertEquals("preference:resources/non-existant-file-foo", name);
    }

    @Test
    public void testGetpfPrefF() throws IOException {
        File f = new File(instance.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo");
        String name = instance.getPortableFilename(f);
        assertEquals("preference:resources/non-existant-file-foo", name);
    }

    @Test
    public void testGetpfProgramF() throws IOException {
        File f = new File("resources" + File.separator + "non-existant-file-foo");
        String name = instance.getPortableFilename(f);
        assertEquals("program:resources/non-existant-file-foo", name);
    }

    @Test
    public void testGetpfProgramS() {
        String name = instance.getPortableFilename("program:resources/non-existant-file-foo");
        assertEquals("program:resources/non-existant-file-foo", name);
    }

    /*
     * Test a real directory. It should end with a separator.
     */
    @Test
    public void testGetpfProgramDirS() {
        String name = instance.getPortableFilename("program:resources/icons");
        assertEquals("program:resources/icons/", name);
    }

    @Test
    public void testGetpfHomeS() {
        String name = instance.getPortableFilename("home:non-existant-file-foo");
        assertEquals("home:non-existant-file-foo", name);
    }

    @Test
    public void testGetpfHomeF() throws IOException {
        File f = new File(System.getProperty("user.home") + File.separator + "resources" + File.separator + "non-existant-file-foo");
        String name = instance.getPortableFilename(f);
        assertEquals("home:resources/non-existant-file-foo", name);
    }

    /*
     * test getAbsoluteFilename()
     */
    // relative file with no prefix: Should become null
    @Test
    public void testGAFRel() {
        String name = instance.getAbsoluteFilename("resources/non-existant-file-foo");
        assertEquals(null, name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    @Test
    public void testGAFProgRel() throws IOException {
        String name = instance.getAbsoluteFilename(FileUtil.PROGRAM + "jython");
        assertEquals(new File("jython").getCanonicalPath(), name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGAFProgAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getAbsoluteFilename(FileUtil.PROGRAM + f.getAbsolutePath());
        assertEquals(f.getCanonicalPath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGAFPrefRel() throws IOException {
        String name = instance.getAbsoluteFilename(FileUtil.PREFERENCES + "non-existant-file-foo");
        assertEquals(new File(instance.getUserFilesPath() + "non-existant-file-foo").getCanonicalPath(), name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGAFPrefAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getAbsoluteFilename(FileUtil.PREFERENCES + f.getAbsolutePath());
        assertEquals(f.getCanonicalPath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    @Test
    public void testGAFHomeRel() throws IOException {
        String name = instance.getAbsoluteFilename(FileUtil.HOME + "non-existant-file-foo");
        assertEquals(new File(System.getProperty("user.home") + File.separator + "non-existant-file-foo").getCanonicalPath(), name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    @Test
    public void testGAFHomeAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = instance.getAbsoluteFilename(FileUtil.HOME + f.getAbsolutePath());
        assertEquals(f.getCanonicalPath(), name);
    }

    @Test
    public void testCopyFile() throws FileNotFoundException {
        File src = instance.getFile(instance.getAbsoluteFilename("program:default.lcf"));
        File dest = new File(instance.getAbsoluteFilename("program:fileUtilTest.lcf"));
        ArrayList<String> sl = new ArrayList<>();
        ArrayList<String> dl = new ArrayList<>();
        try {
            instance.copy(src, dest);
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
            instance.delete(dest);
            fail("Unable to copy");
        }
        instance.delete(dest);
        assertTrue(sl.equals(dl));
    }

    @Test
    public void testCopyDirectoryToExistingDirectory() throws FileNotFoundException, IOException {
        File src = instance.getFile(instance.getAbsoluteFilename("program:web/fonts"));
        File dest = Files.createTempDirectory("FileUtilTest").toFile();
        instance.copy(src, dest);
        String[] destFiles = dest.list();
        String[] srcFiles = src.list();
        Arrays.sort(destFiles);
        Arrays.sort(srcFiles);
        instance.delete(dest);
        assertTrue(Arrays.equals(srcFiles, destFiles));
    }

    @Test
    public void testDeleteFile() throws IOException {
        File file = File.createTempFile("FileUtilTest", null);
        instance.copy(instance.getFile(instance.getAbsoluteFilename("program:default.lcf")), file);
        instance.delete(file);
        assertFalse(file.exists());
    }

    @Test
    public void testAppendTextToFile() throws IOException {
        File file = File.createTempFile("FileUtilTest", null);
        String text = "jmri.util.FileUtil#appendTextToFile";
        instance.appendTextToFile(file, text);
        List<String> lines = Files.readAllLines(Paths.get(file.toURI()), Charset.forName("UTF-8"));
        assertEquals(text, lines.get(0));
    }

    @Test
    public void testFindURIPath() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findURI(this.programTestFile.getName()));
        assertEquals(uri, instance.findURI(FileUtil.PROGRAM + this.programTestFile.getName()));
        assertNull(instance.findURI(FileUtil.PREFERENCES + this.programTestFile.getName()));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findURI(this.preferencesTestFile.getName()));
        assertEquals(uri, instance.findURI(FileUtil.PREFERENCES + this.preferencesTestFile.getName()));
        assertNull(instance.findURI(FileUtil.PROGRAM + this.preferencesTestFile.getName()));
    }

    @Test
    public void testFindURIPathLocation() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findURI(this.programTestFile.getName(), FileUtil.Location.INSTALLED));
        assertEquals(uri, instance.findURI(this.programTestFile.getName(), FileUtil.Location.ALL));
        assertNull(instance.findURI(this.programTestFile.getName(), FileUtil.Location.USER));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findURI(this.preferencesTestFile.getName(), FileUtil.Location.USER));
        assertEquals(uri, instance.findURI(this.preferencesTestFile.getName(), FileUtil.Location.ALL));
        assertNull(instance.findURI(this.preferencesTestFile.getName(), FileUtil.Location.INSTALLED));
    }

    @Test
    public void testFindURIPathSearchPaths() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findURI(this.programTestFile.getName(), new String[]{instance.getProgramPath()}));
        assertEquals(uri, instance.findURI(this.programTestFile.getName(), new String[]{instance.getPreferencesPath(), instance.getProgramPath()}));
        assertEquals(uri, instance.findURI(this.programTestFile.getName(), new String[]{instance.getPreferencesPath()}));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findURI(this.preferencesTestFile.getName(), new String[]{instance.getPreferencesPath()}));
        assertEquals(uri, instance.findURI(this.preferencesTestFile.getName(), new String[]{instance.getPreferencesPath(), instance.getProgramPath()}));
        assertEquals(uri, instance.findURI(this.preferencesTestFile.getName(), new String[]{instance.getProgramPath()}));
    }

    @Test
    public void testFindExternalFilename() {
        URI uri = this.programTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findExternalFilename(this.programTestFile.getName()));
        assertEquals(uri, instance.findExternalFilename(FileUtil.PROGRAM + this.programTestFile.getName()));
        assertNull(instance.findExternalFilename(FileUtil.PREFERENCES + this.programTestFile.getName()));
        uri = this.preferencesTestFile.toURI();
        assertNotNull(uri);
        assertEquals(uri, instance.findExternalFilename(this.preferencesTestFile.getName()));
        assertEquals(uri, instance.findExternalFilename(FileUtil.PREFERENCES + this.preferencesTestFile.getName()));
        assertNull(instance.findExternalFilename(FileUtil.PROGRAM + this.preferencesTestFile.getName()));
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        this.instance = new FileUtilSupport();
        this.programTestFile = new File(UUID.randomUUID().toString());
        this.programTestFile.createNewFile();
        JUnitUtil.waitFor(() -> {
            return this.programTestFile.exists();
        }, "Create program test file");
        File profile = new File(instance.getProfilePath());
        profile.mkdir();
        this.preferencesTestFile = new File(profile, UUID.randomUUID().toString());
        this.preferencesTestFile.createNewFile();
        JUnitUtil.waitFor(() -> {
            return this.preferencesTestFile.exists();
        }, "Create program test file");
    }

    @After
    public void tearDown() {
        this.programTestFile.delete();
        JUnitUtil.waitFor(() -> {
            return !this.programTestFile.exists();
        }, "Remove program test file");
        this.preferencesTestFile.delete();
        JUnitUtil.waitFor(() -> {
            return !this.preferencesTestFile.exists();
        }, "Remove program test file");
        jmri.util.JUnitUtil.tearDown();
    }
}
