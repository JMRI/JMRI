package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IEEE802154MessageTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154Message class
 *
 * @author	Paul Bender
 */
public class IEEE802154MessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @Test
    public void testCtor() {
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new IEEE802154Message(3);
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

}
