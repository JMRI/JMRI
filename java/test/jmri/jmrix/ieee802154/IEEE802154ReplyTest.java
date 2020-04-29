package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * IEEE802154ReplyTest.java
 *
 * Test for the jmri.jmrix.ieee802154.IEEE802154Reply class
 *
 * @author Paul Bender
 */
public class IEEE802154ReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new IEEE802154Reply();
    }

    @After
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
