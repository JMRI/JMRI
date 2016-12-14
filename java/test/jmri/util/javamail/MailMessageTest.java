package jmri.util.javamail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * MailMessageTest.java
 *
 * Description: tests for the MailMessage class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MailMessageTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MailMessage constructor",new MailMessage("test@jmri.org","jmri.org","test@jmri.org"));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

