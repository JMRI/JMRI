// SchemaTest.java

package jmri.jmrit.decoderdefn;

import org.apache.log4j.Logger;
import junit.framework.*;

//import jmri.InstanceManager;

/**
 * Checks of JMRI XML Schema for decoder definition files.
 * 
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 * @version $Revision$
 */
public class SchemaTest extends jmri.configurexml.LoadFileTestBase {

    public void testValidateQsiV7() {
        validate(new java.io.File("java/test/jmri/jmrit/decoderdefn/QSI_Diesel_Ver7.xml"));
    }

    public void testDigitraxPart() {
        validate(new java.io.File("xml/decoders/digitrax/consistAddrDirection.xml"));
    }

    public void testLenzPart() {
        validate(new java.io.File("xml/decoders/lenz/functionmap.xml"));
        validate(new java.io.File("xml/decoders/lenz/braking_cv51.xml"));
        validate(new java.io.File("xml/decoders/lenz/abc_cv51.xml"));
        validate(new java.io.File("xml/decoders/Lenz_Plus_2010.xml"));
    }

    public void testValidateQualifier() {
        validate(new java.io.File("java/test/jmri/jmrit/decoderdefn/DecoderWithQualifier.xml"));
    }

    public void testPartDirectories() {
        java.io.File dir = new java.io.File("xml/decoders/");
        java.io.File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                System.out.println(files[i].getPath());
                validateDirectory(files[i].getPath());
            }
        }
    }
    
    public void testRealFiles() {
        validateDirectory("xml/decoders/");
    }
    
    void validateDirectory(String name) {
        java.io.File dir = new java.io.File(name);
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith("xml")) {
                validate(files[i]);
            }
        }
    }
    
    // from here down is testing infrastructure

    public SchemaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SchemaTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(SchemaTest.class.getName());
}
