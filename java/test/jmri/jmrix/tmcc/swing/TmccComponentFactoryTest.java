package jmri.jmrix.tmcc.swing;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
