package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * IEEE802154ReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154Reply class
 *
 * @author	Paul Bender
 */
public class IEEE802154ReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
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
