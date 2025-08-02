package jmri.jmrix.tmcc.serialmon;

import jmri.jmrix.tmcc.SerialListener;
import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;

/**
 * Frame displaying (and logging) TMCC serial command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
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
        } else {
            nextLine("Cmd: " + parse(l.getOpCode(), l.getAsWord()) + "\n", l.toString());
        }
    }

    @Override
    public synchronized void reply(SerialReply l) { // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + ": \"" + l.toString() + "\"\n",
                    l.toString());
        } else {
            nextLine("Rep: " + parse(l.getOpCode(), l.getAsWord()) + "\n", l.toString());
        }
    }

    String parse(int opCode, int val) {
        if (opCode != 0xFE) {
            // TMCC 2 parsing
            return "TMCC 2 msg 0x"+Integer.toHexString(opCode)+" 0x"+Integer.toHexString(val);
        }
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
                    //if (((D & 0x70) == 0x10) && ((D & 0x0F) < 10)) {
                    //    return "engine " + A + " numeric action command " + (D & 0x0F);
                    //}

                    switch (D) {
                        case 0:
                            return "Engine " + A + " - Forward Direction";
                        case 1:
                            return "Engine " + A + " - Toggle Direction";
                        case 3:
                            return "Engine " + A + " - Reverse Direction";
                        case 4:
                            return "Engine " + A + " - Boost";
                        case 5:
                            return "Engine " + A + " - Open Front Coupler";
                        case 6:
                            return "Engine " + A + " - Open Rear Coupler";
                        case 7:
                            return "Engine " + A + " - Brake";
						case 9:
                            return "Engine " + A + " - AUX1 Option 1 (CAB AUX1 button)";
                        case 13:
                            return "Engine " + A + " - AUX2 Option 1 (CAB AUX2 button) Headlight On/Off";
                        case 16:
                            return "Engine " + A + " - Num 0 - Trigger for Options (Needed to toggle ERR 100 Speed Steps)";
						case 17:
                            return "Engine " + A + " - Num 1 - Sound Volume Increase";
						case 18:
                            return "Engine " + A + " - Num 2 - Crew Talk";
						case 19:
                            return "Engine " + A + " - Num 3 - Sound On w/Start-Up Sequence";
						case 20:
                            return "Engine " + A + " - Num 4 - Sound Volume Decrease";
						case 21:
                            return "Engine " + A + " - Num 5 - Sound Off w/Shut-Down Sequence";
						case 22:
                            return "Engine " + A + " - Num 6 - Steam Release/RPM Decrease";
						case 23:
                            return "Engine " + A + " - Num 7 - Tower Com Announcement";
						case 24:
                            return "Engine " + A + " - Num 8 - Feature Off (Smoke/Aux Lighting)";
						case 25:
                            return "Engine " + A + " - Num 9 - Feature On (Smoke/Aux Lighting)";
						case 28:
                            return "Engine " + A + " - Blow Whistle/Horn 1";
                        case 29:
                            return "Engine " + A + " - Ring Bell";
                        case 30:
                            return "Engine " + A + " - Letoff Sound";
                        case 31:
                            return "Engine " + A + " - Blow Horn 2";
                        default:
                            return "Engine " + A + " - action command D=" + D;
                    }

                case 1:
                    //return "Engine " + A + " - extended command (C=1) with D=" + D;
					if ((D & 0x17) == 0) {
                        return "Engine " + A + " - Momentum Low";
                    }
					if ((D & 0x17) == 1) {
                        return "Engine " + A + " - Momentum Medium";
                    }
					if ((D & 0x17) == 2) {
                        return "Engine " + A + " - Momentum High";
                    }
					if ((D & 0x17) == 3) {
                        return "Engine " + A + " - Set";
                    }
					//$FALL-THROUGH$
                case 2:
                    return "Change Engine " + A + " - Speed (Relative) by " + (D - 5);
                case 3:
                default:    // to let the compiler know there are only 3 cases
                    return "Set Engine " + A + " - Speed (Absolute) to " + D;
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
