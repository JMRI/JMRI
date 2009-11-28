// LoadFileTextBase.java

package jmri.configurexml;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.Assert;
import junit.framework.TestCase;

import jmri.util.JUnitUtil;
import jmri.InstanceManager;

/**
 * Base for load/store testing
 * 
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision: 1.2 $
 */
public class LoadFileTestBase extends TestCase {

    public LoadFileTestBase(String s) {
        super(s);
    }

    // testing services
    public void validate(File file) {
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
        } catch (Exception ex) {
            XmlFile.setVerify(original);
            Assert.fail(ex.toString());
            return;
        } finally {
            XmlFile.setVerify(original);
        }
    }

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
