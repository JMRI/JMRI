package jmri.jmrit.ussctc;

import jmri.*;
import jmri.util.*;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Demo of classes in jmri.jmrit.ussctc
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2015, 2017
  */
public class PackageDemo {

    public PackageDemo(String s) {
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initShutDownManager();
        JUnitUtil.resetProfileManager();

        try {
            InstanceManager.getDefault(jmri.ConfigureManager.class)
                    .load(new java.io.File("java/test/jmri/jmrit/ussctc/PackageDemo.xml"));
        } catch (Exception e) { System.err.println(e); }
        
        // wait for Swing to end
        Thread.getAllStackTraces().keySet().forEach((t) -> 
            { 
                if (t.getName().startsWith("AWT-EventQueue")) {  
                    try {
                        t.join();  // Wait for AWT to end on last window deleted
                    } catch (Exception e) {}
                }
            });
    }

}
