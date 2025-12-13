package jmri.jmrix.tmcc.serialmon;

import jmri.jmrix.tmcc.SerialListener;
import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;

/**
 * Frame displaying (and logging) TMCC serial command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
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
        if (opCode == 0xF8) {
            // TMCC2 Engine Commands
            int A = (val / 512) & 0x7F; // A is TMCC Adddress Code
            int C = (val / 32) & 0x03; // C is TMCC Command Code
            int D = val & 0x3F; // D is TMCC Data Code
            if (A < 99) {
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
                                    return "TMCC2 - Engine " + A + " - AUX1 Option 1 (CAB AUX1 Button; On While Pressed/On Until Next Button Pressed)";
                                case 10:
                                    return "TMCC2 - Engine " + A + " - AUX1 Option 2 (Toggle On/Toggle Off)";
                                case 11:
                                    return "TMCC2 - Engine " + A + " - AUX1 On";
                                case 12:
                                    return "TMCC2 - Engine " + A + " - AUX2 Off";
                                case 13:
                                    return "TMCC2 - Engine " + A + " - Headlight Toggle On/Off (AUX2 Option 1/AUX2 Button)";
                                case 14:
                                    return "TMCC2 - Engine " + A + " - AUX2 Option 2 (On While Pressed)";
                                case 15:
                                    return "TMCC2 - Engine " + A + " - AUX2 On";
                                case 16:
                                    return "TMCC2 - Engine " + A + " - Num 0 - Engine Reset - TMCC2 Loco Feature Type 0";
                                case 17:
                                    return "TMCC2 - Engine " + A + " - Num 1 - Sound Volume Increase - TMCC2 Loco Feature Type 1";
                                case 18:
                                    return "TMCC2 - Engine " + A + " - Num 2 - Crew Talk - TMCC2 Loco Feature Type 2";
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
                                    return "TMCC2 - Engine " + A + " - Unassigned FnKey TMCC2 (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                            }

                        case 1: // If C (TMCC Command Code) == 1
                            switch (D & 0x1F) {
                                case 0:
                                    return "TMCC2 - Engine " + A + " - Assign as Single Unit - Forward Direction";
                                case 1:
                                    return "TMCC2 - Engine " + A + " - Assign as Single Unit - Reverse Direction";
                                case 2:
                                    return "TMCC2 - Engine " + A + " - Assign as Head Unit  - Forward Direction";
                                case 3:
                                    return "TMCC2 - Engine " + A + " - Assign as Head Unit - Reverse Direction";
                                case 4:
                                    return "TMCC2 - Engine " + A + " - Assign as Middle Unit - Forward Direction";
                                case 5:
                                    return "TMCC2 - Engine " + A + " - Assign as Middle Unit - Reverse Direction";
                                case 6:
                                    return "TMCC2 - Engine " + A + " - Assign as Rear Unit - Forward Direction";
                                case 7:
                                    return "TMCC2 - Engine " + A + " - Assign as Rear Unit - Reverse Direction";
                                case 8:
                                    return "TMCC2 - Engine " + A + " - Momentum Low";
                                case 9:
                                    return "TMCC2 - Engine " + A + " - Momentum Medium";
                                case 10:
                                    return "TMCC2 - Engine " + A + " - Momentum High";
                                case 11:
                                    return "TMCC2 - Engine ID " + A + " - Set";
                                case 12:
                                    
                                case 13:
                                    
                                case 14:
                                    
                                case 15:
                                    
                                case 16:
                                    return "TMCC2 - Engine " + A + " - Clear Train (Consist) Assignment";
                                case 17:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 1";
                                case 18:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 2";
                                case 19:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 3";
                                case 20:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 4";
                                case 21:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 5";
                                case 22:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 6";
                                case 23:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 7";
                                case 24:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 8";
                                case 25:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 9";
                                case 26:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 10";
                                case 27:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 11";
                                case 28:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 12";
                                case 29:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 13";
                                case 30:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 14";
                                case 31:
                                    return "TMCC2 - Engine " + A + " - Assign to Train (Consist) 15";
                                default:
                                    return "TMCC2 - Engine " + A + " - Unassigned FnKey TMCC2 (Case C=1) - with A= " + A + " C= " + C + " D= " + D;
                            }
                    
                        //$FALL-THROUGH$
                        case 2: // If C (TMCC Command Code) == 2
                            // return "TMCC2 - Engine " + A + " - Change Speed (Relative) by " + (D - 5);
                        
                        case 3: // If C (TMCC Command Code) == 3
                        default:    // to let the compiler know there are only 3 cases
                            return "TMCC2 (32 Speed Steps) - Engine " + A + " - Speed (Absolute) = " + (D-32);
                    }
                }
            }

            if (val == 0xFF8B) {
                return "TMCC2 - HALT - Emergency System Stop; ALL";
            } else {
                return "TMCC2 (200 Speed Steps) - Engine " + A + " - Speed (Absolute) = " + (val & 0xFF);
            }
        }

        if (opCode == 0xF9) {
            // TMCC2 Train Commands
            int A = (val / 512) & 0x7F; // A is TMCC Adddress Code
            int C = (val / 32) & 0x03; // C is TMCC Command Code
            int D = val & 0x1F; // D is TMCC Data Code
            if (A < 99) {
                if ((val & 0x0100) == 0x0100) {
                    switch (C) {
                        case 0: // If C (TMCC Command Code) == 0                    
                            switch (D) {
                                case 0:
                                    return "TMCC2 - Train " + A + " - Forward Direction";
                                case 1:
                                    return "TMCC2 - Train " + A + " - Toggle Direction";
                                case 2:
                                    
                                case 3:
                                    return "TMCC2 - Train " + A + " - Reverse Direction";
                                case 4:
                                    return "TMCC2 - Train " + A + " - Boost";
                                case 5:
                                    return "TMCC2 - Train " + A + " - Open Front Coupler";
                                case 6:
                                    return "TMCC2 - Train " + A + " - Open Rear Coupler";
                                case 7:
                                    return "TMCC2 - Train " + A + " - Brake";
                                case 8:
                                    return "TMCC2 - Train " + A + " - AUX1 Off";
                                case 9:
                                    return "TMCC2 - Train " + A + " - AUX1 Option 1 (CAB AUX1 Button; On While Pressed/On Until Next Button Pressed)";
                                case 10:
                                    return "TMCC2 - Train " + A + " - AUX1 Option 2 (Toggle On/Toggle Off)";
                                case 11:
                                    return "TMCC2 - Train " + A + " - AUX1 On";
                                case 12:
                                    return "TMCC2 - Train " + A + " - AUX2 Off";
                                case 13:
                                    return "TMCC2 - Train " + A + " - Headlight Toggle On/Off (AUX2 Option 1/AUX2 Button)";
                                case 14:
                                    return "TMCC2 - Train " + A + " - AUX2 Option 2 (On While Pressed)";
                                case 15:
                                    return "TMCC2 - Train " + A + " - AUX2 On";
                                case 16:
                                    return "TMCC2 - Train " + A + " - Num 0 - Train Reset - TMCC2 Loco Feature Type 0";
                                case 17:
                                    return "TMCC2 - Train " + A + " - Num 1 - Sound Volume Increase - TMCC2 Loco Feature Type 1";
                                case 18:
                                    return "TMCC2 - Train " + A + " - Num 2 - Crew Talk - TMCC2 Loco Feature Type 2";
                                case 19:
                                    return "TMCC2 - Train " + A + " - Num 3 - Sound On w/Start-Up Sequence/RPM Increase";
                                case 20:
                                    return "TMCC2 - Train " + A + " - Num 4 - Sound Volume Decrease";
                                case 21:
                                    return "TMCC2 - Train " + A + " - Num 5 - Sound Off w/Shut-Down Sequence";
                                case 22:
                                    return "TMCC2 - Train " + A + " - Num 6 - Steam Release/RPM Decrease";
                                case 23:
                                    return "TMCC2 - Train " + A + " - Num 7 - Tower Com Announcement";
                                case 24:
                                    return "TMCC2 - Train " + A + " - Num 8 - Feature Off (Smoke/Aux Lighting)";
                                case 25:
                                    return "TMCC2 - Train " + A + " - Num 9 - Feature On (Smoke/Aux Lighting)";
                                case 26:
                                    
                                case 27:
                                    
                                case 28:
                                    return "TMCC2 - Train " + A + " - Blow Whistle/Horn 1";
                                case 29:
                                    return "TMCC2 - Train " + A + " - Ring Bell";
                                case 30:
                                    return "TMCC2 - Train " + A + " - Letoff Sound";
                                case 31:
                                    return "TMCC2 - Train " + A + " - Blow Horn 2";
                                default:
                                    return "TMCC2 - Train " + A + " - Unassigned FnKey TMCC2 (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                            }

                        case 1: // If C (TMCC Command Code) == 1
                            switch (D & 0x17) {
                                case 0:
                                    return "TMCC2 - Train " + A + " - Momentum Low";
                                case 1:
                                    return "TMCC2 - Train " + A + " - Momentum Medium";
                                case 2:
                                    return "TMCC2 - Train " + A + " - Momentum High";
                                case 3:
                                    return "TMCC2 - Train ID " + A + " - Set";
                                default:
                                    return "TMCC2 - Train " + A + " - Unassigned FnKey TMCC2 (Case C=1) - with A= " + A + " C= " + C + " D= " + D;
                            }
                    
                        //$FALL-THROUGH$
                        case 2: // If C (TMCC Command Code) == 2
                            // return "TMCC2 - Train " + A + " - Change Speed (Relative) by " + (D - 5);
                        
                        case 3: // If C (TMCC Command Code) == 3
                        default:    // to let the compiler know there are only 3 cases
                            return "TMCC2 (32 Speed Steps) - Train " + A + " - Speed (Absolute) = " + D;
                    }
                }
            }

            if (val == 0xFF8B) {
                return "TMCC2 - HALT - Emergency System Stop; ALL";
            } else {
                return "TMCC2 (200 Speed Steps) - Train " + A + " - Speed (Absolute) = " + (val & 0xFF);
            }

        }        
        
        // TMCC 1 parsing
        if (opCode == 0xFE) {
            if (val == 0xFFFF) {
                return "TMCC1 - HALT - Emergency System Stop; ALL";

            } else if ((val & 0xC000) == 0x4000) {
                // TMCC1 Switch Commands
                int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0
                        switch (D) {
                            case 0:
                                return "Throw Switch " + A + " - THROUGH/CLOSED";
                            case 31:
                                return "Throw Switch " + A + " - OUT/THROWN";
                            default:
                                return "Unrecognized Switch(SW) Command (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                        }

                    case 1: // If C (TMCC Command Code) == 1
                        switch (D) {
                            case 11:
                                return "Switch ID " + A + " - Set";
                            default:
                                return "Unrecognized Switch(SW) Command (Cases C=1) - with A= " + A + " C= " + C + " D= " + D;                                
                        }

                    //$FALL-THROUGH$
                    case 2: // If C (TMCC Command Code) == 2
                        return "Assign switch " + A + " to route " + D + " - THROUGH";

                    case 3: // If C (TMCC Command Code) == 3
                        return "Assign switch " + A + " to route " + D + " - OUT";
                    default:
                        return "Unrecognized Switch(SW) Command (Cases C= 2-3) - with A= " + A + " C= " + C + " D= " + D;
                }


            } else if ((val & 0xF000) == 0xD000) {
                // TMCC1 Route Commands
                int A = (val / 128) & 0x1F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0
                        switch (D) {
                            case 15:
                                return "Route " + A + " - THROW";
                            default:
                                return "Unrecognized Route(RTE) Command (Cases C=0) - with A= " + A + " C= " + C + " D= " + D;
                        }

                    case 1: // If C (TMCC Command Code) == 0
                        switch (D) {
                            case 12:
                                return "Route " + A + " - CLEAR";
                            default:
                                return "Unrecognized Route(RTE) Command (Cases C=1) - with A= " + A + " C= " + C + " D= " + D;

                        }

                default:
                    return "Unrecognized Route(RTE) Command (Cases C) - with A= " + A + " C= " + C + " D= " + D;
                }

            } else if ((val & 0xC000) == 0x0000) {
                // TMCC1 Engine Commands
                int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x3F; // D is TMCC Data Code
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
                                return "TMCC1 - Engine " + A + " - AUX1 Option 1 (CAB AUX1 Button; On While Pressed/On Until Next Button Pressed)";
                            case 10:
                            
                            case 11:
                            
                            case 12:
                            
                            case 13:
                                return "TMCC1 - Engine " + A + " - Headlight Toggle On/Off (AUX2 Option 1/AUX2 Button)";
                            case 14:
                            
                            case 15:
                            
                            case 16:
                                return "TMCC1 - Engine " + A + " - Num 0 - Engine Reset";
                            case 17:
                                return "TMCC1 - Engine " + A + " - Num 1 - Sound Volume Increase";
                            case 18:
                                return "TMCC1 - Engine " + A + " - Num 2 - Crew Talk";
                            case 19:
                                return "TMCC1 - Engine " + A + " - Num 3 - Sound On w/Start-Up Sequence";
                            case 20:
                                return "TMCC1 - Engine " + A + " - Num 4 - Sound Volume Decrease - TMCC1 Loco Feature Type 4";
                            case 21:
                                return "TMCC1 - Engine " + A + " - Num 5 - Sound Off w/Shut-Down Sequence - TMCC1 Loco Feature Type 5";
                            case 22:
                                return "TMCC1 - Engine " + A + " - Num 6 - Steam Release/RPM Decrease - TMCC1 Loco Feature Type 6";
                            case 23:
                                return "TMCC1 - Engine " + A + " - Num 7 - Tower Com Announcement";
                            case 24:
                                return "TMCC1 - Engine " + A + " - Num 8 - Feature Off (Smoke/Aux Lighting) - TMCC1 Loco eature Type 8";
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
                                return "TMCC1 - Engine " + A + " - Unassigned FnKey TMCC1 (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                        }

                    case 1: // If C (TMCC Command Code) == 1
                        switch (D & 0x1F) {
                            case 0:
                                return "TMCC1 - Engine " + A + " - Assign as Single Unit - Forward Direction";
                            case 1:
                                return "TMCC1 - Engine " + A + " - Assign as Head Unit  - Forward Direction";
                            case 2:
                                return "TMCC1 - Engine " + A + " - Assign as Middle Unit  - Forward Direction";
                            case 3:
                                return "TMCC1 - Engine " + A + " - Assign as Rear Unit  - Forward Direction";
                            case 4:
                                return "TMCC1 - Engine " + A + " - Assign as Single Unit - Reverse Direction";
                            case 5:
                                return "TMCC1 - Engine " + A + " - Assign as Head Unit - Reverse Direction";
                            case 6:
                                return "TMCC1 - Engine " + A + " - Assign as Middle Unit - Reverse Direction";
                            case 7:
                                return "TMCC1 - Engine " + A + " - Assign as Rear Unit - Reverse Direction";
                            case 8:
                                return "TMCC1 - Engine " + A + " - Momentum Low";
                            case 9:
                                return "TMCC1 - Engine " + A + " - Momentum Medium";
                            case 10:
                                return "TMCC1 - Engine " + A + " - Momentum High";
                            case 11:
                                return "TMCC1 - Engine ID " + A + " - Set";
                            case 12:
                                
                            case 13:
                                
                            case 14:
                                
                            case 15:
                                
                            case 16:
                                return "TMCC1 - Engine " + A + " - Clear Train (Consist) Assignment";
                            case 17:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 1";
                            case 18:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 2";
                            case 19:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 3";
                            case 20:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 4";
                            case 21:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 5";
                            case 22:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 6";
                            case 23:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 7";
                            case 24:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 8";
                            case 25:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 9";
                            case 26:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 10";
                            case 27:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 11";
                            case 28:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 12";
                            case 29:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 13";
                            case 30:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 14";
                            case 31:
                                return "TMCC1 - Engine " + A + " - Assign to Train (Consist) 15";
                            default:
                                return "TMCC1 - Engine " + A + " - Unassigned FnKey TMCC1 (Case C=1) - with A= " + A + " C= " + C + " D= " + D;
                        }
                    
                    //$FALL-THROUGH$
                    case 2: // If C (TMCC Command Code) == 2
                        return "TMCC1 - Engine " + A + " - Change Speed (Relative) by " + (D - 5);

                    case 3: // If C (TMCC Command Code) == 3
                    default: // to let the compiler know there are only 3 cases
                        return "TMCC1 (32 Speed Steps) - Engine " + A + " - Speed (Absolute) = " + (D-32);
                }


            } else if ((val & 0xC000) == 0xC000) {
                // TMCC1 Train Commands
                int A = (val / 128) & 0x0F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x3F; // D is TMCC Data Code
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0
                        switch (D) {
                            case 0:
                                return "TMCC1 - Train " + A + " - Forward Direction";
                            case 1:
                                return "TMCC1 - Train " + A + " - Toggle Direction";
                            case 2:
                            
                            case 3:
                                return "TMCC1 - Train " + A + " - Reverse Direction";
                            case 4:
                                return "TMCC1 - Train " + A + " - Boost";
                            case 5:
                                return "TMCC1 - Train " + A + " - Open Front Coupler";
                            case 6:
                                return "TMCC1 - Train " + A + " - Open Rear Coupler";
                            case 7:
                                return "TMCC1 - Train " + A + " - Brake";
                            case 8:
                            
                            case 9:
                                return "TMCC1 - Train " + A + " - AUX1 Option 1 (CAB AUX1 Button; On While Pressed/On Until Next Button Pressed)";
                            case 10:
                            
                            case 11:
                            
                            case 12:
                            
                            case 13:
                                return "TMCC1 - Train " + A + " - Headlight Toggle On/Off (AUX2 Option 1/AUX2 Button)";
                            case 14:
                            
                            case 15:
                            
                            case 16:
                                return "TMCC1 - Train " + A + " - Num 0 - Train Reset";
                            case 17:
                                return "TMCC1 - Train " + A + " - Num 1 - Sound Volume Increase";
                            case 18:
                                return "TMCC1 - Train " + A + " - Num 2 - Crew Talk";
                            case 19:
                                return "TMCC1 - Train " + A + " - Num 3 - Sound On w/Start-Up Sequence";
                            case 20:
                                return "TMCC1 - Train " + A + " - Num 4 - Sound Volume Decrease - TMCC1 Loco Feature Type 4";
                            case 21:
                                return "TMCC1 - Train " + A + " - Num 5 - Sound Off w/Shut-Down Sequence - TMCC1 Loco Feature Type 5";
                            case 22:
                                return "TMCC1 - Train " + A + " - Num 6 - Steam Release/RPM Decrease - TMCC1 Loco Feature Type 6";
                            case 23:
                                return "TMCC1 - Train " + A + " - Num 7 - Tower Com Announcement";
                            case 24:
                                return "TMCC1 - Train " + A + " - Num 8 - Feature Off (Smoke/Aux Lighting) - TMCC1 Loco Feature Type 8";
                            case 25:
                                return "TMCC1 - Train " + A + " - Num 9 - Feature On (Smoke/Aux Lighting)";
                            case 26:
                            
                            case 27:
                            
                            case 28:
                                return "TMCC1 - Train " + A + " - Blow Whistle/Horn 1";
                            case 29:
                                return "TMCC1 - Train " + A + " - Ring Bell";
                            case 30:
                                return "TMCC1 - Train " + A + " - Letoff Sound";
                            case 31:
                                return "TMCC1 - Train " + A + " - Blow Horn 2";
                            default:
                                return "TMCC1 - Train " + A + " - Unassigned FnKey TMCC1 (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                        }

                    case 1: // If C (TMCC Command Code) == 1
                        switch (D & 0x17) {
                            case 0:
                                return "TMCC1 - Train " + A + " - Momentum Low";
                            case 1:
                                return "TMCC1 - Train " + A + " - Momentum Medium";
                            case 2:
                                return "TMCC1 - Train " + A + " - Momentum High";
                            case 3:
                                return "TMCC1 - Track/Train ID " + A + " - Set";
                            default:
                                return "Unrecognized Train(TR) Command with A= " + A + " C= " + C + " D= " + D;
                        }

                    //$FALL-THROUGH$
                    case 2: // If C (TMCC Command Code) == 2
                        return "TMCC1 - Train " + A + " - Change Speed (Relative) by " + (D - 5);

                    case 3: // If C (TMCC Command Code) == 3
                    default: // to let the compiler know there are only 3 cases
                        return "TMCC1 (32 Speed Steps) - Train " + A + " - Speed (Absolute) = " + (D-32);
                }

            } else if ((val & 0xC000) == 0x8000) {
                // TMCC1 Accessory Commands
                int A = (val / 128) & 0x7F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0
                        switch (D) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                                return "Aux 1 - ACC " + A + " - OFF";
                            case 9:
                                return "Aux 1 - ACC " + A + " - OPTION 1 (On While Pressed)";
                            case 10:
                                return "Aux 1 - ACC " + A + " - OPTION 2 (Toggle On/Toggle Off)";
                            case 11:
                                return "Aux 1 - ACC " + A + " - ON";
                            case 12:
                                return "Aux 2 - ACC " + A + " - OFF";
                            case 13:
                                return "Aux 2 - ACC " + A + " - OPTION 1 (Toggle On/Toggle Off)";
                            case 14:
                                return "Aux 2 - ACC " + A + " - OPTION 2 (On While Pressed)";
                            case 15:
                                return "Aux 2 - ACC " + A + " - ON";
                            default:
                                return "Unrecognized Accessory(ACC) Command (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                        }
                        
                    case 1: // If C (TMCC Command Code) == 1
                        switch (D) {
                            case 0:
                                return "ALL ACC OFF";
                            case 11:
                                return "Accessory ID " + A + " - Set";
                            case 15:
                                return "ALL ACC ON";
//                } else if ((C == 1) && (D == 0x??)) {
//                    return "Assign Aux 1 to Group D " + A + " - 0-9";
//                } else if ((C == 1) && (D == 0x??)) {
//                    return "Assign Aux 2 to Group D " + A + " - 0-9"";
                            default:
                                return "Unrecognized Accessory(ACC) Command (Case C=1) - with A= " + A + " C= " + C + " D= " + D;
                        }

               default:
                    return "Unrecognized Accessory(ACC) Command (Case C) - with A= " + A + " C= " + C + " D= " + D;
                }

            } else if ((val & 0xF800) == 0xC000) {
                // TMCC1 Group Commands
                int A = (val / 128) & 0x0F; // A is TMCC Adddress Code
                int C = (val / 32) & 0x03; // C is TMCC Command Code
                int D = val & 0x1F; // D is TMCC Data Code
                switch (C) {
                    case 0: // If C (TMCC Command Code) == 0
                        switch (D) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                                return "GROUP - ACC " + A + " - OFF";
                            case 9:
                                return "GROUP - ACC " + A + " - OPTION 1 (On While Pressed)";
                            case 10:
                                return "GROUP - ACC " + A + " - OPTION 2 (Toggle On/Toggle Off)";
                            case 11:
                                return "GROUP - ACC " + A + " - ON";
                            default:
                                return "Unrecognized Group(GR) Command (Case C=0) - with A= " + A + " C= " + C + " D= " + D;
                        }

                    case 1: // If C (TMCC Command Code) == 1
                        switch (D) {
                            case 12:
                                return "GROUP - ACC " + A + " - CLEAR";
                            default:
                                return "Unrecognized Group(GR) Command (Case C=1) - with A= " + A + " C= " + C + " D= " + D;                              
                        }

                default:
                    return "Unrecognized Group(GR) Command (Case C) - with A= " + A + " C= " + C + " D= " + D;
                }
            }            
        }

        // TMCC Error parsing
        if (opCode == 0x00) {
            int C = (val / 32) & 0x03; // C is TMCC Command Code
            int D = val & 0x1F; // D is TMCC Data Code
            switch (C) {
                case 0: // If C (TMCC Command Code) == 0
                    switch (D) {
                        case 0:
                            return "Address Must be Between 1-98 for TMCC1 and TMCC2 ENG/SW/ACC and TMCC2 TR. Address Must be Between 1-9 for TMCC1 TR";
                        case 1:
                            return "CV Must Equal 1 for Programming TMCC Loco/Engine, Switch, Accessory, Track ID#s";
                        case 2:
                            return "CV Must Equal 2 for Programming TMCC Loco Feature Type";
                        case 3:
                            return "Value Entered is Not a TMCC1 Loco Feature Type";
                        case 4:
                            return "Value Entered is Not a TMCC2 Loco Feature Type";
                        default:
                            return "Unrecognized TMCC Error (Case C=0) - with C= " + C + " D= " + D;
                    }
            default:
                return "Unrecognized TMCC Error (Case C) - with C= " + C + " D= " + D;
            }
        }

        return "TMCC - Direct Command Sent";
//        return "TMCC - CV#, Loco ID#/Address/Loco Feature Value - Out of Range";

    }
}

