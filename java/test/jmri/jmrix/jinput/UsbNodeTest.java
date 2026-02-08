package jmri.jmrix.jinput;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
public class UsbNodeTest {

    @Test
    public void testCTor() {
        UsbNode t = new UsbNode("test",null,null);
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbNodeTest.class);

}
