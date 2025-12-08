package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetFeedbackReplyFormatter class.
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetFeedbackReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testTurnoutNotOperatedFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 00 47");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWoFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyNotOperated")+ "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyNotOperated");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutThrownLeftFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 05 42");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWoFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownLeft")+ "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownLeft");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutThrownRightFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 0A 4C");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWoFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownRight")+ "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownRight");
            Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutInvalidPositionFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 0F 48");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWoFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyInvalid")+ "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyInvalid");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutNotOperatedMotionCompleteFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 20 67");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyNotOperated")+ " "
             + Bundle.getMessage("XNetReplyMotionComplete") + "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyNotOperated") + " "
             + Bundle.getMessage("XNetReplyMotionComplete");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutThrownLeftMotionCompleteFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 25 62");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownLeft")+ " "
             + Bundle.getMessage("XNetReplyMotionComplete") + "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownLeft") + " "
             + Bundle.getMessage("XNetReplyMotionComplete");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutThrownRightMotionCompleteFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 2A 6C");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownRight") + " "
             + Bundle.getMessage("XNetReplyMotionComplete") + "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyThrownRight") + " "
             + Bundle.getMessage("XNetReplyMotionComplete");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testTurnoutInvalidPositionMotionCompleteFeedbackMessageHandling() {
        XNetReply r = new XNetReply("42 05 2F 68");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("TurnoutWFeedback")
             + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 21 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyInvalid")+ " "
             + Bundle.getMessage("XNetReplyMotionComplete") + "; "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
             + " " + 22 + " "
             + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
             + Bundle.getMessage("XNetReplyInvalid") + " "
             + Bundle.getMessage("XNetReplyMotionComplete");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testSensorFeedbackOffMessageHandling() {
        XNetReply r = new XNetReply("42 05 48 0F");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("XNetReplyFeedbackEncoder") + " " + 5 + " ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 1 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOff") + "; ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 2 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOff") + "; ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 3 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOff") + "; ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 4 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOn") + "; ";
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testSensorFeedbackOnMessageHandling() {
        XNetReply r = new XNetReply("42 05 57 0F");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
             Bundle.getMessage("XNetReplyFeedbackLabel") + " " +
             Bundle.getMessage("XNetReplyFeedbackEncoder") + " " + 5 + " ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 5 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOn") + "; ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 6 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOn") + "; ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 7 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOn") + "; ";
        targetString += Bundle.getMessage("XNetReplyContactLabel") + " 8 ";
        targetString += Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState"));
        targetString += " " + Bundle.getMessage("PowerStateOff") + "; ";
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetFeedbackReplyFormatter();
    }

}
