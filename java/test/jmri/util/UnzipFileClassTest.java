package jmri.util;

import java.io.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the UnzipFileClassTest class
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class UnzipFileClassTest  {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testFileNotFoundError() throws Exception {
        Assertions.assertNotNull(tempDir);
        
        Exception ex = Assertions.assertThrows(FileNotFoundException.class, () -> {
            UnzipFileClass.unzipFunction(tempDir+"/UnzipFileClass", "noFile.zip"); // noFile.zip should not exist
        });
        Assertions.assertNotNull(ex);
    }

    @Test
    public void testFNoZipFile() throws FileNotFoundException {
        Assertions.assertNotNull(tempDir);

        UnzipFileClass.unzipFunction(new File(tempDir + "/UnzipFileClass"), new FileInputStream("java/test/jmri/util/UnzipFileClassTest.zip")); // build.xml is not a .zip file
        Assert.assertTrue(new File(tempDir + "/UnzipFileClass/UnzipFileClass.txt").exists());
    }

    private File tempDir;

    @BeforeEach
    public void setUp(@TempDir File tempD) throws IOException  {
        JUnitUtil.setUp();
        tempDir = tempD;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
