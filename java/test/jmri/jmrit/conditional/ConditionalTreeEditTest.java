package jmri.jmrit.conditional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/*
* Tests for the ConditionalTreeEdit Class
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalTreeEditTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConditionalTreeEdit Constructor Return", new ConditionalTreeEdit());  // NOI18N
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
