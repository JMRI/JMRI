package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractMRMessageTest extends AbstractMessageTestBase {

    @Test
    public void testSentTime() {
        AbstractMRMessage msg = (AbstractMRMessage) m;
        Assertions.assertFalse(msg.hasSentTime(), "not recorded before it is written");
        Assertions.assertEquals(0L, msg.getSentTime(), "0 when not recorded");

        msg.setSentTime(1234L);
        Assertions.assertTrue(msg.hasSentTime(), "recorded");
        Assertions.assertEquals(1234L, msg.getSentTime());

        msg.setSentTime(5678L);
        Assertions.assertEquals(5678L, msg.getSentTime(), "written again: the latest time");

        AbstractMRMessage copy = new AbstractMRMessage(msg) {
        };
        Assertions.assertFalse(copy.hasSentTime(), "a copy is a new message, not yet sent");
    }

    @Test
    public void testSentListener() {
        AbstractMRMessage msg = (AbstractMRMessage) m;
        Assertions.assertNull(msg.getSentListener(), "none by default");

        AbstractMRMessage.SentListener l = (message, timeNanos) -> {
        };
        msg.setSentListener(l);
        Assertions.assertSame(l, msg.getSentListener());

        AbstractMRMessage copy = new AbstractMRMessage(msg) {
        };
        Assertions.assertNull(copy.getSentListener(), "not kept by a copy");

        msg.setSentListener(null);
        Assertions.assertNull(msg.getSentListener(), "removed");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new AbstractMRMessage(5) {
        };
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(AbstractMRMessageTest.class);

}
