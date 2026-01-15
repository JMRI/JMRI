package jmri.jmrix.tmcc;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle.
 * <p>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
 */
public class SerialThrottle extends AbstractThrottle {

    /**
     * Constructor.
     *
     * @param memo the connected SerialTrafficController
     * @param address Loco ID
     */
    public SerialThrottle(TmccSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo, 85); // supports 85 functions
        tc = memo.getTrafficController();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;
        this.speedStepMode = SpeedStepMode.TMCC1_32;
    }

    private final DccLocoAddress address;
    private final SerialTrafficController tc;

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    // Relating SERIAL_FUNCTION_CODES_TMCC1 to SpeedStepMode.TMCC1_32 and TMCC1_100;
    //    and SERIAL_FUNCTION_CODES_TMCC2 to SpeedStepMode.TMCC2_32 and TMCC2_200.
    private long[] getFnValueArray(int number) {
                
            if (number < 0) return new long[]{};
            
            if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC1_32 || getSpeedStepMode() == jmri.SpeedStepMode.TMCC1_100) {
                if (number < SERIAL_FUNCTION_CODES_TMCC1.length) {
                    return SERIAL_FUNCTION_CODES_TMCC1[number];
                } else {
                    return new long[]{};
                }
            } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC1TR_32 || getSpeedStepMode() == jmri.SpeedStepMode.TMCC1TR_100) {
                if (number < SERIAL_FUNCTION_CODES_TMCC1TR.length) {
                    return SERIAL_FUNCTION_CODES_TMCC1TR[number];
                } else {
                    return new long[]{};
                }
            } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2_32) {
                 if (number < SERIAL_FUNCTION_CODES_TMCC2_32.length) {
                     return SERIAL_FUNCTION_CODES_TMCC2_32[number];
                } else {
                    return new long[]{};
                }
            } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2TR_32) {
                 if (number < SERIAL_FUNCTION_CODES_TMCC2TR_32.length) {
                     return SERIAL_FUNCTION_CODES_TMCC2TR_32[number];
                } else {
                    return new long[]{};
                }
            } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2_200) {
                 if (number < SERIAL_FUNCTION_CODES_TMCC2_200.length) {
                     return SERIAL_FUNCTION_CODES_TMCC2_200[number];
                } else {
                    return new long[]{};
                }
           } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2TR_200) {
                 if (number < SERIAL_FUNCTION_CODES_TMCC2TR_200.length) {
                     return SERIAL_FUNCTION_CODES_TMCC2TR_200[number];
                } else {
                    return new long[]{};
                }
            }
            return new long[]{};
        }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunction(int func, boolean value) {
        updateFunction(func, value);
        
        int numberOfTriple = 0; // ordinal number of triple being processed, e.g. 1, 2, 3, ...
            checksumAccumulator = 0; // zeroes out accumulated checksum after each instance
        
        if (getFnValueArray(func).length >0) {
            for (long triple : getFnValueArray(func)) {
                numberOfTriple = numberOfTriple+1;
                
                // process each returned command
                if (func>=0 && func < SERIAL_FUNCTION_CODES_TMCC1.length) {
                    if ( triple > 0xFFFF ) {
                        // TMCC 2 format
                        if (triple > 0xFFFFFF ) {
                            int first =  (int)(triple >> 24);
                            int second = (int)(triple & 0xFFFFFF);
                            accumulateChecksum(first);
                            accumulateChecksum(second);
                            
                            // if this is the third triple, place the 
                            // checksum in its lowest byte
                            if (numberOfTriple == 3) {
                                second = (second &0xFFFFFF00) | ( (~checksumAccumulator) & 0xFF);
                            }
                            
                            // doubles are only sent once, not repeating
                            sendOneWordOnce(first  + address.getNumber() * 512);
                            sendOneWordOnce(second + address.getNumber() * 512);
                            
                        } else {
                            // single message
                            int content = (int)triple + address.getNumber() * 512;
                            accumulateChecksum(content);
                            
                            // if this is the third triple, place the 
                            // checksum in its lowest byte
                            if (numberOfTriple == 3) {
                                content = (content &0xFFFFFF00) | ( (~checksumAccumulator) & 0xFF);
                            }

                            sendFnToLayout(content, func);
                        }
                    } else {
                        // TMCC 1 format
                        sendFnToLayout((int)triple + address.getNumber() * 128, func);
                    }
                } else {
                    super.setFunction(func, value);
                }
            }
        } else {
            super.setFunction(func, value);
        }
    }

    // accumulate the checksum values into
    // the checksumAccumulator variable
    //
    // Takes the two bottom bytes _only_ and adds them to the accumulation.
    int checksumAccumulator = 0;
    private void accumulateChecksum(int input) {
        int byte1 = input&0xFF;
        int byte2 = (input >> 8)&0xFF;
        checksumAccumulator = (checksumAccumulator + byte1+byte2) & 0xFF;
    }
    
    // the argument is a long containing 3 bytes. 
    // The first byte is the message opcode
    private void sendOneWordOnce(int word) {
        SerialMessage m = new SerialMessage(word);
        tc.sendSerialMessage(m, null);
    }

    // TMCC 1 Function Keys to trigger with TMCC1_32 and TMCC1_100 speed steps.
    private final static long[][] SERIAL_FUNCTION_CODES_TMCC1 = new long[][] {

        // TMCC1 Remote - Defined FnKeys
        {0x00000D}, // Fn0 (Headlamp)
        {0x00001D}, // Fn1 (Bell)
        {0x00001C}, // Fn2 (Horn/Whistle)
        {0x000005}, // Fn3 (F - Open Front Coupler)
        {0x000006}, // Fn4 (R - Open Rear Coupler)

        // TMCC1 Remote - Defined KeyPad FnKeys
        {0x000011}, {0x000012}, {0x000013}, /* Fn5-7 */ // 1-2-3
        {0x000014}, {0x000015}, {0x000016}, /* Fn8-10 */ // 4-5-6
        {0x000017}, {0x000018}, {0x000019}, /* Fn11-13 */ // 7-8-9
                    {0x000010},             /* Fn14 */ // 0

        // TMCC1 Remote - Defined FnKeys
        {0x000009}, // Fn15 (Aux1)
        {0x00001E}, // Fn16 (Letoff Sound)
        {0x000004}, // Fn17 (Boost)
        {0x000007}, // Fn18 (Brake)
        {0x000028}, // Fn19 (Momentum Low)
        {0x000029}, // Fn20 (Momentum Medium)
        {0x00002A}, // Fn21 (Momentum High)
        {0x00002B}, // Fn22 (Set)
        {0x00001F}, // Fn23 (Horn 2)

        // TMCC1 RR Speed FnKeys
        {0x000064}, // Fn24 ( 4)   5mph
        {0x00006A}, // Fn25 (10)  20mph
        {0x00006E}, // Fn26 (14)  35mph
        {0x000072}, // Fn27 (18)  50mph
        {0x000078}, // Fn28 (24)  70mph
        {0x00007F}, // Fn29 (31)   Full

        // TMCC1 ERR - Set SpeedSteps
        {0x000009, 0x000010, 0x000009, 0x000010, 0x000004}, // Fn30 (Set ERR 100 SpeedSteps)
        {0x000009, 0x000010, 0x000009, 0x000010, 0x000007}, // Fn31 (Set ERR  32 SpeedSteps)

        // TMCC1 Acela/Subway FnKeys
        {0x000009, 0x000005}, // Fn32 (Open Doors - Left)
        {0x00000D, 0x000005, 0x00000D}, // Fn33 (Close Doors - Left)
        {0x000009, 0x000006}, // Fn34 (Open Doors - Right)
        {0x00000D, 0x000006, 0x00000D}, // Fn35 (Close Doors - Right)
        {0x000009, 0x000013}, // Fn36 (Pantagraph - Automatic/Prototypical)
        {0x000009, 0x000015}, // Fn37 (Pantagraph - Down)
        {0x000009, 0x000016}, // Fn38 (Pantagraph - Manual Mode/Cycles Through Positions)
        {0x000009, 0x00001C}, // Fn39 (Toggle Horn - City/Country)
        {0x000009, 0x000018}, // Fn40 (Cab Light - Off)
        {0x000009, 0x000019}, // Fn41 (Cab Light - On)
        {0x000009, 0x00000D, 0x000018, 0x00000D}, // Fn42 (Interior Lights - Off)
        {0x000009, 0x00000D, 0x000019, 0x00000D}, // Fn43 (Interior Lights - On)

        // TMCC1 Break-Down B Unit
        {0x000009, 0x000004}, // Fn44 Start Breakdown Sequence
        {0x000009, 0x000015}, // Fn45 Made It Back to the Yard
        {0x000009, 0x000013}, // Fn46 Restart Unit/Repairs Complete

        // TMCC1 Boxcar/LiveStock Car
        {0x000009, 0x000013}, // Fn47 Load
        {0x000009, 0x000012}, // Fn48 Flat Wheel Sound

        // TMCC1 Passenger/Dining Cars
        {0x000009, 0x000017}, // Fn49 Station PA Arrival Dialog
        {0x000009, 0x000012}, // Fn50 Conductor Arrival Dialog

        // TMCC1 Crane/Boom Car
        {0x000009, 0x000011, 0x000007}, // Fn51 Lower the Boom
        {0x000009, 0x000011, 0x000004}, // Fn52 Raises the Boom
        {0x000009, 0x000012, 0x000007}, // Fn53 Lowers Main/Large Hook
        {0x000009, 0x000012, 0x000004}, // Fn54 Raises Main/Large Hook
        {0x000017, 0x000013, 0x000007}, // Fn55 Lowers Small Hook
        {0x000017, 0x000013, 0x000004}, // Fn56 Raises Small Hook
        {0x000004, 0x000014}, // Fn57 Front Work Lights (Toggles On/Off)
        {0x000004, 0x000015}, // Fn58 Rear Work Lights (Toggles On/Off)
        {0x000004, 0x000016}, // Fn59 Launches Outriggers
        {0x000009, 0x000017}, // Fn60 Crew Dialog Off
        {0x000009, 0x000018}, // Fn61 All Sounds Off
        {0x000009, 0x000019}, // Fn62 All Sounds On

        // TMCC1 Unassigned FnKeys
        {0x00002E}, // Fn63 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00002E}, // Fn64 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00002E}, // Fn65 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00002E}, // Fn66 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00002E}, // Fn67 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00002E}, // Fn68 Code to Trigger SerialMonFrame Message/Unassigned FnKey

        // TMCC1 Aux FnKeys
        {0x000008}, // Fnxx (Aux1 Off)
        {0x000009}, // Fnxx (Aux1 Option 1 - On While Held)
        {0x00000A}, // Fnxx (Aux1 Option 2 - Toggle On/Toggle Off)
        {0x00000B}, // Fnxx (Aux1 On)
        {0x00000C}, // Fnxx (Aux2 Off)
        {0x00000D}, // Fnxx (Aux2 Option 1 - Toggle On/Toggle Off)
        {0x00000E}, // Fnxx (Aux2 Option 2 - On While Held)
        {0x00000F}, // Fnxx (Aux2 On)

    };

    // TMCC 1 TR Function Keys to trigger with TMCC1TR_32 and TMCC1TR_100 speed steps.
    private final static long[][] SERIAL_FUNCTION_CODES_TMCC1TR = new long[][] {

        // TMCC1TR Remote - Defined FnKeys
        {0x00C80D}, // Fn0 (Headlamp)
        {0x00C81D}, // Fn1 (Bell)
        {0x00C81C}, // Fn2 (Horn/Whistle)
        {0x00C805}, // Fn3 (F - Open Front Coupler)
        {0x00C806}, // Fn4 (R - Open Rear Coupler)

        // TMCC1TR Remote - Defined KeyPad FnKeys
        {0x00C811}, {0x00C812}, {0x00C813}, /* Fn5-7 */ // 1-2-3
        {0x00C814}, {0x00C815}, {0x00C816}, /* Fn8-10 */ // 4-5-6
        {0x00C817}, {0x00C818}, {0x00C819}, /* Fn11-13 */ // 7-8-9
                    {0x00C810},             /* Fn14 */ // 0

        // TMCC1TR Remote - Defined FnKeys
        {0x00C809}, // Fn15 (Aux1)
        {0x00C81E}, // Fn16 (Letoff Sound)
        {0x00C804}, // Fn17 (Boost)
        {0x00C807}, // Fn18 (Brake)
        {0x00C828}, // Fn19 (Momentum Low)
        {0x00C829}, // Fn20 (Momentum Medium)
        {0x00C82A}, // Fn21 (Momentum High)
        {0x00C82B}, // Fn22 (Set)
        {0x00C81F}, // Fn23 (Horn 2)

        // TMCC1TR RR Speed FnKeys
        {0x00C864}, // Fn24 ( 4)   5mph
        {0x00C86A}, // Fn25 (10)  20mph
        {0x00C86E}, // Fn26 (14)  35mph
        {0x00C872}, // Fn27 (18)  50mph
        {0x00C878}, // Fn28 (24)  70mph
        {0x00C87F}, // Fn29 (31)   Full

        // TMCC1TR ERR - Set SpeedSteps
        {0x00C809, 0x00C810, 0x00C809, 0x00C810, 0x00C804}, // Fn30 (Set ERR 100 SpeedSteps)
        {0x00C809, 0x00C810, 0x00C809, 0x00C810, 0x00C807}, // Fn31 (Set ERR  32 SpeedSteps)

        // TMCC1TR Acela/Subway FnKeys
        {0x00C809, 0x00C805}, // Fn32 (Open Doors - Left)
        {0x00C80D, 0x00C805, 0x00C80D}, // Fn33 (Close Doors - Left)
        {0x00C809, 0x00C806}, // Fn34 (Open Doors - Right)
        {0x00C80D, 0x00C806, 0x00C80D}, // Fn35 (Close Doors - Right)
        {0x00C809, 0x00C813}, // Fn36 (Pantagraph - Automatic/Prototypical)
        {0x00C809, 0x00C815}, // Fn37 (Pantagraph - Down)
        {0x00C809, 0x00C816}, // Fn38 (Pantagraph - Manual Mode/Cycles Through Positions)
        {0x00C809, 0x00C81C}, // Fn39 (Toggle Horn - City/Country)
        {0x00C809, 0x00C818}, // Fn40 (Cab Light - Off)
        {0x00C809, 0x00C819}, // Fn41 (Cab Light - On)
        {0x00C809, 0x00C80D, 0x00C818, 0x00C80D}, // Fn42 (Interior Lights - Off)
        {0x00C809, 0x00C80D, 0x00C819, 0x00C80D}, // Fn43 (Interior Lights - On)

        // TMCC1TR Break-Down B Unit
        {0x00C809, 0x00C804}, // Fn44 Start Breakdown Sequence
        {0x00C809, 0x00C815}, // Fn45 Made It Back to the Yard
        {0x00C809, 0x00C813}, // Fn46 Restart Unit/Repairs Complete

        // TMCC1TR Boxcar/LiveStock Car
        {0x00C809, 0x00C813}, // Fn47 Load
        {0x00C809, 0x00C812}, // Fn48 Flat Wheel Sound

        // TMCC1TR Passenger/Dining Cars
        {0x00C809, 0x00C817}, // Fn49 Station PA Arrival Dialog
        {0x00C809, 0x00C812}, // Fn50 Conductor Arrival Dialog

        // TMCC1TR Crane/Boom Car
        {0x00C809, 0x00C811, 0x00C807}, // Fn51 Lower the Boom
        {0x00C809, 0x00C811, 0x00C804}, // Fn52 Raises the Boom
        {0x00C809, 0x00C812, 0x00C807}, // Fn53 Lowers Main/Large Hook
        {0x00C809, 0x00C812, 0x00C804}, // Fn54 Raises Main/Large Hook
        {0x00C817, 0x00C813, 0x00C807}, // Fn55 Lowers Small Hook
        {0x00C817, 0x00C813, 0x00C804}, // Fn56 Raises Small Hook
        {0x00C804, 0x00C814}, // Fn57 Front Work Lights (Toggles On/Off)
        {0x00C804, 0x00C815}, // Fn58 Rear Work Lights (Toggles On/Off)
        {0x00C804, 0x00C816}, // Fn59 Launches Outriggers
        {0x00C809, 0x00C817}, // Fn60 Crew Dialog Off
        {0x00C809, 0x00C818}, // Fn61 All Sounds Off
        {0x00C809, 0x00C819}, // Fn62 All Sounds On

        // TMCC1TR Unassigned FnKeys
        {0x00C82E}, // Fn63 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00C82E}, // Fn64 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00C82E}, // Fn65 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00C82E}, // Fn66 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00C82E}, // Fn67 Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0x00C82E}, // Fn68 Code to Trigger SerialMonFrame Message/Unassigned FnKey

        // TMCC1TR Aux FnKeys
        {0x00C808}, // Fnxx (Aux1 Off)
        {0x00C809}, // Fnxx (Aux1 Option 1 - On While Held)
        {0x00C80A}, // Fnxx (Aux1 Option 2 - Toggle On/Toggle Off)
        {0x00C80B}, // Fnxx (Aux1 On)
        {0x00C80C}, // Fnxx (Aux2 Off)
        {0x00C80D}, // Fnxx (Aux2 Option 1 - Toggle On/Toggle Off)
        {0x00C80E}, // Fnxx (Aux2 Option 2 - On While Held)
        {0x00C80F}, // Fnxx (Aux2 On)

    };


    /**
    * Translate TMCC1 function numbers to line characters.
    * If the upper byte is zero, it will be replaced by 0xF8
    * and the address will be set in the low position.
    * If the upper byte is non-zero, that value will be sent,
    * and the address will be set in the upper (TMCC2) position.
    * If six bytes are specified (with the upper one non-zero), 
    * this will be interpreted as two commands to be sequentially sent,
    * with the upper bytes sent first.
    */

    // TMCC 2 Legacy Function Keys to trigger with TMCC2_32 speed steps.
    private final static long[][] SERIAL_FUNCTION_CODES_TMCC2_32 = new long[][] {

        // TMCC2_32 Remote - Defined FnKeys
        {0xF8010D}, // Fn0 (Headlamp)
        {0xF8011D}, // Fn1 (Bell)
        {0xF8011C}, // Fn2 (Horn/Whistle)
        {0xF80105}, // Fn3 (F - Open Front Coupler)
        {0xF80106}, // Fn4 (R - Open Rear Coupler)

        // TMCC2_32 Remote - Defined KeyPad FnKeys
        {0xF80111}, {0xF80112}, {0xF80113}, /* Fn5-7 */ // 1-2-3
        {0xF80114}, {0xF80115}, {0xF80116}, /* Fn8-10 */ // 4-5-6
        {0xF80117}, {0xF80118}, {0xF80119}, /* Fn11-13 */ // 7-8-9
                    {0xF80110},             /* Fn14 */ // 0

        // TMCC2_32 Remote - Defined FnKeys
        {0xF80109}, // Fn15 (Aux1)
        {0xF8011E}, // Fn16 (Letoff Sound)
        {0xF80104}, // Fn17 (Boost)
        {0xF80107}, // Fn18 (Brake)
        {0xF80128}, // Fn19 (Momentum Low)
        {0xF80129}, // Fn20 (Momentum Medium)
        {0xF8012A}, // Fn21 (Momentum High)
        {0xF8012B}, // Fn22 (Set)
        {0xF8011F}, // Fn23 (Horn 2)

        // TMCC2_32 RR Speed FnKeys
        {0xF80164}, // Fn24 ( 4)   5mph
        {0xF8016A}, // Fn25 (10)  20mph
        {0xF8016E}, // Fn26 (14)  35mph
        {0xF80172}, // Fn27 (18)  50mph
        {0xF80178}, // Fn28 (24)  70mph
        {0xF8017F}, // Fn29 (31)   Full

        // TMCC2_32 Extended Lighting FnKeys
        {0xF8017D, 0xFB00E8, 0xFB0000}, // Fn30 (Mars Lt On)
        {0xF8017D, 0xFB00E9, 0xFB0000}, // Fn31 (Mars Lt Off)

        {0xF8017D, 0xFB00D0, 0xFB0000}, // Fn32 (Ground Lt On)
        {0xF8017D, 0xFB00D1, 0xFB0000}, // Fn33 (Ground Lt Off)
        {0xF8017D, 0xFB00D2, 0xFB0000}, // Fn34 (Ground Lt Auto)

        {0xF8017D, 0xFB00A0, 0xFB0000}, // Fn35 (DogHouse On)
        {0xF8017D, 0xFB00A1, 0xFB0000}, // Fn36 (DogHouse Off)

        {0xF8017D, 0xFB00CC, 0xFB0000}, // Fn37 (Tender Marker On)
        {0xF8017D, 0xFB00CD, 0xFB0000}, // Fn38 (Tender Marker Off)

        {0xF8017D, 0xFB00F4, 0xFB0000}, // Fn39 (Rule 17 On)
        {0xF8017D, 0xFB00F5, 0xFB0000}, // Fn40 (Rule 17 Off)
        {0xF8017D, 0xFB00F6, 0xFB0000}, // Fn41 (Rule 17 Auto)

        {0xF8017D, 0xFB00C0, 0xFB0000}, // Fn42 (Ditch Lt On)
        {0xF8017D, 0xFB00C1, 0xFB0000}, // Fn43 (Ditch Lt On; Pulse Off with Horn)
        {0xF8017D, 0xFB00C2, 0xFB0000}, // Fn44 (Ditch Lt Off; Pulse On with Horn)
        {0xF8017D, 0xFB00C3, 0xFB0000}, // Fn45 (Ditch Lt Off)

        {0xF8017D, 0xFB00F0, 0xFB0000}, // Fn46 (Cab Lt On)
        {0xF8017D, 0xFB00F1, 0xFB0000}, // Fn47 (Cab Lt Off)
        {0xF8017D, 0xFB00F2, 0xFB0000}, // Fn48 (Cab Lt Auto)

        {0xF8017D, 0xFB00C8, 0xFB0000}, // Fn49 (Loco Marker On)
        {0xF8017D, 0xFB00C9, 0xFB0000}, // Fn50 (Loco Marker Off)

        {0xF8017D, 0xFB00B0, 0xFB0000}, // Fn51 (Hazard Lt On)
        {0xF8017D, 0xFB00B1, 0xFB0000}, // Fn52 (Hazard Lt Off)
        {0xF8017D, 0xFB00B2, 0xFB0000}, // Fn53 (Hazard Lt Auto)

        {0xF8017D, 0xFB00E0, 0xFB0000}, // Fn54 (Strobe Lt On - Single Flash)
        {0xF8017D, 0xFB00E1, 0xFB0000}, // Fn55 (Strobe Lt On - Double Flash)
        {0xF8017D, 0xFB00E2, 0xFB0000}, // Fn56 (Strobe Lt Off)

        {0xF8017D, 0xFB00F8, 0xFB0000}, // Fn57 (Car Cabin Lt On)
        {0xF8017D, 0xFB00F9, 0xFB0000}, // Fn58 (Car Cabin Lt Off)
        {0xF8017D, 0xFB00FA, 0xFB0000}, // Fn59 (Car Cabin Lt Auto)

        // TMCC2 Acela/Subway FnKeys
        {0xF80109, 0xF80112}, // Fn60 (Crew: Report Speed - Moving)
        {0xF80109, 0xF80115}, // Fn61 (Tower: Emergency Stop/Crew: Ack - Moving)
        {0xF8017C, 0xFB0020, 0xFB0000}, // Fn62 (Open Doors - Left)
        {0xF8017C, 0xFB0021, 0xFB0000}, // Fn63 (Close Doors - Left)
        {0xF8017C, 0xFB0022, 0xFB0000}, // Fn64 (Open Doors - Right)
        {0xF8017C, 0xFB0023, 0xFB0000}, // Fn65 (Close Doors - Right)
        {0xF8017C, 0xFB0010, 0xFB0000}, // Fn66 (Pantagraph - Up/F)
        {0xF8017C, 0xFB0011, 0xFB0000}, // Fn67 (Pantagraph - Down/F)
        {0xF8017C, 0xFB0012, 0xFB0000}, // Fn68 (Pantagraph - Up/R)
        {0xF8017C, 0xFB0013, 0xFB0000}, // Fn69 (Pantagraph - Down/R)
        
        // Only TMCC1 Break-Down B Unit

        // TMCC2 Boxcar/Livestock Car
        {0xF8017C, 0xFB0030, 0xFB0000}, // Fn70 (Option1 On)
        {0xF8017C, 0xFB0031, 0xFB0000}, // Fn71 (Opiton1 Off)
        {0xF8017C, 0xFB0032, 0xFB0000}, // Fn72 (Option2 On)
        {0xF8017C, 0xFB0033, 0xFB0000}, // Fn73 (Option2 Off)
        {0xF8017C, 0xFB0034, 0xFB0000}, // Fn74 (Load)
        {0xF8017C, 0xFB0035, 0xFB0000}, // Fn75 (Unload)
        {0xF8017C, 0xFB0036, 0xFB0000}, // Fn76 (FRED On)
        {0xF8017C, 0xFB0037, 0xFB0000}, // Fn77 (FRED Off)
        {0xF8017C, 0xFB0038, 0xFB0000}, // Fn78 (Flat Wheel On)
        {0xF8017C, 0xFB0039, 0xFB0000}, // Fn79 (Flat Wheel Off)
        {0xF8017C, 0xFB003A, 0xFB0000}, // Fn80 (Game On)
        {0xF8017C, 0xFB003B, 0xFB0000}, // Fn81 (Game Off)

        // Only TMCC1 Passenger/Dining Cars

        // Only TMCC1 Crane/Boom Car

        // TMCC2 Smoke System
        {0xF8017C, 0xFB0000, 0xFB0000}, // Fn82 (Smoke System Off)
        {0xF8017C, 0xFB0001, 0xFB0000}, // Fn83 (Smoke System Low)
        {0xF8017C, 0xFB0002, 0xFB0000}, // Fn84 (Smoke System Med)
        {0xF8017C, 0xFB0003, 0xFB0000}, // Fn85 (Smoke System High)

        // TMCC2_32 Unassigned FnKeys
        {0xF8012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0xF8012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey

        // TMCC2_32 Aux FnKeys
        {0xF80108}, // Fnxx (Aux1 Off)
        {0xF80109}, // Fnxx (Aux1 Option 1 - On While Held) 
        {0xF8010A}, // Fnxx (Aux1 Option 2 - Toggle On/Toggle Off)
        {0xF8010B}, // Fnxx (Aux1 On)
        {0xF8010C}, // Fnxx (Aux2 Off)
        {0xF8010D}, // Fnxx (Aux2 Option 1 - Toggle On/Toggle Off) 
        {0xF8010E}, // Fnxx (Aux2 Option 2 - On While Held)
        {0xF8010F}, // Fnxx (Aux2 On)

};

    // TMCC 2 Legacy TR Function Keys to trigger with TMCC2TR_32 speed steps.
    private final static long[][] SERIAL_FUNCTION_CODES_TMCC2TR_32 = new long[][] {

        // TMCC2TR_32 Remote - Defined FnKeys
        {0xF9010D}, // Fn0 (Headlamp)
        {0xF9011D}, // Fn1 (Bell)
        {0xF9011C}, // Fn2 (Horn/Whistle)
        {0xF90105}, // Fn3 (F - Open Front Coupler)
        {0xF90106}, // Fn4 (R - Open Rear Coupler)

        // TMCC2TR_32 Remote - Defined KeyPad FnKeys
        {0xF90111}, {0xF90112}, {0xF90113}, /* Fn5-7 */ // 1-2-3
        {0xF90114}, {0xF90115}, {0xF90116}, /* Fn8-10 */ // 4-5-6
        {0xF90117}, {0xF90118}, {0xF90119}, /* Fn11-13 */ // 7-8-9
                    {0xF90110},             /* Fn14 */ // 0

        // TMCC2TR_32 Remote - Defined FnKeys
        {0xF90109}, // Fn15 (Aux1)
        {0xF9011E}, // Fn16 (Letoff Sound)
        {0xF90104}, // Fn17 (Boost)
        {0xF90107}, // Fn18 (Brake)
        {0xF90128}, // Fn19 (Momentum Low)
        {0xF90129}, // Fn20 (Momentum Medium)
        {0xF9012A}, // Fn21 (Momentum High)
        {0xF9012B}, // Fn22 (Set)
        {0xF9011F}, // Fn23 (Horn 2)

        // TMCC2TR_32 RR Speed FnKeys
        {0xF90164}, // Fn24 ( 4)   5mph
        {0xF9016A}, // Fn25 (10)  20mph
        {0xF9016E}, // Fn26 (14)  35mph
        {0xF90172}, // Fn27 (18)  50mph
        {0xF90178}, // Fn28 (24)  70mph
        {0xF9017F}, // Fn29 (31)   Full

        // TMCC2TR_32 Extended Lighting FnKeys
        {0xF9017D, 0xFB00E8, 0xFB0000}, // Fn30 (Mars Lt On)
        {0xF9017D, 0xFB00E9, 0xFB0000}, // Fn31 (Mars Lt Off)

        {0xF9017D, 0xFB00D0, 0xFB0000}, // Fn32 (Ground Lt On)
        {0xF9017D, 0xFB00D1, 0xFB0000}, // Fn33 (Ground Lt Off)
        {0xF9017D, 0xFB00D2, 0xFB0000}, // Fn34 (Ground Lt Auto)

        {0xF9017D, 0xFB00A0, 0xFB0000}, // Fn35 (DogHouse On)
        {0xF9017D, 0xFB00A1, 0xFB0000}, // Fn36 (DogHouse Off)

        {0xF9017D, 0xFB00CC, 0xFB0000}, // Fn37 (Tender Marker On)
        {0xF9017D, 0xFB00CD, 0xFB0000}, // Fn38 (Tender Marker Off)

        {0xF9017D, 0xFB00F4, 0xFB0000}, // Fn39 (Rule 17 On)
        {0xF9017D, 0xFB00F5, 0xFB0000}, // Fn40 (Rule 17 Off)
        {0xF9017D, 0xFB00F6, 0xFB0000}, // Fn41 (Rule 17 Auto)

        {0xF9017D, 0xFB00C0, 0xFB0000}, // Fn42 (Ditch Lt On)
        {0xF9017D, 0xFB00C1, 0xFB0000}, // Fn43 (Ditch Lt On; Pulse Off with Horn)
        {0xF9017D, 0xFB00C2, 0xFB0000}, // Fn44 (Ditch Lt Off; Pulse On with Horn)
        {0xF9017D, 0xFB00C3, 0xFB0000}, // Fn45 (Ditch Lt Off)

        {0xF9017D, 0xFB00F0, 0xFB0000}, // Fn46 (Cab Lt On)
        {0xF9017D, 0xFB00F1, 0xFB0000}, // Fn47 (Cab Lt Off)
        {0xF9017D, 0xFB00F2, 0xFB0000}, // Fn48 (Cab Lt Auto)

        {0xF9017D, 0xFB00C8, 0xFB0000}, // Fn49 (Loco Marker On)
        {0xF9017D, 0xFB00C9, 0xFB0000}, // Fn50 (Loco Marker Off)

        {0xF9017D, 0xFB00B0, 0xFB0000}, // Fn51 (Hazard Lt On)
        {0xF9017D, 0xFB00B1, 0xFB0000}, // Fn52 (Hazard Lt Off)
        {0xF9017D, 0xFB00B2, 0xFB0000}, // Fn53 (Hazard Lt Auto)

        {0xF9017D, 0xFB00E0, 0xFB0000}, // Fn54 (Strobe Lt On - Single Flash)
        {0xF9017D, 0xFB00E1, 0xFB0000}, // Fn55 (Strobe Lt On - Double Flash)
        {0xF9017D, 0xFB00E2, 0xFB0000}, // Fn56 (Strobe Lt Off)

        {0xF9017D, 0xFB00F8, 0xFB0000}, // Fn57 (Car Cabin Lt On)
        {0xF9017D, 0xFB00F9, 0xFB0000}, // Fn58 (Car Cabin Lt Off)
        {0xF9017D, 0xFB00FA, 0xFB0000}, // Fn59 (Car Cabin Lt Auto)

        // TMCC2TR Acela/Subway FnKeys
        {0xF90109, 0xF90112}, // Fn60 (Crew: Report Speed - Moving)
        {0xF90109, 0xF90115}, // Fn61 (Tower: Emergency Stop/Crew: Ack - Moving)
        {0xF9017C, 0xFB0020, 0xFB0000}, // Fn62 (Open Doors - Left)
        {0xF9017C, 0xFB0021, 0xFB0000}, // Fn63 (Close Doors - Left)
        {0xF9017C, 0xFB0022, 0xFB0000}, // Fn64 (Open Doors - Right)
        {0xF9017C, 0xFB0023, 0xFB0000}, // Fn65 (Close Doors - Right)
        {0xF9017C, 0xFB0010, 0xFB0000}, // Fn66 (Pantagraph - Up/F)
        {0xF9017C, 0xFB0011, 0xFB0000}, // Fn67 (Pantagraph - Down/F)
        {0xF9017C, 0xFB0012, 0xFB0000}, // Fn68 (Pantagraph - Up/R)
        {0xF9017C, 0xFB0013, 0xFB0000}, // Fn69 (Pantagraph - Down/R)
        
        // Only TMCC1 Break-Down B Unit

        // TMCC2TR Boxcar/Livestock Car
        {0xF9017C, 0xFB0030, 0xFB0000}, // Fn70 (Option1 On)
        {0xF9017C, 0xFB0031, 0xFB0000}, // Fn71 (Opiton1 Off)
        {0xF9017C, 0xFB0032, 0xFB0000}, // Fn72 (Option2 On)
        {0xF9017C, 0xFB0033, 0xFB0000}, // Fn73 (Option2 Off)
        {0xF9017C, 0xFB0034, 0xFB0000}, // Fn74 (Load)
        {0xF9017C, 0xFB0035, 0xFB0000}, // Fn75 (Unload)
        {0xF9017C, 0xFB0036, 0xFB0000}, // Fn76 (FRED On)
        {0xF9017C, 0xFB0037, 0xFB0000}, // Fn77 (FRED Off)
        {0xF9017C, 0xFB0038, 0xFB0000}, // Fn78 (Flat Wheel On)
        {0xF9017C, 0xFB0039, 0xFB0000}, // Fn79 (Flat Wheel Off)
        {0xF9017C, 0xFB003A, 0xFB0000}, // Fn80 (Game On)
        {0xF9017C, 0xFB003B, 0xFB0000}, // Fn81 (Game Off)

        // Only TMCC1 Passenger/Dining Cars

        // Only TMCC1 Crane/Boom Car

        // TMCC2TR Smoke System
        {0xF9017C, 0xFB0000, 0xFB0000}, // Fn82 (Smoke System Off)
        {0xF9017C, 0xFB0001, 0xFB0000}, // Fn83 (Smoke System Low)
        {0xF9017C, 0xFB0002, 0xFB0000}, // Fn84 (Smoke System Med)
        {0xF9017C, 0xFB0003, 0xFB0000}, // Fn85 (Smoke System High)

        // TRMCC2TR_32 Unassigned FnKeys
        {0xF9012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0xF9012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey

        // TRMCC2TR_32 Aux FnKeys
        {0xF90108}, // Fnxx (Aux1 Off)
        {0xF90109}, // Fnxx (Aux1 Option 1 - On While Held) 
        {0xF9010A}, // Fnxx (Aux1 Option 2 - Toggle On/Toggle Off)
        {0xF9010B}, // Fnxx (Aux1 On)
        {0xF9010C}, // Fnxx (Aux2 Off)
        {0xF9010D}, // Fnxx (Aux2 Option 1 - Toggle On/Toggle Off) 
        {0xF9010E}, // Fnxx (Aux2 Option 2 - On While Held)
        {0xF9010F}, // Fnxx (Aux2 On)

};

    // TMCC 2 Legacy Function Keys to trigger with TMCC2_200 speed steps.
    private final static long[][] SERIAL_FUNCTION_CODES_TMCC2_200 = new long[][] {

        // TMCC2_200 Remote - Defined FnKeys
        {0xF8010D}, // Fn0 (Headlamp)
        {0xF8011D}, // Fn1 (Bell)
        {0xF8011C}, // Fn2 (Horn/Whistle)
        {0xF80105}, // Fn3 (F - Open Front Coupler)
        {0xF80106}, // Fn4 (R - Open Rear Coupler)

        // TMCC2_200 Remote - Defined KeyPad FnKeys
        {0xF80111}, {0xF80112}, {0xF80113}, /* Fn5-7 */ // 1-2-3
        {0xF80114}, {0xF80115}, {0xF80116}, /* Fn8-10 */ // 4-5-6
        {0xF80117}, {0xF80118}, {0xF80119}, /* Fn11-13 */ // 7-8-9
                    {0xF80110},             /* Fn14 */ // 0

        // TMCC2_200 Remote - Defined FnKeys
        {0xF80109}, // Fn15 (Aux1)
        {0xF8011E}, // Fn16 (Letoff Sound)
        {0xF80104}, // Fn17 (Boost)
        {0xF80107}, // Fn18 (Brake)
        {0xF80128}, // Fn19 (Momentum Low)
        {0xF80129}, // Fn20 (Momentum Medium)
        {0xF8012A}, // Fn21 (Momentum High)
        {0xF8012B}, // Fn22 (Set)
        {0xF8011F}, // Fn23 (Horn 2)

        // TMCC2_200 RR Speed FnKeys
        {0xF8000A}, // Fn24 ( 10)   5mph
        {0xF80028}, // Fn25 ( 40)  20mph
        {0xF80046}, // Fn26 ( 70)  35mph
        {0xF80064}, // Fn27 (100)  50mph
        {0xF8008C}, // Fn28 (140)  70mph
        {0xF800C7}, // Fn29 (199)   Full

        // TMCC2_200 Extended Lighting FnKeys
        {0xF8017D, 0xFB00E8, 0xFB0000}, // Fn30 (Mars Lt On)
        {0xF8017D, 0xFB00E9, 0xFB0000}, // Fn31 (Mars Lt Off)

        {0xF8017D, 0xFB00D0, 0xFB0000}, // Fn32 (Ground Lt On)
        {0xF8017D, 0xFB00D1, 0xFB0000}, // Fn33 (Ground Lt Off)
        {0xF8017D, 0xFB00D2, 0xFB0000}, // Fn34 (Ground Lt Auto)

        {0xF8017D, 0xFB00A0, 0xFB0000}, // Fn35 (DogHouse On)
        {0xF8017D, 0xFB00A1, 0xFB0000}, // Fn36 (DogHouse Off)

        {0xF8017D, 0xFB00CC, 0xFB0000}, // Fn37 (Tender Marker On)
        {0xF8017D, 0xFB00CD, 0xFB0000}, // Fn38 (Tender Marker Off)

        {0xF8017D, 0xFB00F4, 0xFB0000}, // Fn39 (Rule 17 On)
        {0xF8017D, 0xFB00F5, 0xFB0000}, // Fn40 (Rule 17 Off)
        {0xF8017D, 0xFB00F6, 0xFB0000}, // Fn41 (Rule 17 Auto)

        {0xF8017D, 0xFB00C0, 0xFB0000}, // Fn42 (Ditch Lt On)
        {0xF8017D, 0xFB00C1, 0xFB0000}, // Fn43 (Ditch Lt On; Pulse Off with Horn)
        {0xF8017D, 0xFB00C2, 0xFB0000}, // Fn44 (Ditch Lt Off; Pulse On with Horn)
        {0xF8017D, 0xFB00C3, 0xFB0000}, // Fn45 (Ditch Lt Off)

        {0xF8017D, 0xFB00F0, 0xFB0000}, // Fn46 (Cab Lt On)
        {0xF8017D, 0xFB00F1, 0xFB0000}, // Fn47 (Cab Lt Off)
        {0xF8017D, 0xFB00F2, 0xFB0000}, // Fn48 (Cab Lt Auto)

        {0xF8017D, 0xFB00C8, 0xFB0000}, // Fn49 (Loco Marker On)
        {0xF8017D, 0xFB00C9, 0xFB0000}, // Fn50 (Loco Marker Off)

        {0xF8017D, 0xFB00B0, 0xFB0000}, // Fn51 (Hazard Lt On)
        {0xF8017D, 0xFB00B1, 0xFB0000}, // Fn52 (Hazard Lt Off)
        {0xF8017D, 0xFB00B2, 0xFB0000}, // Fn53 (Hazard Lt Auto)

        {0xF8017D, 0xFB00E0, 0xFB0000}, // Fn54 (Strobe Lt On - SingleFlash)
        {0xF8017D, 0xFB00E1, 0xFB0000}, // Fn55 (Strobe Lt On - DoubleFlash)
        {0xF8017D, 0xFB00E2, 0xFB0000}, // Fn56 (Strobe Lt Off)

        {0xF8017D, 0xFB00F8, 0xFB0000}, // Fn57 (Car Cabin Lt On)
        {0xF8017D, 0xFB00F9, 0xFB0000}, // Fn58 (Car Cabin Lt Off)
        {0xF8017D, 0xFB00FA, 0xFB0000}, // Fn59 (Car Cabin Lt Auto)

        // TMCC2 Acela/Subway FnKeys
        {0xF80109, 0xF80112}, // Fn60 (Crew: Report Speed - Moving)
        {0xF80109, 0xF80115}, // Fn61 (Tower: Emergency Stop/Crew: Ack - Moving)
        {0xF8017C, 0xFB0020, 0xFB0000}, // Fn62 (Open Doors - Left)
        {0xF8017C, 0xFB0021, 0xFB0000}, // Fn63 (Close Doors - Left)
        {0xF8017C, 0xFB0022, 0xFB0000}, // Fn64 (Open Doors - Right)
        {0xF8017C, 0xFB0023, 0xFB0000}, // Fn65 (Close Doors - Right)
        {0xF8017C, 0xFB0010, 0xFB0000}, // Fn66 (Pantagraph - Up/F)
        {0xF8017C, 0xFB0011, 0xFB0000}, // Fn67 (Pantagraph - Down/F)
        {0xF8017C, 0xFB0012, 0xFB0000}, // Fn68 (Pantagraph - Up/R)
        {0xF8017C, 0xFB0013, 0xFB0000}, // Fn69 (Pantagraph - Down/R)

        // Only TMCC1 Break-Down B Unit

        // TMCC2 Boxcar/Livestock Car
        {0xF8017C, 0xFB0030, 0xFB0000}, // Fn70 (Option1 On)
        {0xF8017C, 0xFB0031, 0xFB0000}, // Fn71 (Opiton1 Off)
        {0xF8017C, 0xFB0032, 0xFB0000}, // Fn72 (Option2 On)
        {0xF8017C, 0xFB0033, 0xFB0000}, // Fn73 (Option2 Off)
        {0xF8017C, 0xFB0034, 0xFB0000}, // Fn74 (Load)
        {0xF8017C, 0xFB0035, 0xFB0000}, // Fn75 (Unload)
        {0xF8017C, 0xFB0036, 0xFB0000}, // Fn76 (FRED On)
        {0xF8017C, 0xFB0037, 0xFB0000}, // Fn77 (FRED Off)
        {0xF8017C, 0xFB0038, 0xFB0000}, // Fn78 (Flat Wheel On)
        {0xF8017C, 0xFB0039, 0xFB0000}, // Fn79 (Flat Wheel Off)
        {0xF8017C, 0xFB003A, 0xFB0000}, // Fn80 (Game On)
        {0xF8017C, 0xFB003B, 0xFB0000}, // Fn81 (Game Off)

        // Only TMCC1 Passenger/Dining Cars

        // Only TMCC1 Crane/Boom Car

        // TMCC2 Smoke System
        {0xF8017C, 0xFB0000, 0xFB0000}, // Fn82 (Smoke System Off)
        {0xF8017C, 0xFB0001, 0xFB0000}, // Fn83 (Smoke System Low)
        {0xF8017C, 0xFB0002, 0xFB0000}, // Fn84 (Smoke System Med)
        {0xF8017C, 0xFB0003, 0xFB0000}, // Fn85 (Smoke System High)

        // TRMCC2_200 Unassigned FnKeys
        {0xF8012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0xF8012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey

        // TMCC2_200 Aux FnKeys
        {0xF80108}, // Fnxx (Aux1 Off)
        {0xF80109}, // Fnxx (Aux1 Option 1 - On While Held) 
        {0xF8010A}, // Fnxx (Aux1 Option 2 - Toggle On/Toggle Off)
        {0xF8010B}, // Fnxx (Aux1 On)
        {0xF8010C}, // Fnxx (Aux2 Off)
        {0xF8010D}, // Fnxx (Aux2 Option 1 - Toggle On/Toggle Off) 
        {0xF8010E}, // Fnxx (Aux2 Option 2 - On While Held)
        {0xF8010F}, // Fnxx (Aux2 On)

    };

    // TMCC 2 Legacy TR Function Keys to trigger with TMCC2TR_200 speed steps.
    private final static long[][] SERIAL_FUNCTION_CODES_TMCC2TR_200 = new long[][] {

        // TMCC2TR_200 Remote - Defined FnKeys
        {0xF9010D}, // Fn0 (Headlamp)
        {0xF9011D}, // Fn1 (Bell)
        {0xF9011C}, // Fn2 (Horn/Whistle)
        {0xF90105}, // Fn3 (F - Open Front Coupler)
        {0xF90106}, // Fn4 (R - Open Rear Coupler)

        // TMCC2TR_200 Remote - Defined KeyPad FnKeys
        {0xF90111}, {0xF90112}, {0xF90113}, /* Fn5-7 */ // 1-2-3
        {0xF90114}, {0xF90115}, {0xF90116}, /* Fn8-10 */ // 4-5-6
        {0xF90117}, {0xF90118}, {0xF90119}, /* Fn11-13 */ // 7-8-9
                    {0xF90110},             /* Fn14 */ // 0

        // TMCC2TR_200 Remote - Defined FnKeys
        {0xF90109}, // Fn15 (Aux1)
        {0xF9011E}, // Fn16 (Letoff Sound)
        {0xF90104}, // Fn17 (Boost)
        {0xF90107}, // Fn18 (Brake)
        {0xF90128}, // Fn19 (Momentum Low)
        {0xF90129}, // Fn20 (Momentum Medium)
        {0xF9012A}, // Fn21 (Momentum High)
        {0xF9012B}, // Fn22 (Set)
        {0xF9011F}, // Fn23 (Horn 2)

        // TMCC2TR_200 RR Speed FnKeys
        {0xF9000A}, // Fn24 ( 10)   5mph
        {0xF90028}, // Fn25 ( 40)  20mph
        {0xF90046}, // Fn26 ( 70)  35mph
        {0xF90064}, // Fn27 (100)  50mph
        {0xF9008C}, // Fn28 (140)  70mph
        {0xF900C7}, // Fn29 (199)   Full

        // TMCC2TR_200 Extended Lighting FnKeys
        {0xF9017D, 0xFB00E8, 0xFB0000}, // Fn30 (Mars Lt On)
        {0xF9017D, 0xFB00E9, 0xFB0000}, // Fn31 (Mars Lt Off)

        {0xF9017D, 0xFB00D0, 0xFB0000}, // Fn32 (Ground Lt On)
        {0xF9017D, 0xFB00D1, 0xFB0000}, // Fn33 (Ground Lt Off)
        {0xF9017D, 0xFB00D2, 0xFB0000}, // Fn34 (Ground Lt Auto)

        {0xF9017D, 0xFB00A0, 0xFB0000}, // Fn35 (DogHouse On)
        {0xF9017D, 0xFB00A1, 0xFB0000}, // Fn36 (DogHouse Off)

        {0xF9017D, 0xFB00CC, 0xFB0000}, // Fn37 (Tender Marker On)
        {0xF9017D, 0xFB00CD, 0xFB0000}, // Fn38 (Tender Marker Off)

        {0xF9017D, 0xFB00F4, 0xFB0000}, // Fn39 (Rule 17 On)
        {0xF9017D, 0xFB00F5, 0xFB0000}, // Fn40 (Rule 17 Off)
        {0xF9017D, 0xFB00F6, 0xFB0000}, // Fn41 (Rule 17 Auto)

        {0xF9017D, 0xFB00C0, 0xFB0000}, // Fn42 (Ditch Lt On)
        {0xF9017D, 0xFB00C1, 0xFB0000}, // Fn43 (Ditch Lt On; Pulse Off with Horn)
        {0xF9017D, 0xFB00C2, 0xFB0000}, // Fn44 (Ditch Lt Off; Pulse On with Horn)
        {0xF9017D, 0xFB00C3, 0xFB0000}, // Fn45 (Ditch Lt Off)

        {0xF9017D, 0xFB00F0, 0xFB0000}, // Fn46 (Cab Lt On)
        {0xF9017D, 0xFB00F1, 0xFB0000}, // Fn47 (Cab Lt Off)
        {0xF9017D, 0xFB00F2, 0xFB0000}, // Fn48 (Cab Lt Auto)

        {0xF9017D, 0xFB00C8, 0xFB0000}, // Fn49 (Loco Marker On)
        {0xF9017D, 0xFB00C9, 0xFB0000}, // Fn50 (Loco Marker Off)

        {0xF9017D, 0xFB00B0, 0xFB0000}, // Fn51 (Hazard Lt On)
        {0xF9017D, 0xFB00B1, 0xFB0000}, // Fn52 (Hazard Lt Off)
        {0xF9017D, 0xFB00B2, 0xFB0000}, // Fn53 (Hazard Lt Auto)

        {0xF9017D, 0xFB00E0, 0xFB0000}, // Fn54 (Strobe Lt On - SingleFlash)
        {0xF9017D, 0xFB00E1, 0xFB0000}, // Fn55 (Strobe Lt On - DoubleFlash)
        {0xF9017D, 0xFB00E2, 0xFB0000}, // Fn56 (Strobe Lt Off)

        {0xF9017D, 0xFB00F8, 0xFB0000}, // Fn57 (Car Cabin Lt On)
        {0xF9017D, 0xFB00F9, 0xFB0000}, // Fn58 (Car Cabin Lt Off)
        {0xF9017D, 0xFB00FA, 0xFB0000}, // Fn59 (Car Cabin Lt Auto)

        // TMCC2TR Acela/Subway FnKeys
        {0xF90109, 0xF90112}, // Fn60 (Crew: Report Speed - Moving)
        {0xF90109, 0xF90115}, // Fn61 (Tower: Emergency Stop/Crew: Ack - Moving)
        {0xF9017C, 0xFB0020, 0xFB0000}, // Fn62 (Open Doors - Left)
        {0xF9017C, 0xFB0021, 0xFB0000}, // Fn63 (Close Doors - Left)
        {0xF9017C, 0xFB0022, 0xFB0000}, // Fn64 (Open Doors - Right)
        {0xF9017C, 0xFB0023, 0xFB0000}, // Fn65 (Close Doors - Right)
        {0xF9017C, 0xFB0010, 0xFB0000}, // Fn66 (Pantagraph - Up/F)
        {0xF9017C, 0xFB0011, 0xFB0000}, // Fn67 (Pantagraph - Down/F)
        {0xF9017C, 0xFB0012, 0xFB0000}, // Fn68 (Pantagraph - Up/R)
        {0xF9017C, 0xFB0013, 0xFB0000}, // Fn69 (Pantagraph - Down/R)

        // Only TMCC1 Break-Down B Unit

        // TMCC2TR Boxcar/Livestock Car
        {0xF9017C, 0xFB0030, 0xFB0000}, // Fn70 (Option1 On)
        {0xF9017C, 0xFB0031, 0xFB0000}, // Fn71 (Opiton1 Off)
        {0xF9017C, 0xFB0032, 0xFB0000}, // Fn72 (Option2 On)
        {0xF9017C, 0xFB0033, 0xFB0000}, // Fn73 (Option2 Off)
        {0xF9017C, 0xFB0034, 0xFB0000}, // Fn74 (Load)
        {0xF9017C, 0xFB0035, 0xFB0000}, // Fn75 (Unload)
        {0xF9017C, 0xFB0036, 0xFB0000}, // Fn76 (FRED On)
        {0xF9017C, 0xFB0037, 0xFB0000}, // Fn77 (FRED Off)
        {0xF9017C, 0xFB0038, 0xFB0000}, // Fn78 (Flat Wheel On)
        {0xF9017C, 0xFB0039, 0xFB0000}, // Fn79 (Flat Wheel Off)
        {0xF9017C, 0xFB003A, 0xFB0000}, // Fn80 (Game On)
        {0xF9017C, 0xFB003B, 0xFB0000}, // Fn81 (Game Off)

        // Only TMCC1 Passenger/Dining Cars

        // Only TMCC1 Crane/Boom Car

        // TMCC2TR Smoke System
        {0xF9017C, 0xFB0000, 0xFB0000}, // Fn82 (Smoke System Off)
        {0xF9017C, 0xFB0001, 0xFB0000}, // Fn83 (Smoke System Low)
        {0xF9017C, 0xFB0002, 0xFB0000}, // Fn84 (Smoke System Med)
        {0xF9017C, 0xFB0003, 0xFB0000}, // Fn85 (Smoke System High)

        // TRMCC2TR_200 Unassigned FnKeys
        {0xF9012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey
        {0xF9012E}, // Fnxx Code to Trigger SerialMonFrame Message/Unassigned FnKey

        // TMCC2TR_200 Aux FnKeys
        {0xF90108}, // Fnxx (Aux1 Off)
        {0xF90109}, // Fnxx (Aux1 Option 1 - On While Held) 
        {0xF9010A}, // Fnxx (Aux1 Option 2 - Toggle On/Toggle Off)
        {0xF9010B}, // Fnxx (Aux1 On)
        {0xF9010C}, // Fnxx (Aux2 Off)
        {0xF9010D}, // Fnxx (Aux2 Option 1 - Toggle On/Toggle Off) 
        {0xF9010E}, // Fnxx (Aux2 Option 2 - On While Held)
        {0xF9010F}, // Fnxx (Aux2 On)

    };


     int previousValue;
     int newValue;

    /**
     * Set the speed.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */

    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed;
        synchronized(this) {
            oldSpeed = this.speedSetting;
            this.speedSetting = speed;
        }
        
        // Option TMCC2_200 "Absolute" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC2_200) {

            // TMCC2 Legacy 200 speed step mode
            int value = (int) (199 * speed); // max value to send is 199 in 200 step mode
            if (value > 199) {
                // max possible speed
                value = 199;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF8);
    
            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFF8B);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);

            } else {
                // normal speed setting
                m.putAsWord(0x0000 + (address.getNumber() << 9) + value);

                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }
        }

        // Option TMCC2TR_200 "Absolute" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC2TR_200) {

            // TMCC2 Legacy TR 200 speed step mode
            int value = (int) (199 * speed); // max value to send is 199 in 200 step mode
            if (value > 199) {
                // max possible speed
                value = 199;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF9);
    
            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFF8B);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);

            } else {
                // normal speed setting
                m.putAsWord(0x0000 + (address.getNumber() << 9) + value);

                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }
        }

        // Option TMCC1_100 "Relative" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1_100) {
            
          /** 
            * TMCC1 MedMomentum, HighMomentum and 100 speed step mode
            * purpose is to increase resolution of 32 bits
            * across 100 throttle 'clicks'
          */

            int value = (int) (99 * speed); // max value to send is 99 in 100 step mode
            if (value > 99) {
                // max possible speed step
                value = 99;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);

            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFFFF);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }
            if (value == 0) {
                // normal speed step setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value);

                // send to layout (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }

            if (value > 0) {
                 newValue = value;
                 if (newValue > previousValue) {
                    // increase TMCC "Relative" speed +1 (repeat * valueChange46)
                    int valueChange46 = (newValue - previousValue);
                    for (int i = 0x0000; i < valueChange46; i++) {
                        m.putAsWord(0x0046 + address.getNumber() * 128);
                        tc.sendSerialMessage(m, null);
                    }
                }
                if (newValue < previousValue) {
                    // decrease TMCC "Relative" speed -1 (repeat * valueChange44)
                    int valueChange44 = (previousValue - newValue);
                    for (int j = 0x0000; j < valueChange44; j++) {
                        m.putAsWord(0x0044 + address.getNumber() * 128);
                        tc.sendSerialMessage(m, null);
                    }
                }
            previousValue = newValue;                
            }
        }

        // Option TMCC1TR_100 "Relative" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1TR_100) {
            
          /** 
            * TMCC1TR MedMomentum, HighMomentum and 100 speed step mode
            * purpose is to increase resolution of 32 bits
            * across 100 throttle 'clicks'
          */

            int value = (int) (99 * speed); // max value to send is 99 in 100 step mode
            if (value > 99) {
                // max possible speed step
                value = 99;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);

            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFFFF);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }
            if (value == 0) {
                // normal speed step setting
                m.putAsWord(0xC860 + address.getNumber() * 128 + value);

                // send to layout (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }

            if (value > 0) {
                 newValue = value;
                 if (newValue > previousValue) {
                    // increase TMCC "Relative" speed +1 (repeat * valueChange46)
                    int valueChange46 = (newValue - previousValue);
                    for (int i = 0x0000; i < valueChange46; i++) {
                        m.putAsWord(0xC846 + address.getNumber() * 128);
                        tc.sendSerialMessage(m, null);
                    }
                }
                if (newValue < previousValue) {
                    // decrease TMCC "Relative" speed -1 (repeat * valueChange44)
                    int valueChange44 = (previousValue - newValue);
                    for (int j = 0x0000; j < valueChange44; j++) {
                        m.putAsWord(0xC844 + address.getNumber() * 128);
                        tc.sendSerialMessage(m, null);
                    }
                }
            previousValue = newValue;                
            }
        }

        // Option TMCC2_32 "Absolute" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC2_32) {

            // TMCC2 Legacy 32 speed step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                // max possible speed
                value = 31;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF8);
    
            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFF8B);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);

            } else {
                // normal speed setting
                m.putAsWord(0x0160 + address.getNumber() * 512 + value);

                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }
        }

        // Option TMCC2TR_32 "Absolute" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC2TR_32) {

            // TMCC2 Legacy TR 32 speed step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                // max possible speed
                value = 31;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF9);
    
            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFF8B);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);

            } else {
                // normal speed setting
                m.putAsWord(0x0160 + address.getNumber() * 512 + value);

                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
            }
        }

        // Option TMCC1_32 "Absolute" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1_32) {

            // TMCC1 32 speed step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                // max possible speed
                value = 31;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);
    
            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFFFF);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);

            } else {
                // normal speed setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value);

                // send to layout (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);           
            }
        }
                  
        // Option TMCC1TR_32 "Absolute" speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1TR_32) {

            // TMCC1TR 32 speed step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                // max possible speed
                value = 31;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);
    
            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFFFF);

                // send to layout (send 4 times to ensure received)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);

            } else {
                // normal speed setting
                m.putAsWord(0xC860 + address.getNumber() * 128 + value);

                // send to layout (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(m, null);           
            }
        }

        synchronized(this) {
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;

        // notify layout
        SerialMessage m = new SerialMessage();
        if (speedStepMode == jmri.SpeedStepMode.TMCC1_32 || speedStepMode == jmri.SpeedStepMode.TMCC1_100) {
            m.setOpCode(0xFE);
            if (forward) {
                m.putAsWord(0x0000 + address.getNumber() * 128);
                setSpeedSetting(0.0f);
            } else {
                m.putAsWord(0x0003 + address.getNumber() * 128);
                setSpeedSetting(0.0f);
            }
        }

        if (speedStepMode == jmri.SpeedStepMode.TMCC1TR_32 || speedStepMode == jmri.SpeedStepMode.TMCC1TR_100) {
            m.setOpCode(0xFE);
            if (forward) {
                m.putAsWord(0xC800 + address.getNumber() * 128);
                setSpeedSetting(0.0f);
            } else {
                m.putAsWord(0xC803 + address.getNumber() * 128);
                setSpeedSetting(0.0f);
            }
        }

        if (speedStepMode == jmri.SpeedStepMode.TMCC2_32 || speedStepMode == jmri.SpeedStepMode.TMCC2_200) {
            m.setOpCode(0xF8);
            if (forward) {
                m.putAsWord(0x0100 + address.getNumber() * 512);
                setSpeedSetting(0.0f);
            } else {
                m.putAsWord(0x0103 + address.getNumber() * 512);
                setSpeedSetting(0.0f);
            }
        }

        if (speedStepMode == jmri.SpeedStepMode.TMCC2TR_32 || speedStepMode == jmri.SpeedStepMode.TMCC2TR_200) {
            m.setOpCode(0xF9);
            if (forward) {
                m.putAsWord(0x0100 + address.getNumber() * 512);
                setSpeedSetting(0.0f);
            } else {
                m.putAsWord(0x0103 + address.getNumber() * 512);
                setSpeedSetting(0.0f);
            }
        }

        // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);

        firePropertyChange(ISFORWARD, old, isForward);
    }

    /**
     * Send these messages to the layout and repeat
     * while button is pressed/on.
     * @param value Content of message to be sent in three bytes
     * @param func  The number of the function being addressed
     */
    protected void sendFnToLayout(int value, int func) {
        
        if (speedStepMode == jmri.SpeedStepMode.TMCC2_200 || speedStepMode == jmri.SpeedStepMode.TMCC2TR_200) {
            if (func == 24) {
                setSpeedSetting(0.055f);
                return;
            }
            if (func == 25) {
                setSpeedSetting(0.205f);
                return;
            }
            if (func == 26) {
                setSpeedSetting(0.355f);
                return;
            }
            if (func == 27) {
                setSpeedSetting(0.505f);
                return;
            }
            if (func == 28) {
                setSpeedSetting(0.705f);
                return;
            }
            if (func == 29) {
                setSpeedSetting(1.0f);
                return;
            }
        }

        if (speedStepMode == jmri.SpeedStepMode.TMCC1_100 || speedStepMode == jmri.SpeedStepMode.TMCC1TR_100) {
            if (func == 24) {
                setSpeedSetting(0.055f);
                return;
            }
            if (func == 25) {
                setSpeedSetting(0.205f);
                return;
            }
            if (func == 26) {
                setSpeedSetting(0.355f);
                return;
            }
            if (func == 27) {
                setSpeedSetting(0.505f);
                return;
            }
            if (func == 28) {
                setSpeedSetting(0.705f);
                return;
            }
            if (func == 29) {
                setSpeedSetting(1.0f);
                return;
            }
        }

        if (speedStepMode == jmri.SpeedStepMode.TMCC1_32 || speedStepMode == jmri.SpeedStepMode.TMCC1TR_32 || speedStepMode == jmri.SpeedStepMode.TMCC2_32 || speedStepMode == jmri.SpeedStepMode.TMCC2TR_32) {
            if (func == 24) {
                setSpeedSetting(0.130f);
                return;
            }
            if (func == 25) {
                setSpeedSetting(0.320f);
                return;
            }
            if (func == 26) {
                setSpeedSetting(0.450f);
                return;
            }
            if (func == 27) {
                setSpeedSetting(0.580f);
                return;
            }
            if (func == 28) {
                setSpeedSetting(0.775f);
                return;
            }
            if (func == 29) {
                setSpeedSetting(1.0f);
                return;
            }
        }

        /**
        * This code sends FnKey presses to the command station. 
        * Send once is set, per the need of TMCC multi-key commands that
        * do not work when a specific command sequence is not followed.
        * If these multi-key commands are integrated into single FnKeys,
        * this "send" section can be converted back to "send twice" as
        * the other send sequences througout tmcc\SerialThrottle.java.
        */

        repeatFunctionSendWhileOn(value, func); // Single FnKey Press, Single Send; FnKey Held, Repeats FnKey while pressed.
    }

    /**
    * This code block is necessary to support the send repeats of
    * the repeatFunctionSendWhileOn(value, func); code above.
    * This code block "Sends Again" if FkKey is still pressed/on, and
    * repeats per the interval set in static final int REPEAT_TIME.
    */

    static final int REPEAT_TIME = 150;

    protected void repeatFunctionSendWhileOn(int value, int func) {
        if (getFunction(func)) {
            tc.sendSerialMessage(new SerialMessage(value), null);
            jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
                repeatFunctionSendWhileOn(value, func);
            }, REPEAT_TIME);
        }
    }

    /*
     * Set the speed step value.
     * <p>
     * The speed step range is from 32 steps, to 100 steps, to 200 steps
     *
     * @param mode only TMCC1_32, TMCC2_32, TMCC1_100 and TMCC2_200 are allowed
     */
    @Override
    public void setSpeedStepMode(jmri.SpeedStepMode mode) {
        if (mode == jmri.SpeedStepMode.TMCC1_32 || mode == jmri.SpeedStepMode.TMCC1TR_32 || mode == jmri.SpeedStepMode.TMCC2_32 || mode == jmri.SpeedStepMode.TMCC2TR_32 || mode == jmri.SpeedStepMode.TMCC1_100 || mode == jmri.SpeedStepMode.TMCC1TR_100 || mode == jmri.SpeedStepMode.TMCC2_200 || mode == jmri.SpeedStepMode.TMCC2TR_200) {
            super.setSpeedStepMode(mode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throttleDispose() {
        finishRecord();
    }

}
