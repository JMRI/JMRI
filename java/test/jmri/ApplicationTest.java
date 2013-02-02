// ApplicationTest.java

package jmri;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Application class
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 */
public class ApplicationTest extends TestCase {

    public void testDefaultName() {
        Assert.assertEquals("Default Application name is 'JMRI'", "JMRI", Application.getApplicationName());
    }

    public void testNameChange() {
        setApplication("JMRI Testing");
        Assert.assertEquals("Changed Application name is 'JMRI Testing'", "JMRI Testing", Application.getApplicationName());
    }

    public void testSubsequentNameChange() {
        setApplication("JMRI Testing 2");
        Assert.assertEquals("Changed Application name to 'JMRI Testing 2' prevented", "JMRI Testing", Application.getApplicationName());
    }
    private static void setApplication(String name) {
        try {
            jmri.Application.setApplicationName(name);
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to set application name " + ex);
        } catch (IllegalAccessException ex) {
            log.warn("Unable to set application name " + ex);
        }
    }


    // from here down is testing infrastructure

    public ApplicationTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ApplicationTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ApplicationTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    private static final Logger log = Logger.getLogger(ApplicationTest.class.getName());

}
