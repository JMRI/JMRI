// SignalSystemFileCheckTest.java

package jmri.implementation;

import org.apache.log4j.Logger;
import junit.framework.*;

import java.io.*;

/**
 * Tests of the signal system definition files.
 *<p>
 * Checks all files in the distribution directory
 *
 * @author	Bob Jacobsen  Copyright (C) 2009
 * @version $Revision$
 */
public class SignalSystemFileCheckTest extends jmri.configurexml.LoadFileTestBase {

    public void testSampleAspect() {
        File sample = new File("xml"+File.separator+"signals"+File.separator+"sample-aspects.xml");
        validate(sample);
    }
    
    public void testSampleAppearance() {
        File sample = new File("xml"+File.separator+"signals"+File.separator+"sample-appearance.xml");
        validate(sample);
    }
    
	public void testAllAspectFiles() {
        File signalDir = new File("xml"+File.separator+"signals");
        File[] files = signalDir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                // check that there's an aspects.xml file
                File aspects = new File(files[i].getPath()+File.separator+"aspects.xml");
                if (aspects.exists()) {
                    log.info("found system: "+files[i].getName());
                    validate(aspects);
                }
            }
        }
	}

	public void testAllAppearanceFiles() {
        File signalDir = new File("xml"+File.separator+"signals");
        File[] files = signalDir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                // check that there's an aspects.xml file
                File aspects = new File(files[i].getPath()+File.separator+"aspects.xml");
                if (aspects.exists()) {
                    log.info("found system: "+files[i].getName());
                    // gather all the appearance files
                    File[] apps = files[i].listFiles();
                    for (int j=0; j<apps.length; j++) {
                        if (apps[j].getName().startsWith("appearance")
                            && apps[j].getName().endsWith(".xml")) {
                                log.info("   found file: "+apps[j].getName());
                                validate(apps[j]);
                        }
                    }
                }
            }
        }
	}

    
	// from here down is testing infrastructure

	public SignalSystemFileCheckTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SignalSystemFileCheckTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SignalSystemFileCheckTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    static protected Logger log = Logger.getLogger(SignalSystemFileCheckTest.class.getName());
}
