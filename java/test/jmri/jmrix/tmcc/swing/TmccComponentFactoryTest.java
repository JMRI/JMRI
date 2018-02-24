package jmri.jmrix.tmcc.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TmccComponentFactoryTest {

    @Test
    public void testCTor() {
        TmccSystemConnectionMemo memo = new TmccSystemConnectionMemo();
        TmccComponentFactory t = new TmccComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
