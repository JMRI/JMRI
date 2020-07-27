package jmri.jmrix.sprog;

import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SprogMessage class.
 *
 * @author Bob Jacobsen Copyright 2012
 */
public class SprogMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private SprogMessage msg = null;

    @Test
    public void testReadCv() {
        msg = SprogMessage.getReadCV(12, ProgrammingMode.PAGEMODE);
        Assert.assertEquals("string compare ", "V 0012", msg.toString());
    }

    @Test
    public void testWriteCV() {
        msg = SprogMessage.getWriteCV(12, 251, ProgrammingMode.PAGEMODE);
        Assert.assertEquals("string compare ", "V 0012 251", msg.toString());
    }

    @Test
    public void testReadCvLarge() {
        msg = SprogMessage.getReadCV(1021, ProgrammingMode.PAGEMODE);
        Assert.assertEquals("string compare ", "V 1021", msg.toString());
    }

    @Test
    public void testWriteCVLarge() {
        msg = SprogMessage.getWriteCV(1021, 251, ProgrammingMode.PAGEMODE);
        Assert.assertEquals("string compare ", "V 1021 251", msg.toString());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new SprogMessage(1);
    }

    @AfterEach
    public void tearDown() {
        m = msg = null;
        JUnitUtil.tearDown();
    }

}
