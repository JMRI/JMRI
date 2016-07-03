package jmri.util;

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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.FileUtil class.
 *
 * Most tests use a deliberately non-existent filename since FileUtil appends a
 * / to the end of a portable directory name, and tests could fail if they
 * expect a file or non-existent filename and a directory exists at that path.
 *
 * @author	Bob Jacobsen Copyright 2003, 2009
 */
public class FileUtilTest extends TestCase {

    private File programTestFile;
    private File preferencesTestFile;

    // tests of internal to external mapping
    // relative file with no prefix: Leave relative in system-specific form
    public void testGEFRel() {
        String name = FileUtil.getExternalFilename("resources/non-existant-file-foo");
        Assert.assertEquals("resources" + File.separator + "non-existant-file-foo", name); 
    }

    // relative file with no prefix: Leave relative in system-specific form
    public void testGEFAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename(f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // resource: prefix with relative path, convert to relative in system-specific form
    public void testGEFResourceRel() {
        String name = FileUtil.getExternalFilename("resource:resources/non-existant-file-foo");
        Assert.assertEquals(new File("resources/non-existant-file-foo").getAbsolutePath(), name);
    }

    // resource: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFResourceAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("resource:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    public void testGEFProgramRel() {
        String name = FileUtil.getExternalFilename("program:jython");
        Assert.assertEquals(new File("jython").getAbsolutePath(), name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFProgramAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("program:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    public void testGEFPrefRel() {
        String name = FileUtil.getExternalFilename("preference:non-existant-file-foo");
        Assert.assertEquals(FileUtil.getUserFilesPath() + "non-existant-file-foo", name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFPrefAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("preference:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // file: prefix with relative path, convert to absolute in system-specific form
    public void testGEFFileRel() {
        String name = FileUtil.getExternalFilename("file:non-existant-file-foo");
        Assert.assertEquals(new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo").getAbsolutePath(), name);
    }

    // file: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFFileAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("file:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    public void testGEFHomeRel() {
        String name = FileUtil.getExternalFilename("home:non-existant-file-foo");
        Assert.assertEquals(System.getProperty("user.home") + File.separator + "non-existant-file-foo", name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFHomeAbs() {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getExternalFilename("home:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // tests of external to internal mapping
    @SuppressWarnings("unused")
    public void testGetpfPreferenceF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:non-existant-file-foo", name);
    }

    public void testGetpfPreferenceS() {
        String name = FileUtil.getPortableFilename("preference:non-existant-file-foo");
        Assert.assertEquals("preference:non-existant-file-foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfResourceF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/non-existant-file-foo", name);
    }

    public void testGetpfResourceS() {
        String name = FileUtil.getPortableFilename("resource:resources/non-existant-file-foo");
        Assert.assertEquals("program:resources/non-existant-file-foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfPrefF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/non-existant-file-foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfProgramF() throws IOException {
        File f = new File("resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("program:resources/non-existant-file-foo", name);
    }

    public void testGetpfProgramS() {
        String name = FileUtil.getPortableFilename("program:resources/non-existant-file-foo");
        Assert.assertEquals("program:resources/non-existant-file-foo", name);
    }

    /*
     * Test a real directory. It should end with a separator.
     */
    public void testGetpfProgramDirS() {
        String name = FileUtil.getPortableFilename("program:resources/icons");
        Assert.assertEquals("program:resources/icons/", name);
    }

    public void testGetpfFileS() {
        String name = FileUtil.getPortableFilename("file:non-existant-file-foo");
        Assert.assertEquals("preference:resources/non-existant-file-foo", name);
    }

    public void testGetpfFileS2() {
        String name = FileUtil.getPortableFilename("resource:resources/non-existant-file-foo");
        Assert.assertEquals("program:resources/non-existant-file-foo", name);
    }

    public void testGetpfHomeS() {
        String name = FileUtil.getPortableFilename("home:non-existant-file-foo");
        Assert.assertEquals("home:non-existant-file-foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfHomeF() throws IOException {
        File f = new File(System.getProperty("user.home") + File.separator + "resources" + File.separator + "non-existant-file-foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("home:resources/non-existant-file-foo", name);
    }

    /*
     * test getAbsoluteFilename()
     *
     * There are no tests for resource: and file: since getAbsoluteFilename()
     * uses getPortableFilename() to convert these prefixes to one of the
     * other prefixes.
     */
    // relative file with no prefix: Should become null
    public void testGAFRel() {
        String name = FileUtil.getAbsoluteFilename("resources/non-existant-file-foo");
        Assert.assertEquals(null, name);
    }

    // absolute file: Should become canonical path
    public void testGAFAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    public void testGAFProgRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.PROGRAM + "jython");
        Assert.assertEquals(new File("jython").getCanonicalPath(), name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    public void testGAFProgAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(FileUtil.PROGRAM + f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    public void testGAFPrefRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + "non-existant-file-foo");
        Assert.assertEquals(new File(FileUtil.getUserFilesPath() + "non-existant-file-foo").getCanonicalPath(), name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    public void testGAFPrefAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    public void testGAFHomeRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.HOME + "non-existant-file-foo");
        Assert.assertEquals(new File(System.getProperty("user.home") + File.separator + "non-existant-file-foo").getCanonicalPath(), name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    public void testGAFHomeAbs() throws IOException {
        File f = new File("resources/non-existant-file-foo");
        String name = FileUtil.getAbsoluteFilename(FileUtil.HOME + f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
    }

    public void testCopyFile() throws FileNotFoundException {
        File src = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:default.lcf"));
        File dest = new File(FileUtil.getAbsoluteFilename("program:fileUtilTest.lcf"));
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
            log.error("Unable to copy");
        }
        FileUtil.delete(dest);
        Assert.assertTrue(sl.equals(dl));
    }

    public void testCopyDirectoryToExistingDirectory() throws FileNotFoundException, IOException {
        File src = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:web/fonts"));
        File dest = Files.createTempDirectory("FileUtilTest").toFile();
        FileUtil.copy(src, dest);
        String[] destFiles = dest.list();
        String[] srcFiles = src.list();
        Arrays.sort(destFiles);
        Arrays.sort(srcFiles);
        FileUtil.delete(dest);
        Assert.assertTrue(Arrays.equals(srcFiles, destFiles));
    }

    public void testDeleteFile() throws IOException {
        File file = File.createTempFile("FileUtilTest", null);
        FileUtil.copy(FileUtil.getFile(FileUtil.getAbsoluteFilename("program:default.lcf")), file);
        FileUtil.delete(file);
        Assert.assertFalse(file.exists());
    }

    public void testAppendTextToFile() throws IOException {
        File file = File.createTempFile("FileUtilTest", null);
        String text = "jmri.util.FileUtil#appendTextToFile";
        FileUtil.appendTextToFile(file, text);
        List<String> lines = Files.readAllLines(Paths.get(file.toURI()), Charset.forName("UTF-8"));
        Assert.assertEquals(text, lines.get(0));
    }

    public void testFindURIPath() {
        URI uri = this.programTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findURI(this.programTestFile.getName()));
        Assert.assertEquals(uri, FileUtil.findURI(FileUtil.PROGRAM + this.programTestFile.getName()));
        Assert.assertNull(FileUtil.findURI(FileUtil.PREFERENCES + this.programTestFile.getName()));
        uri = this.preferencesTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName()));
        Assert.assertEquals(uri, FileUtil.findURI(FileUtil.PREFERENCES + this.preferencesTestFile.getName()));
        Assert.assertNull(FileUtil.findURI(FileUtil.PROGRAM + this.preferencesTestFile.getName()));
    }

    public void testFindURIPathLocation() {
        URI uri = this.programTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), FileUtil.Location.INSTALLED));
        Assert.assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), FileUtil.Location.ALL));
        Assert.assertNull(FileUtil.findURI(this.programTestFile.getName(), FileUtil.Location.USER));
        uri = this.preferencesTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), FileUtil.Location.USER));
        Assert.assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), FileUtil.Location.ALL));
        Assert.assertNull(FileUtil.findURI(this.preferencesTestFile.getName(), FileUtil.Location.INSTALLED));
    }

    public void testFindURIPathSearchPaths() {
        URI uri = this.programTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), new String[]{FileUtil.getProgramPath()}));
        Assert.assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), new String[]{FileUtil.getPreferencesPath(), FileUtil.getProgramPath()}));
        Assert.assertEquals(uri, FileUtil.findURI(this.programTestFile.getName(), new String[]{FileUtil.getPreferencesPath()}));
        uri = this.preferencesTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), new String[]{FileUtil.getPreferencesPath()}));
        Assert.assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), new String[]{FileUtil.getPreferencesPath(), FileUtil.getProgramPath()}));
        Assert.assertEquals(uri, FileUtil.findURI(this.preferencesTestFile.getName(), new String[]{FileUtil.getProgramPath()}));
    }

    public void testFindExternalFilename() {
        URI uri = this.programTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findExternalFilename(this.programTestFile.getName()));
        Assert.assertEquals(uri, FileUtil.findExternalFilename(FileUtil.PROGRAM + this.programTestFile.getName()));
        Assert.assertNull(FileUtil.findExternalFilename(FileUtil.PREFERENCES + this.programTestFile.getName()));
        uri = this.preferencesTestFile.toURI();
        Assert.assertNotNull(uri);
        Assert.assertEquals(uri, FileUtil.findExternalFilename(this.preferencesTestFile.getName()));
        Assert.assertEquals(uri, FileUtil.findExternalFilename(FileUtil.PREFERENCES + this.preferencesTestFile.getName()));
        Assert.assertNull(FileUtil.findExternalFilename(FileUtil.PROGRAM + this.preferencesTestFile.getName()));
    }

    @Override
    protected void setUp() throws Exception {
        this.programTestFile = new File(UUID.randomUUID().toString());
        this.programTestFile.createNewFile();
        this.preferencesTestFile = new File(FileUtil.getProfilePath() + UUID.randomUUID().toString());
        this.preferencesTestFile.createNewFile();
    }

    @Override
    protected void tearDown() {
        this.programTestFile.delete();
        this.preferencesTestFile.delete();
    }

    // from here down is testing infrastructure
    public FileUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", FileUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FileUtilTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(FileUtilTest.class.getName());
}
