package jmri.jmrit.mastbuilder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MastBuilderActionTest {

    @Test
    public void testCTor() {
        MastBuilderAction t = new MastBuilderAction();
        Assert.assertNotNull("exists",t);
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
