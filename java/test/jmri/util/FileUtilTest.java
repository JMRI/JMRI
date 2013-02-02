// FileUtilTest.java
package jmri.util;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import jmri.jmrit.XmlFile;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.FileUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2009
 * @version	$Revision$
 */
public class FileUtilTest extends TestCase {

    // tests of internal to external mapping
    // relative file with no prefix: Leave relative in system-specific form
    public void testGEFRel() {
        String name = FileUtil.getExternalFilename("resources/icons");
        Assert.assertEquals("resources" + File.separator + "icons", name);
    }

    // relative file with no prefix: Leave relative in system-specific form
    public void testGEFAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename(f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // resource: prefix with relative path, convert to relative in system-specific form
    public void testGEFResourceRel() {
        String name = FileUtil.getExternalFilename("resource:resources/icons");
        Assert.assertEquals("resources" + File.separator + "icons", name);
    }

    // resource: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFResourceAbs() {
        File f = new File("resources/icons");
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
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("program:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    public void testGEFPrefRel() {
        String name = FileUtil.getExternalFilename("preference:foo");
        Assert.assertEquals(FileUtil.getUserFilesPath() + "foo", name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFPrefAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("preference:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // file: prefix with relative path, convert to absolute in system-specific form
    public void testGEFFileRel() {
        String name = FileUtil.getExternalFilename("file:foo");
        Assert.assertEquals(FileUtil.getUserFilesPath() + "resources" + File.separator + "foo", name);
    }

    // file: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFFileAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("file:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    public void testGEFHomeRel() {
        String name = FileUtil.getExternalFilename("home:foo");
        Assert.assertEquals(System.getProperty("user.home") + File.separator + "foo", name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFHomeAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("home:" + f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // tests of external to internal mapping
    @SuppressWarnings("unused")
    public void testGetpfPreferenceF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:foo", name);
    }

    public void testGetpfPreferenceS() {
        String name = FileUtil.getPortableFilename("preference:foo");
        Assert.assertEquals("preference:foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfResourceF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/foo", name);
    }

    public void testGetpfResourceS() {
        String name = FileUtil.getPortableFilename("resource:resources/foo");
        Assert.assertEquals("program:resources/foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfPrefF() throws IOException {
        File f = new File(FileUtil.getUserFilesPath() + "resources" + File.separator + "icons");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/icons", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfProgramF() throws IOException {
        File f = new File("resources" + File.separator + "icons");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("program:resources/icons", name);
    }

    public void testGetpfProgramS() {
        String name = FileUtil.getPortableFilename("program:resources/icons");
        Assert.assertEquals("program:resources/icons", name);
    }

    public void testGetpfFileS() {
        String name = FileUtil.getPortableFilename("file:icons");
        Assert.assertEquals("preference:resources/icons", name);
    }

    public void testGetpfFileS2() {
        String name = FileUtil.getPortableFilename("resource:resources/icons");
        Assert.assertEquals("program:resources/icons", name);
    }

    public void testGetpfHomeS() {
        String name = FileUtil.getPortableFilename("home:foo");
        Assert.assertEquals("home:foo", name);
    }

    @SuppressWarnings("unused")
    public void testGetpfHomeF() throws IOException {
        File f = new File(System.getProperty("user.home") + File.separator + "resources" + File.separator + "icons");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("home:resources/icons", name);
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
        String name = FileUtil.getAbsoluteFilename("resources/icons");
        Assert.assertEquals(null, name);
    }

    // absolute file: Should become canonical path
    public void testGAFAbs() throws IOException {
        File f = new File("resources/icons");
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
        File f = new File("resources/icons");
        String name = FileUtil.getAbsoluteFilename(FileUtil.PROGRAM + f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    public void testGAFPrefRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + "foo");
        Assert.assertEquals(new File(FileUtil.getUserFilesPath() + "foo").getCanonicalPath(), name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    public void testGAFPrefAbs() throws IOException {
        File f = new File("resources/icons");
        String name = FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
    }

    // home: prefix with relative path, convert to absolute in system-specific form
    public void testGAFHomeRel() throws IOException {
        String name = FileUtil.getAbsoluteFilename(FileUtil.HOME + "foo");
        Assert.assertEquals(new File(System.getProperty("user.home") + File.separator + "foo").getCanonicalPath(), name);
    }

    // home: prefix with absolute path, convert to absolute in system-specific form
    public void testGAFHomeAbs() throws IOException {
        File f = new File("resources/icons");
        String name = FileUtil.getAbsoluteFilename(FileUtil.HOME + f.getAbsolutePath());
        Assert.assertEquals(f.getCanonicalPath(), name);
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
