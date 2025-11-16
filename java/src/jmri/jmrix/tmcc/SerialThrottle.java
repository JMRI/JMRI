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
        super(memo, 69); // supports 69 functions
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
            } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2_32) {
                 if (number < SERIAL_FUNCTION_CODES_TMCC2_32.length) {
                     return SERIAL_FUNCTION_CODES_TMCC2_32[number];
                } else {
                    return new long[]{};
                }
            } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2_200) {
                 if (number < SERIAL_FUNCTION_CODES_TMCC2_200.length) {
                     return SERIAL_FUNCTION_CODES_TMCC2_200[number];
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
        
        if (getFnValueArray(func).length >0) {
            for (long triple : getFnValueArray(func)) {
                // process each returned command
                if (func>=0 && func < SERIAL_FUNCTION_CODES_TMCC1.length) {
                    if ( triple > 0xFFFF ) {
                        // TMCC 2 format
                        if (triple > 0xFFFFFF ) {
                            int first =  (int)(triple >> 24);
                            int second = (int)(triple & 0xFFFFFF);
                            // doubles are only sent once, not repeating
                            sendOneWordOnce(first  + address.getNumber() * 512);
                            sendOneWordOnce(second + address.getNumber() * 512);
                        } else {
                            // single message
                            sendFnToLayout((int)triple + address.getNumber() * 512, func);
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
        {0x00007F}, // Fn29 (31) 100mph (or whatever speed for full throttle)

        // TMCC1 ERR - Set SpeedSteps
        {0x000009, 0x000010, 0x000009, 0x000010, 0x000004}, // Fn30 (Set ERR 100 SpeedSteps)
        {0x000009, 0x000010, 0x000009, 0x000010, 0x000007}, // Fn31 (Set ERR  32 SpeedSteps)

        // TMCC1 Acela/Subway FnKeys
        {0x000009, 0x000006}, // Fn32 (Open Doors - Right)
        {0x00000D, 0x000006, 0x00000D}, // Fn33 (Close Doors - Right)
        {0x000009, 0x000005}, // Fn34 (Open Doors - Left)
        {0x00000D, 0x000005, 0x00000D}, // Fn35 (Close Doors - Left)
        {0x000009, 0x000013}, // Fn36 (Pantagraph - Automatic/Prototypical)
        {0x000009, 0x000015}, // Fn37 (Pantagraph - Down)
        {0x000009, 0x000015}, // Fn38 (Pantagraph - Manual Mode/Cycles Through Positions)
        {0x000009, 0x00001C}, // Fn39 (Toggle Horn - City/Country)
        {0x000009, 0x000018}, // Fn40 (Cab Light - Off)
        {0x000009, 0x000019}, // Fn41 (Cab Light - On)
        {0x000009, 0x00000D, 0x000018, 0x00000D}, // Fn42 (Interior Lights - Off)
        {0x000009, 0x00000D, 0x000019, 0x00000D}, // Fn43 (Interior Lights - On)

        // TMCC1 Break-Down Unit

        // TMCC1 Freight Cars

        // TMCC1 Passenger Cars

        // TMCC1 Crane/Boom Car

        // TMCC1 Aux FnKeys
        {0x000008}, // Fn32 (Aux1 Off)
        {0x000009}, // Fn33 (Aux1 Option 1 - On While Held)
        {0x00000A}, // Fn34 (Aux1 Option 2 - Toggle On/Toggle Off)
        {0x00000B}, // Fn35 (Aux1 On)
        {0x00000C}, // Fn36 (Aux2 Off)
        {0x00000D}, // Fn37 (Aux2 Option 1 - Toggle On/Toggle Off)
        {0x00000E}, // Fn38 (Aux2 Option 2 - On While Held)
        {0x00000F}, // Fn39 (Aux2 On)

        // TMCC1 Unused FnKeys
        {0x00002E}, // Fn40
        {0x00002E}, // Fn41
        {0x00002E}, // Fn42
        {0x00002E}, // Fn43
        {0x00002E}, // Fn44
        {0x00002E}, // Fn45
        {0x00002E}, // Fn46
        {0x00002E}, // Fn47
        {0x00002E}, // Fn48
        {0x00002E}, // Fn49
        {0x00002E}, // Fn50
        {0x00002E}, // Fn51
        {0x00002E}, // Fn52
        {0x00002E}, // Fn53
        {0x00002E}, // Fn54
        {0x00002E}, // Fn55
        {0x00002E}, // Fn56
        {0x00002E}, // Fn57
        {0x00002E}, // Fn58
        {0x00002E}, // Fn59
        {0x00002E}, // Fn60
        {0x00002E}, // Fn61
        {0x00002E}, // Fn62
        {0x00002E}, // Fn63
        {0x00002E}, // Fn64
        {0x00002E}, // Fn65
        {0x00002E}, // Fn66
        {0x00002E}, // Fn67
        {0x00002E}, // Fn68
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
        {0xF8017F}, // Fn29 (31) 100mph (or whatever speed for full throttle)

        // TMCC2_32 Extended Lighting FnKeys
        // Fn33 (Mars On)
        // Fn34 (Mars Off)

        // Fn35 (Ground Lt On)
        // Fn36 (Ground Lt Off)
        // Fn37 (Ground Lt Auto)

        // Fn38 (DogHouse On)
        // Fn39 (DogHouse Off)

        // Fn40 (Tender Marker On)
        // Fn41 (Tender Marker Off)

        // Fn42 (Rule 17 On)
        // Fn43 (Rule 17 Off)
        // Fn44 (Rule 17 Auto)

        // Fn45 (Ditch Lt On)
        // Fn46 (Ditch Lt On; Pulse Off with Horn)
        // Fn47 (Ditch Lt Off; Pulse On with Horn)
        // Fn48 (Ditch Lt Off)

        // Fn49 (Cab Lt On)
        // Fn50 (Cab Lt Off)
        {0xF8017D, 0xFB00F2, 0xFB0000}, // Fn51 (Cab Lt Auto)

        // Fn52 (Loco Marker On)
        // Fn53 (Loco Marker Off)

        // Fn54 (Hazard Lt On)
        // Fn55 (Hazard Lt Off)
        // Fn56 (Hazard Lt Auto)

        // Fn57 (Strobe Lt On - Single Flash)
        // Fn58 (Strobe Lt On - Double Flash)
        // Fn59 (Strobe Lt Off)

        // Fn60 (Car Cabin Lt On)
        // Fn61 (Car Cabin Lt Off)
        // Fn62 (Car Cabin Lt Auto)


        // Extended Sound Effects FnKeys
        // {0xF801FB, 0xF801FC}, // Fn35 Start Up Sequence 1 (Delayed Prime Mover, then Immediate Start Up)
        // {0xF801FC}, // Fn36 Start Up Sequence 2 (Immediate Start Up)
        // {0xF801FD, 0xF801FE}, // Fn37 Shut Down Sequence 1 (Delay w/ Announcement then Immediate Shut Down)
        // {0xF801FE}, // Fn38 Shut down Sequence 2 (Immediate Shut Down)


        // TRMCC2_32 Aux FnKeys
        {0xF80108}, // Fn30 (Aux1 Off)
        {0xF80109}, // Fn31 (Aux1 Option 1 - On While Held) 
        {0xF8010A}, // Fn32 (Aux1 Option 2 - Toggle On/Toggle Off)
        {0xF8010B}, // Fn33 (Aux1 On)
        {0xF8010C}, // Fn34 (Aux2 Off)
        {0xF8010D}, // Fn35 (Aux2 Option 1 - Toggle On/Toggle Off) 
        {0xF8010E}, // Fn36 (Aux2 Option 2 - On While Held)
        {0xF8010F}, // Fn37 (Aux2 On)

        // TRMCC2_32 Unused FnKeys
        {0xF8012E}, // Fn38
        {0xF8012E}, // Fn39
        {0xF8012E}, // Fn40
        {0xF8012E}, // Fn41
        {0xF8012E}, // Fn42
        {0xF8012E}, // Fn43
        {0xF8012E}, // Fn44
        {0xF8012E}, // Fn45
        {0xF8012E}, // Fn46
        {0xF8012E}, // Fn47
        {0xF8012E}, // Fn48
        {0xF8012E}, // Fn49
        {0xF8012E}, // Fn50
        {0xF8012E}, // Fn51
        {0xF8012E}, // Fn52
        {0xF8012E}, // Fn53
        {0xF8012E}, // Fn54
        {0xF8012E}, // Fn55
        {0xF8012E}, // Fn56
        {0xF8012E}, // Fn57
        {0xF8012E}, // Fn58
        {0xF8012E}, // Fn59
        {0xF8012E}, // Fn60
        {0xF8012E}, // Fn61
        {0xF8012E}, // Fn62
        {0xF8012E}, // Fn63
        {0xF8012E}, // Fn64
        {0xF8012E}, // Fn65
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
        {0xF800C7}, // Fn29 (199) 100mph (or whatever speed for full throttle)

        // TMCC2_200 Extended Lighting FnKeys

        // {0xF8017D, 0xFB01F2, 0xFB0189}, // Fn35 Set Cab Light Auto (test!!!)


        // Extended Sound Effects FnKeys
        // {0xF801FB, 0xF801FC}, // Fn35 Start Up Sequence 1 (Delayed Prime Mover, then Immediate Start Up)
        // {0xF801FC}, // Fn36 Start Up Sequence 2 (Immediate Start Up)
        // {0xF801FD, 0xF801FE}, // Fn37 Shut Down Sequence 1 (Delay w/ Announcement then Immediate Shut Down)
        // {0xF801FE}, // Fn38 Shut down Sequence 2 (Immediate Shut Down)


        // TMCC2_200 Aux FnKeys
        {0xF80108}, // Fn30 (Aux1 Off)
        {0xF80109}, // Fn31 (Aux1 Option 1 - On While Held) 
        {0xF8010A}, // Fn32 (Aux1 Option 2 - Toggle On/Toggle Off)
        {0xF8010B}, // Fn33 (Aux1 On)
        {0xF8010C}, // Fn34 (Aux2 Off)
        {0xF8010D}, // Fn35 (Aux2 Option 1 - Toggle On/Toggle Off) 
        {0xF8010E}, // Fn36 (Aux2 Option 2 - On While Held)
        {0xF8010F}, // Fn37 (Aux2 On)

        // TMCC2_200 Unused FnKeys
        {0xF8012E}, // Fn38
        {0xF8012E}, // Fn39
        {0xF8012E}, // Fn40
        {0xF8012E}, // Fn41
        {0xF8012E}, // Fn42
        {0xF8012E}, // Fn43
        {0xF8012E}, // Fn44
        {0xF8012E}, // Fn45
        {0xF8012E}, // Fn46
        {0xF8012E}, // Fn47
        {0xF8012E}, // Fn48
        {0xF8012E}, // Fn49
        {0xF8012E}, // Fn50
        {0xF8012E}, // Fn51
        {0xF8012E}, // Fn52
        {0xF8012E}, // Fn53
        {0xF8012E}, // Fn54
        {0xF8012E}, // Fn55
        {0xF8012E}, // Fn56
        {0xF8012E}, // Fn57
        {0xF8012E}, // Fn58
        {0xF8012E}, // Fn59
        {0xF8012E}, // Fn60
        {0xF8012E}, // Fn61
        {0xF8012E}, // Fn62
        {0xF8012E}, // Fn63
        {0xF8012E}, // Fn64
        {0xF8012E}, // Fn65
    };

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
        
        // send to layout option 200 speed steps
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
            } else {
                // normal speed setting
                m.putAsWord(0x0000 + (address.getNumber() << 9) + value);
            }
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);
        }

        // send to layout option 100 speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1_100) {
            
          /** 
            * TMCC1 ERR 100 speed step mode
            * purpose is to increase resolution of 32 bits
            * across 100 throttle 'clicks' by dividing value by 3            
            * and setting top speed at 32
          */
            int value = (int) (99 * speed); // max value to send is 99 in 100 step mode
            if (value > 93) {
                // max possible speed step
                value = 93;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);

            if (value < 0) {
                // System HALT (immediate stop; ALL)
                m.putAsWord(0xFFFF);
            }
            if (value >= 0) {
                // normal speed step setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value / 3);
            }
                            
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);
        }

        // send to layout option TMCC2 32 speed steps
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
            } else {
                // normal speed setting
                m.putAsWord(0x0160 + address.getNumber() * 512 + value);
            }
    
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);           
        }

        // send to layout option TMCC1 32 speed steps
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
            } else {
                // normal speed setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value);
            }
    
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);           
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
        
        if (speedStepMode == jmri.SpeedStepMode.TMCC2_200) {
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

        if (speedStepMode == jmri.SpeedStepMode.TMCC1_32 || speedStepMode == jmri.SpeedStepMode.TMCC1_100 || speedStepMode == jmri.SpeedStepMode.TMCC2_32) {
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
     * Only 32 steps is available
     *
     * @param mode only TMCC1 32, TMCC2 32, TMCC1 100 and TMCC2 200 are allowed
     */
    @Override
    public void setSpeedStepMode(jmri.SpeedStepMode mode) {
        if (mode == jmri.SpeedStepMode.TMCC1_32 || mode == jmri.SpeedStepMode.TMCC2_32 || mode == jmri.SpeedStepMode.TMCC1_100 || mode == jmri.SpeedStepMode.TMCC2_200) {
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
