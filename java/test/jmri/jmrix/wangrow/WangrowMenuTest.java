package jmri.jmrix.wangrow;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;


/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WangrowMenuTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        WangrowMenu t = new WangrowMenu(new NceSystemConnectionMemo());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WangrowMenuTest.class);

}
