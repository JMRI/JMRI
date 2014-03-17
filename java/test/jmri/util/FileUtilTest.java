// FileUtilTest.java
package jmri.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

/**
 * Tests for the jmri.util.FileUtil class.
 *
 * Most tests use a deliberately non-existent filename since FileUtil appends a
 * / to the end of a portable directory name, and tests could fail if they
 * expect a file or non-existent filename and a directory exists at that path.
 *
 * @author	Bob Jacobsen Copyright 2003, 2009
 * @version	$Revision$
 */
public class FileUtilTest extends TestCase {

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
        Assert.assertEquals("resources" + File.separator + "non-existant-file-foo", name);
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
        Assert.assertEquals("jython", name);
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
        Assert.assertEquals(FileUtil.getUserFilesPath() + "resources" + File.separator + "non-existant-file-foo", name);
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
        ArrayList<String> sl = new ArrayList<String>();
        ArrayList<String> dl = new ArrayList<String>();
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
        // create a temporary directory by creating a temp file, deleting it, and creating a directory with a similar name
        // see http://stackoverflow.com/a/617438
        File file = File.createTempFile("FileUtilTest", null);
        file.delete();
        File dest = new File(file.getPath() + ".d");
        dest.mkdir();
        FileUtil.copy(src, dest);
        String[] destFiles = dest.list();
        FileUtil.delete(dest);
        Assert.assertTrue(Arrays.equals(src.list(), destFiles));
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
        // Java 7 equivalent for following code
        // List<String> lines = Files.readAllLines(Paths.get(path), encoding);
        // Assert.assertEquals(text, lines.get(0));
        FileInputStream stream = new FileInputStream(file);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            // test appends newline because appendTextToFile() method uses println() to append to file
            Assert.assertEquals(text + "\n", StandardCharsets.UTF_8.decode(bb).toString());
        } finally {
            stream.close();
        }
    }

    // from here down is testing infrastructure
    public FileUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FileUtilTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FileUtilTest.class);
        return suite;
    }
    static Logger log = Logger.getLogger(FileUtilTest.class.getName());
}
