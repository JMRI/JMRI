package jmri;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates an NMRA packet containing the correct payload to enable or disable
 * pushbutton lockout. Currently supports the following Decoders NCE CVP AD4
 *
 *
 *
 * NCE is the easliest to implement, CV556 = 0 disable lockout, CV556 = 1 enable
 * lockout
 *
 * CVP is a bit tricker, CV514 controls the lockout for four turnouts. Each
 * turnout can have one or two button controls. Therefore the user must specify
 * if they are using one or two buttons for each turnout.
 *
 * From the CVP user manual:
 *
 * Function CV514 Lock all inputs 0 Unlock 1 1 Unlock 2 4 Unlock 3 16 Unlock 4
 * 64 Unlock all 85 Enable 2 button 255
 *
 * This routine assumes that for two button operations the following table is
 * true:
 *
 * Lock all inputs 0 Unlock 1 3 Unlock 2 12 Unlock 3 48 Unlock 4 192 Unlock all
 * 255
 *
 * Each CVP can operate up to four turnouts, luckly for us, they are sequential.
 * Also note that CVP decoder's use the old legacy format for ops mode
 * programming.
 *
 * @author Daniel Boudreau Copyright (C) 2007
 *
 */
public class PushbuttonPacket {

    /**
     * Valid stationary decoder names
     */
    public final static String unknown = "None";
    public final static String NCEname = "NCE_Rev_C";
    public final static String CVP_1Bname = "CVP_AD4_1B";
    public final static String CVP_2Bname = "CVP_AD4_2B";

    private final static String[] VALIDDECODERNAMES = {unknown, NCEname, CVP_1Bname,
        CVP_2Bname};

    /**
     * @throws IllegalArgumentException if input not OK
     * @return a DCC packet
     */
    @Nonnull
    public static byte[] pushbuttonPkt(@Nonnull String prefix, int turnoutNum, boolean locked) {

        Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(prefix + turnoutNum);
        byte[] bl;

        if (t == null || t.getDecoderName() == null ) {
            throw new IllegalArgumentException("No turnout or turnout decoder name");
        } else if (unknown.equals(t.getDecoderName())) {
            throw new IllegalArgumentException("Turnout decoder name is unknown");
        } else if (NCEname.equals(t.getDecoderName())) {
            if (locked) {
                bl = NmraPacket.accDecoderPktOpsMode(turnoutNum, 556, 1);
            } else {
                bl = NmraPacket.accDecoderPktOpsMode(turnoutNum, 556, 0);
            }
            if (bl == null) {
                throw new IllegalArgumentException("No valid DCC packet address");
            }
            return bl;

        // Note CVP decoders use the old legacy accessory  format
        } else if (CVP_1Bname.equals(t.getDecoderName())
                || CVP_2Bname.equals(t.getDecoderName())) {
            int CVdata = CVPturnoutLockout(prefix, turnoutNum);
            bl = NmraPacket.accDecoderPktOpsModeLegacy(turnoutNum, 514, CVdata);
            if (bl == null) {
                throw new IllegalArgumentException("No valid DCC packet address");
            }
            return bl;
        } else {
            log.error("Invalid decoder name for turnout " + turnoutNum);
            throw new IllegalArgumentException("Illegal decoder name");
        }
    }

    @Nonnull
    public static String[] getValidDecoderNames() {
        String[] arrayCopy = new String[VALIDDECODERNAMES.length];

        System.arraycopy(VALIDDECODERNAMES, 0, arrayCopy, 0, VALIDDECODERNAMES.length);
        return arrayCopy;
    }

    // builds the data byte for CVP decoders, builds based on JMRI's current
    // knowledge of turnout pushbutton lockout states. If a turnout doesn't
    // exist, assume single button operation.
    private static int CVPturnoutLockout(@Nonnull String prefix, int turnoutNum) {

        int CVdata = 0;
        int oneButton = 1;       // one pushbutton enable
        int twoButton = 3;       // two pushbutton enable
        int modTurnoutNum = (turnoutNum - 1) & 0xFFC; // mask off bits, there are 4 turnouts per
        // decoder

        for (int i = 0; i < 4; i++) {
            // set the default for one button in case the turnout doesn't exist
            int button = oneButton;
            modTurnoutNum++;
            Turnout t = InstanceManager.turnoutManagerInstance()
                    .getBySystemName(prefix + modTurnoutNum);
            if (t != null && t.getDecoderName() != null) {
                if (CVP_1Bname.equals(t.getDecoderName())) {
                    // do nothing button already = oneButton
                } else if (CVP_2Bname.equals(t.getDecoderName())) {
                    button = twoButton;
                } else {
                    log.warn("Turnout " + modTurnoutNum
                            + ", all CVP turnouts on one decoder should be "
                            + CVP_1Bname + " or " + CVP_2Bname);
                }
                // zero out the bits if the turnout is locked
                if (t.getLocked(Turnout.PUSHBUTTONLOCKOUT)) {
                    button = 0;
                }
            }
            CVdata = CVdata + button;
            oneButton = oneButton << 2; // move to the next turnout
            twoButton = twoButton << 2;

        }
        return CVdata;
    }

    private final static Logger log = LoggerFactory.getLogger(PushbuttonPacket.class);
}
