// BDL16Panel.java
package jmri.jmrix.loconet.bdl16;

import javax.swing.JCheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying and programming a BDL16x configuration.
 * <P>
 * The read and write require a sequence of operations, which we handle with a
 * state variable.
 * <P>
 * Programming of the BDL16x is done via configuration messages, so the BDL16x
 * should not be put into programming mode via the built-in pushbutton while
 * this tool is in use.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2004, 2007, 2010
 * @version	$Revision$
 */
public class BDL16Panel extends jmri.jmrix.loconet.AbstractBoardProgPanel {

    /**
     *
     */
    private static final long serialVersionUID = -287798661310758892L;

    public BDL16Panel() {
        this(1);
    }

    public BDL16Panel(int boardNum) {
        super(boardNum);
        appendLine(provideAddressing("BDL16x"));  // add read/write buttons, address

        appendLine(commonrail);
        appendLine(polarity);
        appendLine(transpond);
        appendLine(rx4connected1);
        appendLine(rx4connected2);
        appendLine(forceoccupied);
        appendLine(section16qualpower);
        appendLine(nomaster);
        appendLine(noterminate);
        appendLine(delayhalfsecond);
        appendLine(highthreshold);
        appendLine(drivefromswitch);
        appendLine(decodefromloconet);
        appendLine(reserved36);
        appendLine(longdelay);
        appendLine(extralongdelay);
        appendLine(transpondtrack);
        appendLine(poweronmsg);
        appendLine(antichatfilt);
        appendLine(antichatsens);
        appendLine(setdefault);

        appendLine(provideStatusLine());
        setStatus("The BDL16x should be in its normal mode. (Do not push any of the buttons on the BDL16x!)");

        setTypeWord(0x71);  // configure BDL16x message type
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.bdl16.BDL16Frame";
    }

    public String getTitle() {
        return getTitle(jmri.jmrix.loconet.LocoNetBundle.bundle().getString("MenuItemBDL16Programmer"));
    }

    /**
     * Copy from the GUI to the opsw array.
     * <p>
     * Used before write operations start
     */
    protected void copyToOpsw() {
        // copy over the display
        opsw[1] = commonrail.isSelected();
        opsw[3] = polarity.isSelected();
        opsw[5] = transpond.isSelected();
        opsw[6] = rx4connected1.isSelected();
        opsw[7] = rx4connected2.isSelected();
        opsw[9] = forceoccupied.isSelected();
        opsw[10] = section16qualpower.isSelected();
        opsw[11] = nomaster.isSelected();
        opsw[12] = noterminate.isSelected();
        opsw[13] = delayhalfsecond.isSelected();
        opsw[19] = highthreshold.isSelected();
        opsw[25] = drivefromswitch.isSelected();
        opsw[26] = decodefromloconet.isSelected();
        opsw[36] = reserved36.isSelected();
        opsw[37] = longdelay.isSelected();
        opsw[38] = extralongdelay.isSelected();
        opsw[39] = transpondtrack.isSelected();
        opsw[42] = poweronmsg.isSelected();
        opsw[43] = antichatfilt.isSelected();
        opsw[44] = antichatsens.isSelected();
        opsw[40] = setdefault.isSelected();

    }

    protected void updateDisplay() {
        commonrail.setSelected(opsw[1]);
        polarity.setSelected(opsw[3]);
        transpond.setSelected(opsw[5]);
        rx4connected1.setSelected(opsw[6]);
        rx4connected2.setSelected(opsw[7]);
        forceoccupied.setSelected(opsw[9]);
        section16qualpower.setSelected(opsw[10]);
        nomaster.setSelected(opsw[11]);
        noterminate.setSelected(opsw[12]);
        delayhalfsecond.setSelected(opsw[13]);
        highthreshold.setSelected(opsw[19]);
        drivefromswitch.setSelected(opsw[25]);
        decodefromloconet.setSelected(opsw[26]);
        reserved36.setSelected(opsw[36]);
        longdelay.setSelected(opsw[37]);
        extralongdelay.setSelected(opsw[38]);
        transpondtrack.setSelected(opsw[39]);
        poweronmsg.setSelected(opsw[42]);
        antichatfilt.setSelected(opsw[43]);
        antichatsens.setSelected(opsw[44]);
        setdefault.setSelected(opsw[40]);
    }

