package jmri.util;

import java.io.*;

import org.junit.*;

/**
 * Tests for the UnzipFileClassTest class
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class UnzipFileClassTest  {

    @Test public void testCtor() {
        new UnzipFileClass();
    }


    @Test public void testFileNotFoundError() {
        new File("temp").mkdirs();
        
        try {
            UnzipFileClass.unzipFunction("temp/UnzipFileClass", "noFile.zip"); // noFile.zip should not exist
        } catch (FileNotFoundException e) { return; }
        Assert.fail("Should have thrown");
    }
        
    @Test public void testFNoZipFile() throws FileNotFoundException {
        new File("temp").mkdirs();
        new File("temp/UnzipFileClass//UnzipFileClass.txt").delete();
        
        UnzipFileClass.unzipFunction(new File("temp/UnzipFileClass"), new FileInputStream("java/test/jmri/util/UnzipFileClassTest.zip")); // build.xml is not a .zip file
        
        Assert.assertTrue(new File("temp/UnzipFileClass/UnzipFileClass.txt").exists());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
