package jmri.jmrix.nce.ncemon;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceMonBinaryTest {

    @Test
    public void testCTor() {
        NceMonBinary t = NceMonBinary.INSTANCE;
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

    // private static final Logger log = LoggerFactory.getLogger(NceMonBinaryTest.class);

}
