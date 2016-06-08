package jmri.jmrix.loconet.spjfile;

import jmri.jmrit.Sound;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.spjfile package
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class PackageTest extends TestCase {

    public void testCreate() {
        new SpjFile(new java.io.File("ac4400.spj"));
    }

    SpjFile testFile = null;

    void loadFile() throws java.io.IOException {
        if (testFile == null) {
            testFile = new SpjFile(new java.io.File("java/test/jmri/jmrix/loconet/spjfile/test.spj"));
            testFile.read();
        }
    }

    // The following is commented out; usually used to split
    // out a file into several subparts
/*     public void testWriteSubFile() throws java.io.IOException { */
    /*          */
    /*         // and write */
    /*         testFile = new SpjFile("java/test/jmri/jmrix/loconet/spjfile/test.spj"); */
    /*         testFile.read(); */
    /*         testFile.writeSubFiles(); */
    /*     } */
    public void testPlayWav() throws java.io.IOException {
        loadFile();

        // and write
        // play 1st wav header
        int n = testFile.numHeaders();
        for (int i = 1; i < n; i++) {
            if (testFile.headers[i].isWAV()) {
                byte[] buffer = testFile.headers[i].getByteArray();
                playSoundBuffer(buffer);
                return;
            }
        }
    }

    public void playSoundBuffer(byte[] data) {
        Sound.playSoundBuffer(data);
    }

    public void testGetMapEntries() throws java.io.IOException {
        loadFile();

        Assert.assertEquals("1", "DIESEL_START_BELL", testFile.getMapEntry(1));
        Assert.assertEquals("2", "DIESEL_START", testFile.getMapEntry(2));
        Assert.assertEquals("31", "USER_F28", testFile.getMapEntry(31));
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        return suite;
    }

}
