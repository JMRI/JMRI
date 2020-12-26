package jmri.jmrix.ieee802154.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IEEE802154MenuTest {

    @Test
    public void testCTor() {
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo = new jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo();
        IEEE802154Menu t = new IEEE802154Menu("IEEE 802.15.4 test menu",memo);
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

    // private final static Logger log = LoggerFactory.getLogger(IEEE802154MenuTest.class);

}
