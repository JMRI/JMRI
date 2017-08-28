package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Pr1Importer class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class Pr1ImporterTest {

    class Pr1ImporterDummy extends Pr1Importer {

        Pr1ImporterDummy(File f) throws java.io.IOException {
            super(f);
        }

        boolean checkResult() {
            return m_packedValues;
        }
    }

    public File makeTempFile(String contents) throws IOException {
        // create a file
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File f = new java.io.File(FileUtil.getUserFilesPath() + "temp" + File.separator + "Pr1Importer.test.xml");
        // recreate it
        if (f.exists()) {
            f.delete();
        }
        try (PrintStream p = new PrintStream(new FileOutputStream(f))) {
            p.print(contents);
        }

        return f;
    }

    @Test
    public void testJustCVValues() throws IOException {
        // create a file
        String s = "CV1=0\n"
                + "CV2=1\n";
        File f = makeTempFile(s);

        boolean result = new Pr1ImporterDummy(f).checkResult();

        Assert.assertTrue("should not pack", !result);
    }

    @Test
    public void testHasBadHeader() throws IOException {
        // create a file
        String s = "Version=2\n"
                + "CV2=1\n";
        File f = makeTempFile(s);

        try {
            new Pr1Importer(f);
        } catch (IOException e) {
            Assert.assertTrue("should have failed", e.getMessage().startsWith("Unsupported PR1"));
            return;
        }
        Assert.fail("Should have asserted error due to bad version");
    }

    @Test
    public void testhasLargeValues() throws IOException {
        // create a file
        String s = "CV1=3\n"
                + "CV2=1\n"
                + "CV3=300\n"
                + "CV4=23\n";
        File f = makeTempFile(s);

        boolean result = new Pr1ImporterDummy(f).checkResult();

        Assert.assertTrue("should pack", result);
    }

    @Test
    public void testOkVersion() throws IOException {
        // create a file
        String s = "Version=0\n"
                + "CV2=1\n";
        File f = makeTempFile(s);

        boolean result = new Pr1ImporterDummy(f).checkResult();

        Assert.assertTrue("should pack", result);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
