package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * IEEE802154MessageTest.java
 *
 * Test for the jmri.jmrix.ieee802154.IEEE802154Message class
 *
 * @author Paul Bender
 */
public class IEEE802154MessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @Test
    public void testCtor() {
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new IEEE802154Message(3);
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
