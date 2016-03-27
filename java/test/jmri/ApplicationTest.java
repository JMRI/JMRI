package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the Application class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class ApplicationTest extends TestCase {

    public void testSetName() {
        // test default
        Assert.assertEquals("Default Application name is 'JMRI'", "JMRI", Application.getApplicationName());

        // test ability to change
        setApplication("JMRI Testing");
        Assert.assertEquals("Changed Application name is 'JMRI Testing'", "JMRI Testing", Application.getApplicationName());

        // test failure on 2nd change
        setApplication("JMRI Testing 2");
        Assert.assertEquals("Changed Application name to 'JMRI Testing 2' prevented", "JMRI Testing", Application.getApplicationName());
        jmri.util.JUnitAppender.assertWarnMessage("Unable to set application name java.lang.IllegalAccessException: Application name cannot be modified once set.");
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
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class.getName());

}
