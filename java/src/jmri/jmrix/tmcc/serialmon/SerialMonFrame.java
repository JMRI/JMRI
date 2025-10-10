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
            int A = (val / 512) & 0x7F; // A is TMCC Adddress Code
            int C = (val / 32) & 0x03; // C is TMCC Command Code
            int D = val & 0x1F; // D is TMCC Data Code
            if ((val & 0x0100) == 0x0100) {
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0                    
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
                                return "TMCC2 - Engine " + A + " - Num 0 - Engine Reset - Needed to toggle ERR 100 Speed Steps - TMCC2 Feature Type 0 ";
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

                    case 1: // If C (TMCC Command Code) == 1
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
                            return "TMCC2 - Engine ID " + A + " - Set";
                        }
                        if ((D & 0x17) == 6) {
                            return "TMCC2 - Engine " + A + " - Unassigned FnKey 111";
                        }
                    
                        //$FALL-THROUGH$
                    case 2: // If C (TMCC Command Code) == 2
                        return "TMCC2 - Engine " + A + " - Change Speed (Relative) by +" + (D - 5);
                        
                    case 3: // If C (TMCC Command Code) == 3
                    default:    // to let the compiler know there are only 3 cases
                        return "TMCC2 (32 Speed Steps) - Engine " + A + " - Speed (Absolute) = " + D;
                }
            }

            return "TMCC2 (200 Speed Steps) - Engine " + A + " - Speed (Absolute) = " + (val & 0xFF);
        }
        
        
        // TMCC 1 parsing
        if (opCode == 0xFE) {
            if ((val & 0xC000) == 0x4000) {
                // TMCC1 Switch Commands
                int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                if ((C == 0) && (D == 0)) {
                    return "Throw switch " + A + " - THROUGH";
                } else if ((C == 0) && (D == 0x1F)) {
                    return "Throw switch " + A + " - OUT";
                } else if ((C == 1) && (D == 0x0B)) {
                    return "Switch ID " + A + " - Set";                
                } else if (C == 2) {
                    return "Assign switch " + A + " to route " + D + " - THROUGH";
                } else if (C == 3) {
                    return "Assign switch " + A + " to route " + D + " - OUT";
                } else {
                    return "unrecognized switch command with A=" + A + " C=" + C + " D=" + D;
                }
            } else if ((val & 0xF000) == 0xD000) {
                // TMCC1 Route Commands
                int A = (val / 128) & 0x1F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                if ((C == 0) && (D == 0x1F)) {
                    return "Route " + A + " - THROW";
                } else if ((C == 1) && (D == 0x0C)) {
                    return "Route " + A + " - CLEAR";
                } else {
                      return "unrecognized route command with A=" + A + " C=" + C + " D=" + D;
                }
            } else if ((val & 0xC000) == 0x0000) {
                // TMCC1 Engine Commands
                int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0                    
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

                    case 1: // If C (TMCC Command Code) == 1
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
                            return "TMCC1 - Engine ID " + A + " - Set";
                        }
                        if ((D & 0x17) == 6) {
                            return "TMCC1 - Engine " + A + " - Unassigned FnKey 222";
                        }
                    
                    //$FALL-THROUGH$
                    case 2: // If C (TMCC Command Code) == 2
                        return "TMCC1 - Engine " + A + " - Change Speed (Relative) by " + (D - 5);

                    case 3: // If C (TMCC Command Code) == 3
                    default:    // to let the compiler know there are only 3 cases
                        return "TMCC1 (32 Speed Steps) - Engine " + A + " - Speed (Absolute) = " + D;
                }

            } else if ((val & 0xF800) == 0xC800) {
                // TMCC1 Train Commands
                int A = (val / 128) & 0x0F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                return "unrecognized train command with A=" + A + " C=" + C + " D=" + D;
            } else if ((val & 0xC000) == 0x8000) {
                // TMCC1 Accessory Commands
                int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                if ((C == 0) && (D == 0x08)) {
                    return "Aux 1 - ACC " + A + " - OFF";
                } else if ((C == 0) && (D == 0x09)) {
                    return "Aux 1 - ACC " + A + " - OPTION 1";
                } else if ((C == 0) && (D == 0x0A)) {
                    return "Aux 1 - ACC " + A + " - OPTION 2";
                } else if ((C == 0) && (D == 0x0B)) {
                    return "Aux 1 - ACC " + A + " - ON";
                } else if ((C == 0) && (D == 0x0C)) {
                    return "Aux 2 - ACC " + A + " - OFF";
                } else if ((C == 0) && (D == 0x0D)) {
                    return "Aux 2 - ACC " + A + " - OPTION 1";
                } else if ((C == 0) && (D == 0x0E)) {
                    return "Aux 2 - ACC " + A + " - OPTION 2";
                } else if ((C == 0) && (D == 0x0F)) {
                    return "Aux 2 - ACC " + A + " - ON";
//                } else if ((C == 0) && (D == 0x??)) {
//                    return "Numeric Command - ACC " + A + " - 0-9";
                } else if ((C == 1) && (D == 0x00)) {
                    return "ALL ACC OFF";
                } else if ((C == 1) && (D == 0x1F)) {
                    return "ALL ACC ON";
                } else if ((C == 1) && (D == 0x0B)) {
                    return "Accessory ID " + A + " - Set";
//                } else if ((C == 1) && (D == 0x??)) {
//                    return "Assign Aux 1 to Group D " + A + " - 0-9";
//                } else if ((C == 1) && (D == 0x??)) {
//                    return "Assign Aux 2 to Group D " + A + " - 0-9"";
                } else {
                    return "unrecognized accessory command with A=" + A + " C=" + C + " D=" + D;
                }
            } else if ((val & 0xF800) == 0xC000) {
                // TMCC1 Group Commands
                int A = (val / 128) & 0x0F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                if ((C == 0) && (D == 0x08)) {
                    return "GROUP - ACC " + A + " - OFF";
                } else if ((C == 0) && (D == 0x09)) {
                    return "GROUP - ACC " + A + " - OPTION 1";
                } else if ((C == 0) && (D == 0x0A)) {
                    return "GROUP - ACC " + A + " - OPTION 2";
                } else if ((C == 0) && (D == 0x0B)) {
                    return "GROUP - ACC " + A + " - ON";
                } else if ((C == 1) && (D == 0x0C)) {
                    return "GROUP - ACC " + A + " - CLEAR";
                } else {
                    return "unrecognized group command with A=" + A + " C=" + C + " D=" + D;
                }
            }            
        }


        // TMCC Error parsing
        if (opCode == 0x00) {
//            int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
            int C = (val / 32) & 0x03; // C is TMCC Command Code
            int D = val & 0x1F; // D is TMCC Data Code
            if ((C == 0) && (D == 0)) {
                return "Address Must be Between 1-98 for TMCC";
            } else if ((C == 0) && (D == 0x01)) {
                return "CV Must Equal 1 for Programming TMCC Loco/Engine, Switch, Accessory ID#s";
            } else if ((C == 0) && (D == 0x02)) {
                return "CV Must Equal 2 for Programming TMCC Feature Type";
            } else if ((C == 0) && (D == 0x03)) {
                return "Value Entered is Not a TMCC1 Feature Type";
            } else if ((C == 0) && (D == 0x04)) {
                return "Value Entered is Not a TMCC2 Feature Type";                
            }
        }
        
        return "TMCC - CV#, Loco ID#/Address/Feature Value - Out of Range";
    }
}