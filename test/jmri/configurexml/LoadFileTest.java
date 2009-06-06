// LoadFileText.java

package jmri.configurexml;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;
import jmri.InstanceManager;

/**
 * Test upper level loading of a file
 * 
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision: 1.2 $
 */
public class LoadFileTest extends TestCase {

    public void testLoadOne() {
        // load file
        InstanceManager.configureManagerInstance()
            .load(new java.io.File("java/test/jmri/configurexml/LoadFileTest.xml"));
    
        // check existance of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));
        
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));
        
    }
    
    public void testLoadStoreOne() throws Exception {
        // load manager
        java.io.File inFile = new java.io.File("java/test/jmri/configurexml/LoadFileTest.xml");
        
        // load file
        InstanceManager.configureManagerInstance()
            .load(inFile);
    
        // store file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");
        java.io.File outFile = new java.io.File("temp/LoadFileTest.xml");
        InstanceManager.configureManagerInstance()
            .storeConfig(outFile);
        
        // compare files, except for certain special lines
        BufferedReader inFileStream = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(inFile)));
        BufferedReader outFileStream = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(outFile)));
        String inLine;
        String outLine;
        while ( (inLine = inFileStream.readLine())!=null && (outLine = outFileStream.readLine())!=null) {
            if (!inLine.startsWith("  <!--Written by JMRI version"))
                Assert.assertEquals(inLine, outLine);
        }
    }
        
    // from here down is testing infrastructure

    public LoadFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LoadFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoadFileTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadFileTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp(); 
        JUnitUtil.resetInstanceManager();
        InstanceManager.setConfigureManager(new ConfigXmlManager());
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        InstanceManager.configureManagerInstance().registerConfig(
                InstanceManager.memoryManagerInstance());
    }
    
    protected void tearDown() throws Exception { 
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }
}
