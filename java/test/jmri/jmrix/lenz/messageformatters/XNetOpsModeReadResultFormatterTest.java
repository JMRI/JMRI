package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the XNetDirectModelTimeFormatter class
 * .
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetOpsModeReadResultFormatterTest {

    @Test
    public void testFormatMessage() {
        XNetBCModelTimeFormatter formatter = new XNetBCModelTimeFormatter();
        XNetReply reply = new XNetReply("64 24 00 00 00 40");
        assertThat(formatter.handlesMessage(reply)).isTrue();
        assertThat(formatter.formatMessage(reply)).isEqualTo("Fast Clock Broadcast: Day 4 time 21:54 Rate: 2");
    }

}
