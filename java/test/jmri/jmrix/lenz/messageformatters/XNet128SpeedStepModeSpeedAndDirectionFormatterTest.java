package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNet128SpeedStepModeSpeedAndDirectionFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNet128SpeedStepModeSpeedAndDirectionFormatterTest {

    @Test
    public void testFormattingForwardHalfSpeed() {
        XNet128SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet128SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234, jmri.SpeedStepMode.NMRA_DCC_128, 0.5f, true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 64 and direction Forward in 128 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardHalfSpeed() {
        XNet128SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet128SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_128,0.5f,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 64 and direction Reverse in 128 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingForwardStopped() {
        XNet128SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet128SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_128,0,true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Forward in 128 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardStopped() {
        XNet128SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet128SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_128,0,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Reverse in 128 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

}
