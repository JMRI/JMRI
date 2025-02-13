package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLocoInfoNormalUnitReplyFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetLocoInfoNormalUnitReplyFormatterTest {

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode128Speed0Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 04 00 04 00 E4");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 128 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                    formatter.formatMessage(r));
        }

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode128Speed3Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 04 04 04 00 E0");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals(
                    "Locomotive Information Response: Normal Unit,Reverse,in 128 Speed Step Mode,Speed Step: 3. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                    formatter.formatMessage(r));
        }

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode28Speed0Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 0A 00 04 00 EA");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 28 Speed Step Mode,Speed Step: 0. Address in use by another device. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                    formatter.formatMessage(r));
        }

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode28Speed17Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 0A 0A 04 00 E0");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 28 Speed Step Mode,Speed Step: 17. Address in use by another device. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                    formatter.formatMessage(r));
        }
        @Test
        public void testToMonitorStringNormalLocoInfoSSMode27Speed0Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 01 00 1F FF F5");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 27 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 On; F1 On; F2 On; F3 On; F4 On; F5 On; F6 On; F7 On; F8 On; F9 On; F10 On; F11 On; F12 On; ",
                    formatter.formatMessage(r));
        }

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode27Speed7Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 01 05 1F FF F0");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 27 Speed Step Mode,Speed Step: 7. Address is Free for Operation. F0 On; F1 On; F2 On; F3 On; F4 On; F5 On; F6 On; F7 On; F8 On; F9 On; F10 On; F11 On; F12 On; ",
                    formatter.formatMessage(r));
        }

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode14Speed0Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 00 00 00 00 E4");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 14 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                    formatter.formatMessage(r));
        }

        @Test
        public void testToMonitorStringNormalLocoInfoSSMode14Speed3Response() {
            XNetLocoInfoNormalUnitReplyFormatter formatter = new XNetLocoInfoNormalUnitReplyFormatter();
            XNetReply r = new XNetReply("E4 00 04 00 00 E0");
            Assertions.assertTrue(formatter.handlesMessage(r));
            Assertions.assertEquals("Locomotive Information Response: Normal Unit,Reverse,in 14 Speed Step Mode,Speed Step: 3. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                    formatter.formatMessage(r));
        }

}
