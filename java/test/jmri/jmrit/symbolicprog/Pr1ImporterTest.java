// Pr1ImporterTest.java

package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import jmri.util.FileUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests for Pr1Importer class.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 */
public class Pr1ImporterTest extends TestCase {

    class Pr1ImporterDummy extends Pr1Importer {
        Pr1ImporterDummy(File f) throws java.io.IOException { super(f); }

        boolean checkResult() {
            return m_packedValues;
        }
    }

    public File makeTempFile(String contents) throws IOException {
        // create a file
        FileUtil.createDirectory(FileUtil.getUserFilesPath()+"temp");
        File f = new java.io.File(FileUtil.getUserFilesPath()+"temp"+File.separator+"Pr1Importer.test.xml");
        // recreate it
        if (f.exists()) f.delete();
        PrintStream p = new PrintStream(new FileOutputStream(f));
        p.print(contents);
        p.close();

        return f;
    }

    public void testJustCVValues() throws IOException {
        // create a file
        String s = "CV1=0\n"
                    +"CV2=1\n";
        File f = makeTempFile(s);

        boolean result = new Pr1ImporterDummy(f).checkResult();

        Assert.assertTrue("should not pack", !result);
    }

    public void testHasBadHeader() throws IOException {
        // create a file
        String s = "Version=2\n"
                    +"CV2=1\n";
        File f = makeTempFile(s);

        try {
            new Pr1Importer(f);
        } catch (IOException e) {
            Assert.assertTrue("should have failed", e.getMessage().startsWith("Unsupported PR1"));
            return;
        }
        Assert.fail("Should have asserted error due to bad version");
    }

    public void testhasLargeValues() throws IOException {
        // create a file
        String s = "CV1=3\n"
                    +"CV2=1\n"
                    +"CV3=300\n"
                    +"CV4=23\n";
        File f = makeTempFile(s);

        boolean result = new Pr1ImporterDummy(f).checkResult();

        Assert.assertTrue("should pack", result);
    }

    public void testOkVersion() throws IOException {
        // create a file
        String s = "Version=0\n"
                    +"CV2=1\n";
        File f = makeTempFile(s);

        boolean result = new Pr1ImporterDummy(f).checkResult();

        Assert.assertTrue("should pack", result);
    }

    public Pr1ImporterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Pr1ImporterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite( Pr1ImporterTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Pr1ImporterTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}

