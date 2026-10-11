package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the XNetOpsModeReadResultFormatter class
 * .
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetOpsModeReadResultFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessage() {
        XNetReply reply = new XNetReply("64 24 00 00 00 40");
        assertThat(formatter.handlesMessage(reply)).isTrue();
        assertThat(formatter.formatMessage(reply)).isEqualTo("Ops Mode: Programming Response: Address: 0 Value:0");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetOpsModeReadResultFormatter();
    }

}
