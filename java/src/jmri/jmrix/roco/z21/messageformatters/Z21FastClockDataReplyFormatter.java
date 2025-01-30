package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Z21 Fast Clock Data Reply Formatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21FastClockDataReplyFormatter implements Z21MessageFormatter {


    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x00CD;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        Z21Reply r = (Z21Reply) m;
        int day = ((r.getElement(6) & 0xE0) >>5);
        int hour = ((r.getElement(6) & 0x1F));
        int minute = r.getElement(7) & 0x3F;
        int second = r.getElement(8) & 0x3F;
        boolean stopped = (r.getElement(8) & 0x10000000) != 0;
        boolean paused = (r.getElement(8) & 0x01000000) != 0;
        int rate = r.getElement(9) & 0x3F;
        int settings = r.getElement(10);
        return Bundle.getMessage("Z21ClockMessageString",day,hour,minute,second,rate,
                stopped?Bundle.getMessage("ClockStopped"):"",
                paused?Bundle.getMessage("ClockPaused"):"",
                formatSettings(settings));
    }

    String formatSettings(int settings) {
        StringBuilder sb = new StringBuilder();
        sb.append("settings ");
        if ((settings & 0x01) == 0x01) {
            sb.append(Bundle.getMessage("LocoNetClock"));
        }
        if ((settings & 0x02) == 0x02) {
            sb.append(Bundle.getMessage("XPressNetBroadcast"));
        }
        if ((settings & 0x08) == 0x08) {
            sb.append(Bundle.getMessage("DCCBroadcast"));
        }
        if ((settings & 0x10) == 0x10) {
            sb.append(Bundle.getMessage("MRClockMulticast"));
        }
        if ((settings & 0x40) == 0x40) {
            sb.append(Bundle.getMessage("ClockEStop"));
        }
        if ((settings & 0x80) == 0x80) {
            sb.append(Bundle.getMessage("ClockEnabled"));
        }
        return sb.toString();
    }

}
