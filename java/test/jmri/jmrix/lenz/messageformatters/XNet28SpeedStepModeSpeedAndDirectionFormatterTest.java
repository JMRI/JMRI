package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNet28SpeedStepModeSpeedAndDirectionFormatter class.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNet28SpeedStepModeSpeedAndDirectionFormatterTest {

    @Test
    public void testFormattingForwardHalfSpeed() {
        XNet28SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet28SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234, jmri.SpeedStepMode.NMRA_DCC_28, 0.5f, true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 14 and direction Forward in 28 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardHalfSpeed() {
        XNet28SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet28SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_28,0.5f,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 14 and direction Reverse in 28 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingForwardStopped() {
        XNet28SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet28SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_28,0,true);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Forward in 28 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

    @Test
    public void testFormattingBackwardStopped() {
        XNet28SpeedStepModeSpeedAndDirectionFormatter formatter = new XNet28SpeedStepModeSpeedAndDirectionFormatter();
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(1234,jmri.SpeedStepMode.NMRA_DCC_28,0,false);
        Assertions.assertThat(formatter.handlesMessage(msg)).isTrue();
        Assertions.assertThat("Mobile Decoder Operations Request: Set Address 1234 to Speed Step 0 and direction Reverse in 28 Speed Step Mode.").isEqualTo(formatter.formatMessage(msg));
    }

}
