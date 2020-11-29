package jmri.util.junit;

import org.junit.Assert;
import org.junit.jupiter.api.*;
/**
 * Ensure Java's assert() works with JMRI infrastructure
 * 
 * For a discussion, see https://stackoverflow.com/questions/2758224/what-does-the-java-assert-keyword-do-and-when-should-it-be-used
 * 
 * This should be run with and without the -ea (or -enableassertions) runtime option for complete coverage
 *
 * @author Bob Jacobsen Copyright 2018
 */


public class AssertTest {

    @Test
    public void assertPasses() {
        assert true ;
        // assert(true) always drops through
    }
    
    @Test
    public void assertDisplay() {
        // show the assert status
        log.info("AssertTest: assert are {}", assertsEnabled ? "enabled" : "disabled");
        System.err.println("AssertTest: asserts are "+(assertsEnabled ? "enabled" : "disabled"));
    }

    @Test
    public void assertFails() {
        try {
            assert false ;
        } catch (AssertionError e) {
            // assert(false) asserts when enabled
            Assert.assertTrue("don't fail if not enabled", assertsEnabled);
            return;
        }
        Assert.assertFalse("fail if enabled", assertsEnabled);
    }
    
    // assert doesn't evaluate argument if not enabled
    @Test
    public void assertEvaluateParameter() {
        itRan = false;
        assert isParameterRun();
        if (assertsEnabled) {
            Assert.assertTrue("Evaluate parameter if asserts enabled", itRan);
        } else {
            Assert.assertFalse("don't evaluate parameter if asserts disabled", itRan);
        }
    }
    // service routine that notes if it's been run
    public boolean isParameterRun() { 
        itRan = true;
        return true;
    }
    boolean itRan; // flag for running

    // initialized in setUp
    boolean assertsEnabled;
    
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        
        assertsEnabled = false;

        assert localAssign(); // Intentional side-effect if assert is enabled
        
        // Now assertsEnabled is set to the correct value
    }

    boolean localAssign() {
        assertsEnabled = true; // Intentional side-effect if assert is enabled
        return true;
    }
    
    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssertTest.class);
}