    protected int nextState(int state) {
        switch (state) {
            case 1:
                return 3;
            case 3:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            case 7:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 11:
                return 12;
            case 12:
                return 13;
            case 13:
                return 19;
            case 19:
                return 25;
            case 25:
                return 26;
            case 26:
                return 36;
            case 36:
                return 37;
            case 37:
                return 38;
            case 38:
                return 39;
            case 39:
                return 42;
            case 42:
                return 43;
            case 43:
                return 44;
            case 44:
                return 40;    // have to do 40 last
            case 40:
                return 0;    // done!
            default:
                log.error("unexpected state " + state);
                return 0;
        }
    }

    JCheckBox commonrail = new JCheckBox("OpSw 01: Common rail wiring");  // opsw 01
    JCheckBox polarity = new JCheckBox("OpSw 03: Reverse polarity for detection"); // opsw 03
    JCheckBox transpond = new JCheckBox("OpSw 05: Enable transponding"); // opsw 05
    JCheckBox rx4connected1 = new JCheckBox("OpSw 06: Reserved (Unset if RX4 is connected)"); // opsw 06
    JCheckBox rx4connected2 = new JCheckBox("OpSw 07: Reserved (Unset if RX4 is connected)"); // opsw 07
    JCheckBox forceoccupied = new JCheckBox("OpSw 09: Show unoccupied when power off");  // opsw 09
    JCheckBox section16qualpower = new JCheckBox("OpSw 10: Section 16 used to sense power");  // opsw 10
    JCheckBox nomaster = new JCheckBox("OpSw 11: Do not allow BDL16x to be LocoNet master");  // opsw 11
    JCheckBox noterminate = new JCheckBox("OpSw 12: Do not allow BDL16x to terminate LocoNet");  // opsw 12
    JCheckBox delayhalfsecond = new JCheckBox("OpSw 13: Delay only 1/2 second at power up");  // opsw 13
    JCheckBox highthreshold = new JCheckBox("OpSw 19: High threshold sense (10kohms)");  // opsw 19
    JCheckBox drivefromswitch = new JCheckBox("OpSw 25: Drive LEDs from switch commands, not occupancy");  // opsw 25
    JCheckBox decodefromloconet = new JCheckBox("OpSw 26: Decode switch commands from LocoNet");  // opsw 26
    JCheckBox reserved36 = new JCheckBox("OpSw 36: Ignore GPON messages, only reply to interrogate");  // opsw 36
    JCheckBox longdelay = new JCheckBox("OpSw 37: Long detection delay (BDL168 only)");  // opsw 37
    JCheckBox extralongdelay = new JCheckBox("OpSw 38: Extra long detection delay (BDL168 only)");  // opsw 38
    JCheckBox transpondtrack = new JCheckBox("OpSw 39: Transponder Tracking (BDL168 only)");  // opsw 39
    JCheckBox poweronmsg = new JCheckBox("OpSw 42: Turn off power-on interogate (BDL168 only)");  // opsw 43
    JCheckBox antichatfilt = new JCheckBox("OpSw 43: Anti-chatter filtering (BDL168 only)");  // opsw 43
    JCheckBox antichatsens = new JCheckBox("OpSw 44: Anti-chatter filter sensitivity (BDL168 only)");  // opsw 44
    JCheckBox setdefault = new JCheckBox("OpSw 40: Restore factory default, including address");  // opsw 40

    private final static Logger log = LoggerFactory.getLogger(BDL16Panel.class.getName());

}
