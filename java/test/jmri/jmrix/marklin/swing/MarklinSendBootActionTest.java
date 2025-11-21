package jmri.jmrix.marklin.swing;

import java.awt.event.ActionEvent;
import java.util.List;

import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
        Assert.assertNotNull("Action created", action);
        Assert.assertEquals("Action name", "Test Boot Action", action.getValue(MarklinSendBootAction.NAME));
    }

    @Test
    public void testCTorDefaultName() {
        MarklinSendBootAction action = new MarklinSendBootAction(memo);
        Assert.assertNotNull("Action created", action);
        Assert.assertNotNull("Action has default name", action.getValue(MarklinSendBootAction.NAME));
    }

    @Test
    public void testCTorWithNullMemo() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Action", null);
        Assert.assertNotNull("Action created with null memo", action);
    }

    @Test
    public void testActionPerformed() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Boot Action", memo);
        
        // Verify no messages sent initially
        Assert.assertEquals("No initial messages", 0, tc.getSentMessages().size());
        
        // Trigger the action
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");
        action.actionPerformed(event);
        
        // Verify message was sent
        List<MarklinMessage> sentMessages = tc.getSentMessages();
        Assert.assertEquals("One message sent", 1, sentMessages.size());
        
        // Verify the message is a CAN BOOT message
        MarklinMessage sentMessage = sentMessages.get(0);
        Assert.assertNotNull("Message exists", sentMessage);
        
        // Compare with expected CAN BOOT message
        MarklinMessage expectedMessage = MarklinMessage.getCanBoot();
        Assert.assertEquals("Message length matches", expectedMessage.getNumDataElements(), sentMessage.getNumDataElements());
        
        // Check that the sent message has the same structure as the expected CAN BOOT message
        for (int i = 0; i < expectedMessage.getNumDataElements(); i++) {
            Assert.assertEquals("Byte " + i + " matches", expectedMessage.getElement(i), sentMessage.getElement(i));
        }
    }

    @Test
    public void testActionPerformedWithNullMemo() {
        MarklinSendBootAction action = new MarklinSendBootAction("Test Action", null);
        
        // This should not throw an exception and should not send any messages
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");
        action.actionPerformed(event);
        
        // Verify no messages were sent (since memo is null)
        Assert.assertEquals("No messages sent", 0, tc.getSentMessages().size());
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
        Assert.assertEquals("No messages sent", 0, tc.getSentMessages().size());
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
        Assert.assertEquals("Three messages sent", 3, sentMessages.size());
        
        // Verify all are CAN BOOT messages
        MarklinMessage expectedMessage = MarklinMessage.getCanBoot();
        for (MarklinMessage sentMessage : sentMessages) {
            for (int i = 0; i < expectedMessage.getNumDataElements(); i++) {
                Assert.assertEquals("Message byte " + i + " matches expected", 
                                  expectedMessage.getElement(i), sentMessage.getElement(i));
            }
        }
    }

    @Test
    public void testCanBootMessageStructure() {
        // Test the structure of the CAN BOOT message itself
        MarklinMessage bootMessage = MarklinMessage.getCanBoot();
        Assert.assertNotNull("CAN BOOT message created", bootMessage);
        
        // Based on the implementation, verify the message structure
        // Command 0xB1: (0xB1 >> 7) & 0xFF = 0x01, (0xB1 << 1) & 0xFF = 0x62
        Assert.assertEquals("Element 0 (high bits)", 0x01, bootMessage.getElement(0));
        Assert.assertEquals("Element 1 (low bits)", 0x62, bootMessage.getElement(1));
        
        // Hash bytes should be from MarklinConstants
        Assert.assertEquals("Element 2 (hash byte 1)", 0x47, bootMessage.getElement(2)); // MarklinConstants.HASHBYTE1
        Assert.assertEquals("Element 3 (hash byte 2)", 0x11, bootMessage.getElement(3)); // MarklinConstants.HASHBYTE2
        
        // DLC should be 0
        Assert.assertEquals("Element 4 (DLC)", 0x00, bootMessage.getElement(4));
        
        // Remaining elements should be 0
        for (int i = 5; i < bootMessage.getNumDataElements(); i++) {
            Assert.assertEquals("Element " + i + " should be 0", 0x00, bootMessage.getElement(i));
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
