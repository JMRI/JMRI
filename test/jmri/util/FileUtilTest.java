// FileUtilTest.java

package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.io.*;

import jmri.jmrit.XmlFile;

/**
 * Tests for the jmri.util.FileUtil class.
 * @author	Bob Jacobsen  Copyright 2003, 2009
 * @version	$Revision: 1.5 $
 */
public class FileUtilTest extends TestCase {


    // tests of internal to external mapping
    
    public void testGEFRel() {
        String name = FileUtil.getExternalFilename("resources/icons");
        Assert.assertEquals("resources/icons", name);
    }

    public void testGEFAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename(f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    public void testGEFResourceRel() {
        String name = FileUtil.getExternalFilename("resource:icons");
        Assert.assertEquals("resources/icons", name);
    }

    public void testGEFResourceAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("resource:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    public void testGEFProgramRel() {
        String name = FileUtil.getExternalFilename("program:jython");
        Assert.assertEquals("jython", name);
    }

    public void testGEFProgramAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("program:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    public void testGEFPrefRel() {
        String name = FileUtil.getExternalFilename("preference:foo");
        Assert.assertEquals(XmlFile.userFileLocationDefault()+"foo", name);
    }

    public void testGEFPrefAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("preference:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    public void testGEFFileRel() {
        String name = FileUtil.getExternalFilename("file:foo");
        Assert.assertEquals(XmlFile.userFileLocationDefault()+"resources"+File.separator+"foo", name);
    }

    public void testGEFFileAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("file:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // tests of external to internal mapping

    @SuppressWarnings("unused")
	public void testGetpfPreferenceF() throws IOException {
        File f = new File(XmlFile.prefsDir()+File.separator+"foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:foo", name);
    }

    public void testGetpfPreferenceS() {
        String name = FileUtil.getPortableFilename("preference:foo");
        Assert.assertEquals("preference:foo", name);
    }

    @SuppressWarnings("unused")
	public void testGetpfResourceF() throws IOException {
        File f = new File(XmlFile.prefsDir()+File.separator+"resources"+File.separator+"foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/foo", name);
    }

    public void testGetpfResourceS() {
        String name = FileUtil.getPortableFilename("preference:resources/foo");
        Assert.assertEquals("preference:resources/foo", name);
    }

    @SuppressWarnings("unused")
	public void testGetpfProgramF() throws IOException {
        File f = new File("resources"+File.separator+"icons");
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
        String name = FileUtil.getPortableFilename("resource:icons");
        Assert.assertEquals("program:resources/icons", name);
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

	 static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileUtilTest.class.getName());

}
