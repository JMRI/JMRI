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
        XNetOpsModeReadResultFormatter formatter = new XNetOpsModeReadResultFormatter();
        XNetReply reply = new XNetReply("64 24 00 00 00 40");
        assertThat(formatter.handlesMessage(reply)).isTrue();
        assertThat(formatter.formatMessage(reply)).isEqualTo("Ops Mode: Programming Response: Address: 0 Value:0");
    }

}
