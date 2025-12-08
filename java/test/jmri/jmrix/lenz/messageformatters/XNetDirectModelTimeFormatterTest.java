package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the XNetDirectModelTimeFormatter class
 * .
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetDirectModelTimeFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessage() {
        XNetReply reply = new XNetReply("64 25 95 36 02 C3");
        assertThat(formatter.handlesMessage(reply)).isTrue();
        assertThat(formatter.formatMessage(reply)).isEqualTo("Fast Clock: Day 4 time 21:54 Rate: 2");
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetDirectModelTimeFormatter();
    }

}
