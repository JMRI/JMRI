package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConstantsTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testCp290Constants() {
        Assertions.assertEquals( 0x02,Constants.CMD_ON);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConstantsTest.class);

}
