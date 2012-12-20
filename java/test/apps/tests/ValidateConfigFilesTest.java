// ValidateConfigFilesTest.java

package apps;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.InstanceManager;

/**
 * Test upper level loading of config files
 * 
 * @author Bob Jacobsen Copyright 2012
 * @since 2.5.5
 * @version $Revision$
 */
public class ValidateConfigFilesTest extends jmri.configurexml.LoadFileTestBase {


    public void testValidateOne() {
        validate(new java.io.File("xml/config/parts/jmri/jmrit/roster/swing/RosterFrameToolBar.xml"));
    }

    // from here down is testing infrastructure

    // Note setup() and teardown are provided from base class, and 
    // need to be invoked if you add methods here
    
    public ValidateConfigFilesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", ValidateConfigFilesTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ValidateConfigFilesTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ValidateConfigFilesTest.class.getName());

}
