// AddSignalMastPanelTest.java

package jmri.jmrit.beantable.signalmast;

import javax.swing.JFrame;

import java.io.*;

import junit.framework.*;

/**
 * @author	Bob Jacobsen  Copyright 2014
 * @version	$Revision$
 */
public class AddSignalMastPanelTest extends TestCase {

    public void testDefaultSystems() {
        AddSignalMastPanel  a = new AddSignalMastPanel();
        
        jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");
        jmri.util.JUnitAppender.assertWarnMessage("No Configuration file set, unable to save or load user preferences");
        
        // check that "Basic Model Signals" (basic directory) system is present
        boolean found = false;
        for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
            if (a.sigSysBox.getItemAt(i).equals("Basic Model Signals")) {
                found = true;
            }
        }
        Assert.assertTrue("found Basic Model Signals", found);
    }


    public void testSearch() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            createMockSystem();
        
            AddSignalMastPanel  a = new AddSignalMastPanel();
        
            jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");
            jmri.util.JUnitAppender.assertWarnMessage("No Configuration file set, unable to save or load user preferences");
        
            // check that "Basic Model Signals" (basic directory) system is present
            boolean found = false;
            for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
                if (a.sigSysBox.getItemAt(i).equals("JUnit Test Signals")) {
                    found = true;
                }
            }
            Assert.assertTrue("found JUnit Test Signals", found);
        } finally {
            deleteMockSystem();
        }
    }

    String path = jmri.util.FileUtil.getUserFilesPath()+File.separator+"resources";
    String dummy = "JUnitTestSignals"; // something that won't exist
    
    void createMockSystem() throws IOException {
        // creates mock (no appearances) system
        // in the user area.
        InputStream in = null;
        OutputStream out = null;
        try {
            new File(path).mkdir(); // might already exist
            new File(path+File.separator+"signals").mkdir();  // already exists if using signals
            new File(path+File.separator+"signals"+File.separator+"JUnitTestSignals").mkdir(); // assume doesn't exist, or at least belongs to us
            // copy file
            in = new FileInputStream(new File("java/test/jmri/jmrit/beantable/signalmast/testAspects.xml"));
            out = new FileOutputStream(new File(path+File.separator+"signals"+File.separator+"JUnitTestSignals"+File.separator+"aspects.xml"));
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();            
        }
    }
    
    void deleteMockSystem() throws IOException {
        new File(path+File.separator+"signals"+File.separator+"JUnitTestSignals"+File.separator+"aspects.xml").delete();
        new File(path+File.separator+"signals"+File.separator+"JUnitTestSignals").delete();
    }
    
    // from here down is testing infrastructure

    public AddSignalMastPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AddSignalMastPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AddSignalMastPanelTest.class);

        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp(); 

        jmri.util.JUnitUtil.resetInstanceManager(); 
        jmri.util.JUnitUtil.initInternalTurnoutManager(); 
        jmri.util.JUnitUtil.initInternalLightManager(); 
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.managers.DefaultUserMessagePreferences.resetInstance();
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
    }
    protected void tearDown() throws Exception { 
        jmri.util.JUnitUtil.resetInstanceManager(); 

        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }
    
}
