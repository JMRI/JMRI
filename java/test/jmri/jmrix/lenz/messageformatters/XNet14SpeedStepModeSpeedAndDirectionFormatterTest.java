package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNet14SpeedStepModeSpeedAndDirectionFormatter class.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNet14SpeedStepModeSpeedAndDirectionFormatterTest {

    @Test
    public void testFormattingForwardHalfSpeed() {
        XNet14SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet14SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234, jmri.SpeedStepMode.NMRA_DCC_14, 0.5f, true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 8 and direction Forward in 14 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardHalfSpeed() {
        XNet14SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet14SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_14,0.5f,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 8 and direction Reverse in 14 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingForwardStopped() {
        XNet14SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet14SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_14,0,true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Forward in 14 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardStopped() {
        XNet14SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet14SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_14,0,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Reverse in 14 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

}
