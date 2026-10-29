package jmri.jmrix.marklin;

import jmri.Light;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Marklin light commands and feedback.
 */
public class MarklinLightTest extends jmri.implementation.AbstractLightTestBase {

    private MarklinTrafficControlScaffold tc;
    private MarklinSystemConnectionMemo memo;

    @Override
    public int numListeners() {
        return tc.getNumberListeners();
    }

    @Override
    public void checkOnMsgSent() {
        assertEquals("00 16 03 00 06 00 00 38 00 01 01 00 00",
                tc.getSentMessages().get(0).toString());
    }

    @Override
    public void checkOffMsgSent() {
        assertEquals("00 16 03 00 06 00 00 38 00 00 01 00 00",
                tc.getSentMessages().get(0).toString());
    }

    @ParameterizedTest
    @CsvSource({"1, 14336", "DCC1, 14336", "DCC45, 14380", "MM123, 12410",
        "CAN7168, 7168", "CAN0, 0", "CAN305419896, 305419896"})
    public void testProtocolCommandsAndFeedback(String address, int wireAddress) {
        MarklinLight light = new MarklinLight("MCL" + address, "test light", "MC", tc);
        try {
            for (int state : new int[]{Light.ON, Light.OFF}) {
                int start = tc.getSentMessages().size();
                int position = state == Light.ON ? 1 : 0;
                light.setState(state);
                assertEquals(MarklinMessage.getSetTurnout(wireAddress, position, 1).toString(),
                        tc.getSentMessages().get(start).toString());
                JUnitUtil.waitFor(() -> tc.getSentMessages().size() == start + 2, "output released");
                assertEquals(MarklinMessage.getSetTurnout(wireAddress, position, 0).toString(),
                        tc.getLastMessageSent().toString());
                assertEquals(state, light.getState(), "release must not change the light state");
            }

            MarklinReply reply = new MarklinReply();
            reply.setCommand(MarklinConstants.ACCCOMMANDSTART);
            reply.setAddress(wireAddress);
            int sent = tc.getSentMessages().size();
            reply.setElement(9, 1);
            light.reply(reply);
            assertEquals(Light.ON, light.getState());
            reply.setElement(9, 0);
            light.reply(reply);
            assertEquals(Light.OFF, light.getState());
            assertEquals(sent, tc.getSentMessages().size(), "feedback must not echo commands");
        } finally {
            light.dispose();
        }
    }

    @Test
    public void testFeedbackFiltering() {
        MarklinLight light = (MarklinLight) t;
        MarklinReply reply = new MarklinReply();
        reply.setCommand(MarklinConstants.ACCCOMMANDSTART);
        reply.setAddress(MarklinConstants.MM1ACCSTART);
        reply.setElement(9, 1);
        light.reply(reply);
        assertEquals(Light.OFF, light.getState(), "same number in another protocol is unrelated");
        reply.setAddress(MarklinConstants.DCCACCSTART);
        reply.setCommand(MarklinConstants.ACCCOMMANDEND);
        light.reply(reply);
        assertEquals(Light.OFF, light.getState(), "configuration messages are not switching feedback");
        reply.setCommand(MarklinConstants.ACCCOMMANDSTART);
        reply.setElement(0, 0x10);
        light.reply(reply);
        assertEquals(Light.OFF, light.getState(), "wrong priority");
        assertEquals(0, tc.getSentMessages().size());
    }

    @Test
    public void testInvalidStateDoesNotSend() {
        assertThrows(IllegalArgumentException.class, () -> t.setState(Light.UNKNOWN));
        assertTrue(tc.getSentMessages().isEmpty());
    }

    @Test
    public void testDisposeReleasesOutput() {
        t.setState(Light.ON);
        t.dispose();
        assertEquals("00 16 03 00 06 00 00 38 00 01 00 00 00", tc.getLastMessageSent().toString());
        assertEquals(0, numListeners());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        t = new MarklinLight("ML1", null, "M", tc);
    }

    @AfterEach
    public void tearDown() {
        t.dispose();
        memo.dispose();
        tc.dispose();
        JUnitUtil.tearDown();
    }
}
