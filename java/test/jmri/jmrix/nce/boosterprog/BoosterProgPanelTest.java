package jmri.jmrix.nce.boosterprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of BoosterProgPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BoosterProgPanelTest {

    @Test
    public void testCtor() {
        BoosterProgPanel action = new BoosterProgPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
