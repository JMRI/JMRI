// CheckProgrammerNames.java

package jmri.jmrit.symbolicprog.tabbedframe;

import java.io.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrit.XmlFile;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2007, 2008
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class CheckProgrammerNames extends jmri.util.swing.GuiUtilBaseTest {
    
    public void testAdvanced() {
        checkAgainstNames(new File("xml/programmers/Advanced.xml"));
    }
    
    public void testComprehensive() {
        checkAgainstNames(new File("xml/programmers/Comprehensive.xml"));
    }
    
    public void testBasic() {
        checkAgainstNames(new File("xml/programmers/Basic.xml"));
    }
    
    public void testTrainShowBasic() {
        checkAgainstNames(new File("xml/programmers/TrainShowBasic.xml"));
    }
    
    public void testSampleClub() {
        checkAgainstNames(new File("xml/programmers/Sample Club.xml"));
    }
    
    public void testCustom() {
        checkAgainstNames(new File("xml/programmers/Custom.xml"));
    }
    
    public void testTutorial() {
        checkAgainstNames(new File("xml/programmers/Tutorial.xml"));
    }
    
    public void testRegisters() {
        checkAgainstNames(new File("xml/programmers/Registers.xml"));
    }
    
/*     public void testESU() { */
/*         checkAgainstNames(new File("xml/programmers/ESU.xml")); */
/*     } */
    
/*     public void testZimo() { */
/*         checkAgainstNames(new File("xml/programmers/Zimo.xml")); */
/*     } */
    
    public void testComprehensiveComplete() {
        checkComplete(new File("xml/programmers/Comprehensive.xml"));
    }
    
    public void testAdvancedComplete() {
        checkComplete(new File("xml/programmers/Advanced.xml"));
    }
    
    
    
    
    public void testAdvancedValidation() {
        validate(new File("xml/programmers/Advanced.xml"));
    }

    public void testComprehensiveValidation() {
        validate(new File("xml/programmers/Comprehensive.xml"));
    }
    
    public void testBasicValidation() {
        validate(new File("xml/programmers/Basic.xml"));
    }
    
    public void testTrainShowBasicValidation() {
        validate(new File("xml/programmers/TrainShowBasic.xml"));
    }
    
    public void testSampleClubValidation() {
        validate(new File("xml/programmers/Sample Club.xml"));
    }
    
    public void testCustomValidation() {
        validate(new File("xml/programmers/Custom.xml"));
    }
    
    public void testTutorialValidation() {
        validate(new File("xml/programmers/Tutorial.xml"));
    }
    
    public void testRegistersValidation() {
        validate(new File("xml/programmers/Registers.xml"));
    }
    
    public void testESUValidation() {
        validate(new File("xml/programmers/ESU.xml"));
    }
    
    public void testZimoValidation() {
        validate(new File("xml/programmers/Zimo.xml"));
    }
    
    public void testValidateAllParts() {
        doDirectory("xml/programmers");
    }

    // utilities
    
    public void checkAgainstNames(File file) {
        String result = ProgCheckAction.checkMissingNames(file);
        if (!result.equals(""))
            Assert.fail(result);
    }
    
    public void checkComplete(File file) {
        String result = ProgCheckAction.checkIncompleteComprehensive(file);
        if (!result.equals(""))
            Assert.fail(result);
    }
        
    // from here down is testing infrastructure

    public CheckProgrammerNames(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CheckProgrammerNames.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CheckProgrammerNames.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CheckProgrammerNames.class.getName());
    
}
