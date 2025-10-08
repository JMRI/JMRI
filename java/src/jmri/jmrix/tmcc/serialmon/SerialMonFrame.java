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
        // TMCC 2 parsing
        if (opCode == 0xF8 || opCode == 0xF9 || opCode == 0xFB) {
            // TMCC2 Engine Commands
            int A = (val / 512) & 0x7F;
            int C = (val / 32) & 0x03;
            int D = val & 0x1F;
            if ((val & 0x0100) == 0x0100) {
                switch (C) {
                    case 0:
                    
                        switch (D) {
                            case 0:
                                return "TMCC2 - Engine " + A + " - Forward Direction";
                            case 1:
                                return "TMCC2 - Engine " + A + " - Toggle Direction";
                            case 2:
                                
                            case 3:
                                return "TMCC2 - Engine " + A + " - Reverse Direction";
                            case 4:
                                return "TMCC2 - Engine " + A + " - Boost";
                            case 5:
                                return "TMCC2 - Engine " + A + " - Open Front Coupler";
                            case 6:
                                return "TMCC2 - Engine " + A + " - Open Rear Coupler";
                            case 7:
                                return "TMCC2 - Engine " + A + " - Brake";
                            case 8:
                                return "TMCC2 - Engine " + A + " - AUX1 Off";
                            case 9:
                                return "TMCC2 - Engine " + A + " - AUX1 Option 1 (CAB AUX1 button)";
                            case 10:
                                return "TMCC2 - Engine " + A + " - AUX1 Option 2";
                            case 11:
                                return "TMCC2 - Engine " + A + " - AUX1 On";
                            case 12:
                                return "TMCC2 - Engine " + A + " - AUX2 Off";
                            case 13:
                                return "TMCC2 - Engine " + A + " - AUX2 Option 1 (CAB AUX2 button) Headlight On/Off";
                            case 14:
                                return "TMCC2 - Engine " + A + " - AUX2 Option 2";
                            case 15:
                                return "TMCC2 - Engine " + A + " - AUX2 On";
                            case 16:
                                return "TMCC2 - Engine " + A + " - Num 0 - Engine Reset - TMCC2 Feature Type 0 - Needed to toggle ERR 100 Speed Steps";
                            case 17:
                                return "TMCC2 - Engine " + A + " - Num 1 - Sound Volume Increase - TMCC2 Feature Type 1";
                            case 18:
                                return "TMCC2 - Engine " + A + " - Num 2 - Crew Talk - TMCC2 Feature Type 2";
                            case 19:
                                return "TMCC2 - Engine " + A + " - Num 3 - Sound On w/Start-Up Sequence/RPM Increase";
                            case 20:
                                return "TMCC2 - Engine " + A + " - Num 4 - Sound Volume Decrease";
                            case 21:
                                return "TMCC2 - Engine " + A + " - Num 5 - Sound Off w/Shut-Down Sequence";
                            case 22:
                                return "TMCC2 - Engine " + A + " - Num 6 - Steam Release/RPM Decrease";
                            case 23:
                                return "TMCC2 - Engine " + A + " - Num 7 - Tower Com Announcement";
                            case 24:
                                return "TMCC2 - Engine " + A + " - Num 8 - Feature Off (Smoke/Aux Lighting)";
                            case 25:
                                return "TMCC2 - Engine " + A + " - Num 9 - Feature On (Smoke/Aux Lighting)";
                            case 26:
                                
                            case 27:
                                
                            case 28:
                                return "TMCC2 - Engine " + A + " - Blow Whistle/Horn 1";
                            case 29:
                                return "TMCC2 - Engine " + A + " - Ring Bell";
                            case 30:
                                return "TMCC2 - Engine " + A + " - Letoff Sound";
                            case 31:
                                return "TMCC2 - Engine " + A + " - Blow Horn 2";
                            default:
                                return "TMCC2 - Engine " + A + " - action command D=" + D;
                        }

                    case 1:
                        //return "TMCC2 - Engine " + A + " - extended command (C=1) with D=" + D;
                        if ((D & 0x17) == 0) {
                            return "TMCC2 - Engine " + A + " - Momentum Low";
                        }
                        if ((D & 0x17) == 1) {
                            return "TMCC2 - Engine " + A + " - Momentum Medium";
                        }
                        if ((D & 0x17) == 2) {
                            return "TMCC2 - Engine " + A + " - Momentum High";
                        }
                        if ((D & 0x17) == 3) {
                            return "TMCC2 - Engine " + A + " - Set";
                        }
                        if ((D & 0x17) == 6) {
                            return "TMCC2 - Engine " + A + " - Unassigned FnKey";
                        }
                    
                        //$FALL-THROUGH$
                    case 2:
                        return "TMCC2 - Engine " + A + " - Change Speed (Relative) by " + (D - 5);
                    case 3:
                    default:    // to let the compiler know there are only 3 cases
                        return "TMCC2 - Engine " + A + " - Unassigned FnKey";
                }

            }
        }
        
        
        // TMCC 1 parsing
        if (opCode == 0xFE) {
            if ((val & 0xC000) == 0x4000) {
                // TMCC1 Switch Commands
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
                // TMCC1 Route Commands
                int A = (val / 128) & 0x1F;
                int C = (val / 32) & 0x03;
                int D = val & 0x1F;
                return "route command with A=" + A + " C=" + C + " D=" + D;
            } else if ((val & 0xC000) == 0x0000) {
                // TMCC1 Engine Commands
                int A = (val / 128) & 0x7F;
                int C = (val / 32) & 0x03;
                int D = val & 0x1F;
                switch (C) {
                    case 0:
                    
                        switch (D) {
                            case 0:
                                return "TMCC1 - Engine " + A + " - Forward Direction";
                            case 1:
                                return "TMCC1 - Engine " + A + " - Toggle Direction";
                            case 2:
                            
                            case 3:
                                return "TMCC1 - Engine " + A + " - Reverse Direction";
                            case 4:
                                return "TMCC1 - Engine " + A + " - Boost";
                            case 5:
                                return "TMCC1 - Engine " + A + " - Open Front Coupler";
                            case 6:
                                return "TMCC1 - Engine " + A + " - Open Rear Coupler";
                            case 7:
                                return "TMCC1 - Engine " + A + " - Brake";
                            case 8:
                            
                            case 9:
                                return "TMCC1 - Engine " + A + " - AUX1 Option 1 (CAB AUX1 button)";
                            case 10:
                            
                            case 11:
                            
                            case 12:
                            
                            case 13:
                                return "TMCC1 - Engine " + A + " - AUX2 Option 1 (CAB AUX2 button) Headlight On/Off";
                            case 14:
                            
                            case 15:
                            
                            case 16:
                                return "TMCC1 - Engine " + A + " - Num 0 - Engine Reset (Needed to toggle ERR 100 Speed Steps)";
                            case 17:
                                return "TMCC1 - Engine " + A + " - Num 1 - Sound Volume Increase";
                            case 18:
                                return "TMCC1 - Engine " + A + " - Num 2 - Crew Talk";
                            case 19:
                                return "TMCC1 - Engine " + A + " - Num 3 - Sound On w/Start-Up Sequence";
                            case 20:
                                return "TMCC1 - Engine " + A + " - Num 4 - Sound Volume Decrease - TMCC1 Feature Type 4";
                            case 21:
                                return "TMCC1 - Engine " + A + " - Num 5 - Sound Off w/Shut-Down Sequence - TMCC1 Feature Type 5";
                            case 22:
                                return "TMCC1 - Engine " + A + " - Num 6 - Steam Release/RPM Decrease - TMCC1 Feature Type 6";
                            case 23:
                                return "TMCC1 - Engine " + A + " - Num 7 - Tower Com Announcement";
                            case 24:
                                return "TMCC1 - Engine " + A + " - Num 8 - Feature Off (Smoke/Aux Lighting) - TMCC1 Feature Type 8";
                            case 25:
                                return "TMCC1 - Engine " + A + " - Num 9 - Feature On (Smoke/Aux Lighting)";
                            case 26:
                            
                            case 27:
                            
                            case 28:
                                return "TMCC1 - Engine " + A + " - Blow Whistle/Horn 1";
                            case 29:
                                return "TMCC1 - Engine " + A + " - Ring Bell";
                            case 30:
                                return "TMCC1 - Engine " + A + " - Letoff Sound";
                            case 31:
                                return "TMCC1 - Engine " + A + " - Blow Horn 2";
                            default:
                                return "TMCC1 - Engine " + A + " - action command D=" + D;
                        }

                    case 1:
                        //return "TMCC1 - Engine " + A + " - extended command (C=1) with D=" + D;
                        if ((D & 0x17) == 0) {
                            return "TMCC1 - Engine " + A + " - Momentum Low";
                        }
                        if ((D & 0x17) == 1) {
                            return "TMCC1 - Engine " + A + " - Momentum Medium";
                        }
                        if ((D & 0x17) == 2) {
                            return "TMCC1 - Engine " + A + " - Momentum High";
                        }
                        if ((D & 0x17) == 3) {
                            return "TMCC1 - Engine " + A + " - Set";
                        }
                        if ((D & 0x17) == 6) {
                            return "TMCC1 - Engine " + A + " - Unassigned FnKey";
                        }
                    
                    //$FALL-THROUGH$
                    case 2:
                        return "TMCC1 - Engine " + A + " - Change Speed (Relative) by " + (D - 5);
                    case 3:
                    default:    // to let the compiler know there are only 3 cases
                        return "TMCC1 - Engine " + A + " - Unassigned FnKey";
                }

            } else if ((val & 0xF800) == 0xC800) {
                // TMCC1 Train Commands
                int A = (val / 128) & 0x0F;
                int C = (val / 32) & 0x03;
                int D = val & 0x1F;
                return "train command with A=" + A + " C=" + C + " D=" + D;
            } else if ((val & 0xC000) == 0x8000) {
                // TMCC1 Accessory Commands
                int A = (val / 128) & 0x7F;
                int C = (val / 32) & 0x03;
                int D = val & 0x1F;
                return "accessory command with A=" + A + " C=" + C + " D=" + D;
            } else if ((val & 0xF800) == 0xC000) {
                // TMCC1 Group Commands
                int A = (val / 128) & 0x0F;
                int C = (val / 32) & 0x03;
                int D = val & 0x1F;
                return "group command with A=" + A + " C=" + C + " D=" + D;

            }
        }
        return "TMCC - CV#, Loco ID#/Address/Feature Value - Out of Range";
    }
}
