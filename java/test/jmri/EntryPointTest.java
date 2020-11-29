package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EntryPointTest {

    @Test
    public void testCTor() {
        Block a = new Block("a");
        Block b = new Block("b");
        EntryPoint t = new EntryPoint(a,b,"forward");
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

    // private final static Logger log = LoggerFactory.getLogger(EntryPointTest.class);

}
