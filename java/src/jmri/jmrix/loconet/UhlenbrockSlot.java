package jmri.jmrix.loconet;


/**
 * Extends representation of a single slot for the UhlenBrock command station
 * <p>
 * Does specific Uhlenbrock Intellibox message handling.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Alain Le Marchand Copyright (C) 2014
 */
public class UhlenbrockSlot extends LocoNetSlot {

    public UhlenbrockSlot(int i) {
        super(i);
    }

    public UhlenbrockSlot(LocoNetMessage l) throws LocoNetException {
        super(l);
    }

    /**
     * Load functions 9 through 12 from LocoNet Uhlenbrock Intellibox-II
     * implementation to be used only for message with Op code
     * RE_OPC_IB2_F9_F12.
     * @param m a LocoNet message which contains an iB2 "function control" message.
     */
    public void iB2functionMessage(LocoNetMessage m) {
        // parse for F9-12 functions
        int funcs = m.getElement(2);
        localF9 = (funcs & LnConstants.RE_IB2_F9_MASK) != 0;
        localF10 = (funcs & LnConstants.RE_IB2_F10_MASK) != 0;
        localF11 = (funcs & LnConstants.RE_IB2_F11_MASK) != 0;
        localF12 = (funcs & LnConstants.RE_IB2_F12_MASK) != 0;
        notifySlotListeners();
    }

    /**
     * Load functions 9 through 28 from LocoNet Uhlenbrock Intellibox-I and -II
     * implementation to be used only for message with Op code
     * RE_OPC_IB2_SPECIAL.
     * @param m a LocoNet message which contains an iB "function control" message.
     */
    public void iBfunctionMessage(LocoNetMessage m) {
        // parse for which set of functions
        int token = m.getElement(3);
        int funcs = m.getElement(4);
        if (token == LnConstants.RE_IB1_SPECIAL_F5_F11_TOKEN) {
            // F9-12
            localF9 = (funcs & LnConstants.RE_IB1_F9_MASK) != 0;
            localF10 = (funcs & LnConstants.RE_IB1_F10_MASK) != 0;
            localF11 = (funcs & LnConstants.RE_IB1_F11_MASK) != 0;
            notifySlotListeners();
        } else if (token == LnConstants.RE_IB2_SPECIAL_F13_F19_TOKEN) {
            // check F13-19
            localF13 = (funcs & LnConstants.RE_IB2_F13_MASK) != 0;
            localF14 = (funcs & LnConstants.RE_IB2_F14_MASK) != 0;
            localF15 = (funcs & LnConstants.RE_IB2_F15_MASK) != 0;
            localF16 = (funcs & LnConstants.RE_IB2_F16_MASK) != 0;
            localF17 = (funcs & LnConstants.RE_IB2_F17_MASK) != 0;
            localF18 = (funcs & LnConstants.RE_IB2_F18_MASK) != 0;
            localF19 = (funcs & LnConstants.RE_IB2_F19_MASK) != 0;
            notifySlotListeners();
        } else if (token == LnConstants.RE_IB2_SPECIAL_F21_F27_TOKEN) {
            // check F21-27
            localF21 = (funcs & LnConstants.RE_IB2_F21_MASK) != 0;
            localF22 = (funcs & LnConstants.RE_IB2_F22_MASK) != 0;
            localF23 = (funcs & LnConstants.RE_IB2_F23_MASK) != 0;
            localF24 = (funcs & LnConstants.RE_IB2_F24_MASK) != 0;
            localF25 = (funcs & LnConstants.RE_IB2_F25_MASK) != 0;
            localF26 = (funcs & LnConstants.RE_IB2_F26_MASK) != 0;
            localF27 = (funcs & LnConstants.RE_IB2_F27_MASK) != 0;
            notifySlotListeners();
        } else if (token == LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN) {
            // check F12, F20 and  F28
            localF12 = (funcs & LnConstants.RE_IB2_SPECIAL_F12_MASK) != 0;
            localF20 = (funcs & LnConstants.RE_IB2_SPECIAL_F20_MASK) != 0;
            localF28 = (funcs & LnConstants.RE_IB2_SPECIAL_F28_MASK) != 0;
            notifySlotListeners();
        }
    }
}
