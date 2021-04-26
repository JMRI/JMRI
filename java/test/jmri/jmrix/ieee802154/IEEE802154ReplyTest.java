package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * IEEE802154ReplyTest.java
 *
 * Test for the jmri.jmrix.ieee802154.IEEE802154Reply class
 *
 * @author Paul Bender
 */
public class IEEE802154ReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new IEEE802154Reply();
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
