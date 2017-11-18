package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LnDplxGrpInfoImpl
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LnDplxGrpInfoImplTest {

    @Test
    public void testCtor() {
        LnDplxGrpInfoImpl action = new LnDplxGrpInfoImpl(new jmri.jmrix.loconet.LocoNetSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
