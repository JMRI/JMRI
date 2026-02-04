package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        Assertions.assertNotNull( t, "exists");
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
