package jmri.util.junit;

import org.junit.*;
/**
 * Check out Java's assert() works with JMRI
 * 
 * For a discussion, see https://stackoverflow.com/questions/2758224/what-does-the-java-assert-keyword-do-and-when-should-it-be-used
 *
 * @author	Bob Jacobsen Copyright 2018
 */


public class AssertTest {

    @Test
    public void assertPasses() {
        assert true ;
        System.err.println("assert(true) drops through");
    }
    
    // assert doesn't fail unless run-time option added
    // that needs to be added to our test support
    @Test
    public void assertFails() {
        assert false ;
        System.err.println("assert(false) drops through");
    }
    
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
