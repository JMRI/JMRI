package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the Application class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class ApplicationTest {

    @Test
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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetApplication();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetApplication();
        jmri.util.JUnitUtil.tearDown();
    }

    private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

}
