package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the XNetDirectModelTimeFormatter class
 * .
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetDirectModelTimeFormatterTest {

    @Test
    public void testFormatMessage() {
        XNetDirectModelTimeFormatter formatter = new XNetDirectModelTimeFormatter();
        XNetReply reply = new XNetReply("64 25 95 36 02 C3");
        assertThat(formatter.handlesMessage(reply)).isTrue();
        assertThat(formatter.formatMessage(reply)).isEqualTo("Fast Clock: Day 4 time 21:54 Rate: 2");
    }

}
