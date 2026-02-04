package jmri.jmrix.loconet.duplexgroup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnDplxGrpInfoImplConstantsTest {

    // no Ctor test, tested class only supplies static methods

    @Test
    public void testLnDplxGrpInfoImplConstants() {
        assertEquals( 11, LnDplxGrpInfoImplConstants.DPLX_MIN_CH);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnDplxGrpInfoImplConstantsTest.class);

}
