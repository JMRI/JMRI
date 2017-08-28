package jmri.jmrit.conditional;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
