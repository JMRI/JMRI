// SchemaTest.java

package jmri.jmrit.decoderdefn;

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
        validate(new java.io.File("xml/decoders/lenz/braking_cv51.xml"));
    }

    public void testValidateQualifier() {
        validate(new java.io.File("java/test/jmri/jmrit/decoderdefn/DecoderWithQualifier.xml"));
    }

    public void testRealFiles() {
        java.io.File dir = new java.io.File("xml/decoders/");
        java.io.File[] files = dir.listFiles();
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SchemaTest.class.getName());
}
