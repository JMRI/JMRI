package jmri.jmrix.tmcc.serialmon;

import jmri.jmrix.tmcc.SerialListener;
import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;

/**
 * Frame displaying (and logging) TMCC serial command messages.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private TmccSystemConnectionMemo _memo = null;

    public SerialMonFrame(TmccSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return Bundle.getMessage("MonitorXTitle", "TMCC");
    }

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addSerialListener(this);
    }

    @Override
    public void dispose() {
        _memo.getTrafficController().removeSerialListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(SerialMessage l) { // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 3) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n",
                    l.toString());
            return;
        } else {
            nextLine("Cmd: " + parse(l.getAsWord()) + "\n", l.toString());
        }
    }

    @Override
    public synchronized void reply(SerialReply l) { // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + ": \"" + l.toString() + "\"\n",
                    l.toString());
            return;
        } else {
            nextLine("Rep: " + parse(l.getAsWord()) + "\n", l.toString());
        }
    }

    String parse(int val) {
        if ((val & 0xC000) == 0x4000) {
            // switch command
            int A = (val / 128) & 0x7F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            if ((C == 0) && (D == 0)) {
                return "Throw switch " + A + " THROUGH";
            } else if ((C == 0) && (D == 0x1F)) {
                return "Throw switch " + A + " OUT";
            } else if ((C == 1) && (D == 0x09)) {
                return "Switch " + A + " set address";
            } else if (C == 2) {
                return "Assign switch " + A + " to route " + D + " THROUGH";
            } else if (C == 3) {
                return "Assign switch " + A + " to route " + D + " OUT";
            } else {
                return "unrecognized switch command with A=" + A + " C=" + C + " D=" + D;
            }
        } else if ((val & 0xF000) == 0xD000) {
            // route command
            int A = (val / 128) & 0x1F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            return "route command with A=" + A + " C=" + C + " D=" + D;
        } else if ((val & 0xC000) == 0x0000) {
            // engine command
            int A = (val / 128) & 0x7F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            switch (C) {
                case 0:
                    if (((D & 0x70) == 0x10) && ((D & 0x0F) < 10)) {
                        return "engine " + A + " numeric action command " + (D & 0x0F);
                    }

                    switch (D) {
                        case 0:
                            return "engine " + A + " forward direction";
                        case 1:
                            return "engine " + A + " toggle direction";
                        case 3:
                            return "engine " + A + " reverse direction";
                        case 7:
                            return "engine " + A + " brake";
                        case 4:
                            return "engine " + A + " boost";
                        case 5:
                            return "engine " + A + " open front coupler";
                        case 6:
                            return "engine " + A + " open rear coupler";
                        case 28:
                            return "engine " + A + " blow horn 1";
                        case 29:
                            return "engine " + A + " ring bell";
                        case 30:
                            return "engine " + A + " letoff sound";
                        case 31:
                            return "engine " + A + " blow horn 2";
                        case 8:
                            return "engine " + A + " AUX1 off";
                        case 9:
                            return "engine " + A + " AUX1 option 1 (CAB AUX1 button)";
                        case 10:
                            return "engine " + A + " AUX1 option 2";
                        case 11:
                            return "engine " + A + " AUX1 on";
                        case 12:
                            return "engine " + A + " AUX2 off";
                        case 13:
                            return "engine " + A + " AUX2 option 1 (CAB AUX2 button)";
                        case 14:
                            return "engine " + A + " AUX2 option 2";
                        case 15:
                            return "engine " + A + " AUX2 on";
                        default:
                            return "engine " + A + " action command D=" + D;
                    }

                case 1:
                    return "engine " + A + " extended command (C=1) with D=" + D;
                case 2:
                    return "change engine " + A + " speed (relative) by " + (D - 5);
                case 3:
                default:    // to let the compiler know there are only 3 cases
                    return "set engine " + A + " speed (absolute) to " + D;
            }
        } else if ((val & 0xF800) == 0xC800) {
            // train command
            int A = (val / 128) & 0x0F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            return "train command with A=" + A + " C=" + C + " D=" + D;
        } else if ((val & 0xC000) == 0x8000) {
            // accessory command
            int A = (val / 128) & 0x7F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            return "accessory command with A=" + A + " C=" + C + " D=" + D;
        } else if ((val & 0xF800) == 0xC000) {
            // group command
            int A = (val / 128) & 0x0F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            return "group command with A=" + A + " C=" + C + " D=" + D;
        } else {
            return "unexpected command " + Integer.toHexString(val & 0xFF);
        }
    }

}
