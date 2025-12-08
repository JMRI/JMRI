package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNet27SpeedStepModeSpeedAndDirectionFormatter class.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNet27SpeedStepModeSpeedAndDirectionFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormattingForwardHalfSpeed() {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234, jmri.SpeedStepMode.NMRA_DCC_27, 0.5f, true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 14 and direction Forward in 27 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardHalfSpeed() {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_27,0.5f,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 14 and direction Reverse in 27 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingForwardStopped() {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_27,0,true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Forward in 27 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardStopped() {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_27,0,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Reverse in 27 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNet27SpeedStepModeSpeedAndDirectionFormatter();
    }

}
