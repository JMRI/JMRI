package jmri.jmrix.marklin.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.event.ActionEvent;
import java.util.List;

import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinSendBootAction.
 *
 * @author JMRI Community
 */
public class MarklinSendBootActionTest {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;

    @Test
    public void testCTorWithName() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Boot Action", memo);
        assertNotNull(action, "Action created");
        assertEquals("Test Boot Action", action.getValue(MarklinSendBootAction.NAME), "Action name");
    }

    @Test
    public void testCTorDefaultName() {
        MarklinSendBootAction action = new MarklinSendBootAction(memo);
        assertNotNull(action, "Action created");
        assertNotNull(action.getValue(MarklinSendBootAction.NAME), "Action has default name");
    }

    @Test
    public void testCTorWithNullMemo() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Action", null);
        assertNotNull(action, "Action created with null memo");
    }

    @Test
    public void testActionPerformed() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Boot Action", memo);

        // Verify no messages sent initially
        assertEquals(0, tc.getSentMessages().size(), "No initial messages");

        // Trigger the action
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");
        action.actionPerformed(event);

        // Verify message was sent
        List<MarklinMessage> sentMessages = tc.getSentMessages();
        assertEquals(1, sentMessages.size(), "One message sent");

        // Verify the message is a CAN BOOT message
        MarklinMessage sentMessage = sentMessages.get(0);
        assertNotNull(sentMessage, "Message exists");

        // Compare with expected CAN BOOT message
        MarklinMessage expectedMessage = MarklinMessage.getCanBoot();
        assertEquals(expectedMessage.getNumDataElements(), sentMessage.getNumDataElements(),
                "Message length matches");

        // Check that the sent message has the same structure as the expected CAN BOOT message
        for (int i = 0; i < expectedMessage.getNumDataElements(); i++) {
            assertEquals(expectedMessage.getElement(i), sentMessage.getElement(i), "Byte " + i + " matches");
        }
    }

    @Test
    public void testActionPerformedWithNullMemo() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Action", null);
        
        // This should not throw an exception and should not send any messages
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");
        action.actionPerformed(event);
        
        // Verify no messages were sent (since memo is null)
        assertEquals(0, tc.getSentMessages().size(), "No messages sent");
        JUnitAppender.assertWarnMessage("Cannot send CanBoot message - no connection available");
    }

    @Test
    public void testActionPerformedWithNullTrafficController() {
        MarklinSystemConnectionMemo memoWithNullTC = new MarklinSystemConnectionMemo();
        // Don't set a traffic controller - it will be null
        
        MarklinSendBootAction action = new MarklinSendBootAction("Test Action", memoWithNullTC);
        
        // This should not throw an exception and should not send any messages
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");
        action.actionPerformed(event);
        
        // Verify no messages were sent (since traffic controller is null)
        assertEquals(0, tc.getSentMessages().size(), "No messages sent");
        JUnitAppender.assertWarnMessage("Cannot send CanBoot message - no connection available");
    }

    @Test
    public void testMultipleActionCalls() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Boot Action", memo);
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");

        // Send multiple messages
        action.actionPerformed(event);
        action.actionPerformed(event);
        action.actionPerformed(event);

        // Verify all messages were sent
        List<MarklinMessage> sentMessages = tc.getSentMessages();
        assertEquals(3, sentMessages.size(), "Three messages sent");

        // Verify all are CAN BOOT messages
        MarklinMessage expectedMessage = MarklinMessage.getCanBoot();
        for (MarklinMessage sentMessage : sentMessages) {
            for (int i = 0; i < expectedMessage.getNumDataElements(); i++) {
                assertEquals(expectedMessage.getElement(i), sentMessage.getElement(i),
                        "Message byte " + i + " matches expected");
            }
        }
    }

    @Test
    public void testCanBootMessageStructure() {
        // Test the structure of the CAN BOOT message itself
        MarklinMessage bootMessage = MarklinMessage.getCanBoot();
        assertNotNull(bootMessage, "CAN BOOT message created");

        // Based on the implementation, verify the message structure
        // Command 0x1B: (0x1B >> 7) & 0xFF = 0x00, (0x1B << 1) & 0xFF = 0x36
        assertEquals(0x00, bootMessage.getElement(0), "Element 0 (high bits)");
        assertEquals(0x36, bootMessage.getElement(1), "Element 1 (low bits)");

        // Hash bytes should be from MarklinConstants
        assertEquals(0x47, bootMessage.getElement(2), "Element 2 (hash byte 1)"); // MarklinConstants.HASHBYTE1
        assertEquals(0x11, bootMessage.getElement(3), "Element 3 (hash byte 2)"); // MarklinConstants.HASHBYTE2

        // DLC should be 5
        assertEquals(0x05, bootMessage.getElement(4), "Element 4 (DLC)");

        // Elements 5-8 should be 0 (address bytes for broadcast)
        for (int i = 5; i <= 8; i++) {
            assertEquals(0x00, bootMessage.getElement(i), "Element " + i + " should be 0");
        }

        // Element 9 (data byte 0) should be 0x11 (magic value for Gleisbox activation)
        assertEquals(0x11, bootMessage.getElement(9), "Element 9 (data byte 0)");

        // Remaining elements should be 0
        for (int i = 10; i < bootMessage.getNumDataElements(); i++) {
            assertEquals(0x00, bootMessage.getElement(i), "Element " + i + " should be 0");
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        // Create traffic controller scaffold for testing
        tc = new MarklinTrafficControlScaffold();
        
        // Create system connection memo with the scaffold
        memo = new MarklinSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        if (tc != null) {
            tc.terminateThreads();
        }
        if (memo != null) {
            memo.dispose();
        }
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }
}
