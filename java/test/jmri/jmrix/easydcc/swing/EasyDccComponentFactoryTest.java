package jmri.jmrix.easydcc.swing;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EasyDccComponentFactoryTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccSystemConnectionMemo memo = new EasyDccSystemConnectionMemo();
        EasyDccComponentFactory t = new EasyDccComponentFactory(memo);
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
