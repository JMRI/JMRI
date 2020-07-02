package jmri.jmrit.dualdecoder;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DualDecoderToolActionTest {

    @Test
    public void testCTor() {
        DualDecoderToolAction t = new DualDecoderToolAction();
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

    // private final static Logger log = LoggerFactory.getLogger(DualDecoderToolActionTest.class);

}
