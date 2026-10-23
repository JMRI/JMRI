package jmri.jmrix.dccpp.swing.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/** Tests for MessageQueue. */
public class MessageQueueTest {

    private long[] now;
    private MessageQueue queue;

    private List<String> texts() {
        List<String> list = new ArrayList<>();
        for (MessageQueue.Entry e : queue.getEntries()) {
            list.add(e.getText());
        }
        return list;
    }

    @Test
    public void testDefaults() {
        Assertions.assertEquals(30, queue.getExpirySeconds());
        Assertions.assertEquals(10, queue.getMaxLines());
        Assertions.assertTrue(queue.getEntries().isEmpty());
    }

    @Test
    public void testNewestFirst() {
        queue.add("a");
        queue.add("b");
        Assertions.assertEquals(Arrays.asList("b", "a"), texts());
    }

    @Test
    public void testExpiresAtExactlyExpiry() {
        queue.setExpirySeconds(30);
        queue.add("a");
        now[0] = 29_999;
        Assertions.assertFalse(queue.expire());
        Assertions.assertEquals(1, queue.getEntries().size());
        now[0] = 30_000;
        Assertions.assertTrue(queue.expire());
        Assertions.assertEquals(0, queue.getEntries().size());
    }

    @Test
    public void testZeroExpiryNeverExpires() {
        queue.setExpirySeconds(0);
        queue.add("a");
        now[0] = 10_000_000;
        Assertions.assertFalse(queue.expire());
        Assertions.assertEquals(1, queue.getEntries().size());
    }

    @Test
    public void testMaxLinesTrimsOldest() {
        queue.setMaxLines(3);
        for (int i = 1; i <= 5; i++) {
            queue.add(Integer.toString(i));
        }
        Assertions.assertEquals(Arrays.asList("5", "4", "3"), texts());
    }

    @Test
    public void testShrinkMaxLinesTrimsImmediately() {
        for (int i = 1; i <= 5; i++) {
            queue.add(Integer.toString(i));
        }
        queue.setMaxLines(2);
        Assertions.assertEquals(Arrays.asList("5", "4"), texts());
    }

    @Test
    public void testMaxLinesMinimumIsOne() {
        queue.setMaxLines(0);
        Assertions.assertEquals(1, queue.getMaxLines());
    }

    @Test
    public void testChangeExpiryAppliesToExisting() {
        queue.add("a");
        now[0] = 15_000;
        queue.setExpirySeconds(10);
        Assertions.assertTrue(queue.expire());
        Assertions.assertEquals(0, queue.getEntries().size());
    }

    @Test
    public void testEntryKeepsTimestamp() {
        now[0] = 1234;
        queue.add("a");
        Assertions.assertEquals(1234, queue.getEntries().get(0).getTimeMillis());
    }

    @Test
    public void testClear() {
        queue.add("a");
        queue.clear();
        Assertions.assertTrue(queue.getEntries().isEmpty());
    }

    @Test
    public void testEntriesUnmodifiable() {
        queue.add("a");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.getEntries().clear());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        now = new long[]{0};
        queue = new MessageQueue(() -> now[0]);
    }

    @AfterEach
    public void tearDown() {
        queue = null;
        now = null;
        JUnitUtil.tearDown();
    }

}
