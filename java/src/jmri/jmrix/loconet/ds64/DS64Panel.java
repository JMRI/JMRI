// DS64Panel.java
package jmri.jmrix.loconet.ds64;

import javax.swing.JCheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying and programming a DS64 configuration.
 * <P>
 * The read and write require a sequence of operations, which we handle with a
 * state variable.
 * <P>
 * Programming of the BDL16 is done via configuration messages, so the BDL16
 * should not be put into programming mode via the built-in pushbutton while
 * this tool is in use.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2004, 2005, 2007, 2010
 * @version	$Revision$
 */
public class DS64Panel extends jmri.jmrix.loconet.AbstractBoardProgPanel {

    /**
     *
     */
    private static final long serialVersionUID = -8039300267592456122L;

    public DS64Panel() {
        this(1);
    }

    public DS64Panel(int boardNum) {
        super(boardNum);

        appendLine(provideAddressing("DS64"));  // add read/write buttons, address

        appendLine(opsw1);
        appendLine(opsw2);
        appendLine(opsw3);
        appendLine(opsw4);
        appendLine(opsw5);
        appendLine(opsw6);
        appendLine(opsw7);
        appendLine(opsw8);
        appendLine(opsw9);
        appendLine(opsw10);
        appendLine(opsw11);
        appendLine(opsw12);
        appendLine(opsw13);
        appendLine(opsw14);
        appendLine(opsw15);
        appendLine(opsw16);
        appendLine(opsw17);
        appendLine(opsw18);
        appendLine(opsw19);
        appendLine(opsw20);
        appendLine(opsw21);

        appendLine(provideStatusLine());
        setStatus("The DS64 should be in normal mode (Don't push the buttons on the DS64!)");
        // add status
        appendLine(provideStatusLine());

        setTypeWord(0x73);  // configure DS64 message type
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.ds64.DS64Frame";
    }

    public String getTitle() {
        return getTitle(jmri.jmrix.loconet.LocoNetBundle.bundle().getString("MenuItemDS64Programmer"));
    }

    /**
     * Copy from the GUI to the opsw array.
     * <p>
     * Used before write operations start
     */
    protected void copyToOpsw() {
        // copy over the display
        opsw[1] = opsw1.isSelected();
        opsw[2] = opsw2.isSelected();
        opsw[3] = opsw3.isSelected();
        opsw[4] = opsw4.isSelected();
        opsw[5] = opsw5.isSelected();
        opsw[6] = opsw6.isSelected();
        opsw[7] = opsw7.isSelected();
        opsw[8] = opsw8.isSelected();
        opsw[9] = opsw9.isSelected();
        opsw[10] = opsw10.isSelected();
        opsw[11] = opsw11.isSelected();
        opsw[12] = opsw12.isSelected();
        opsw[13] = opsw13.isSelected();
        opsw[14] = opsw14.isSelected();
        opsw[15] = opsw15.isSelected();
        opsw[16] = opsw16.isSelected();
        opsw[17] = opsw17.isSelected();
        opsw[18] = opsw18.isSelected();
        opsw[19] = opsw19.isSelected();
        opsw[20] = opsw20.isSelected();
        opsw[21] = opsw21.isSelected();

    }

    protected void updateDisplay() {
        opsw1.setSelected(opsw[1]);
        opsw2.setSelected(opsw[2]);
        opsw3.setSelected(opsw[3]);
        opsw4.setSelected(opsw[4]);
        opsw5.setSelected(opsw[5]);
        opsw6.setSelected(opsw[6]);
        opsw7.setSelected(opsw[7]);
        opsw8.setSelected(opsw[8]);
        opsw9.setSelected(opsw[9]);
        opsw10.setSelected(opsw[10]);
        opsw11.setSelected(opsw[11]);
        opsw12.setSelected(opsw[12]);
        opsw13.setSelected(opsw[13]);
        opsw14.setSelected(opsw[14]);
        opsw15.setSelected(opsw[15]);
        opsw16.setSelected(opsw[16]);
        opsw17.setSelected(opsw[17]);
        opsw18.setSelected(opsw[18]);
        opsw19.setSelected(opsw[19]);
        opsw20.setSelected(opsw[20]);
        opsw21.setSelected(opsw[21]);
    }

    protected int nextState(int state) {
        switch (state) {
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            case 6:
                return 8;   // 7 has to be done last, as it's reset
            case 8:
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
                return 14;
            case 14:
                return 15;
            case 15:
                return 16;
            case 16:
                return 17;
            case 17:
                return 18;
            case 18:
                return 19;
            case 19:
                return 20;
            case 20:
                return 21;
            case 21:
                return 7;
            case 7:
                return 0;       // done!
            default:
                log.error("unexpected state " + state);
                return 0;
        }
    }

    JCheckBox opsw1 = new JCheckBox("OpSw 01: Static Output Type (Pulse if off)");
    JCheckBox opsw2 = new JCheckBox("OpSw 02: Pulse Timeout  200ms");
    JCheckBox opsw3 = new JCheckBox("OpSw 03: Pulse Timeout  400ms");
    JCheckBox opsw4 = new JCheckBox("OpSw 04: Pulse Timeout  800ms");
    JCheckBox opsw5 = new JCheckBox("OpSw 05: Pulse Timeout 1600ms");
    JCheckBox opsw6 = new JCheckBox("OpSw 06: Output Power Management - Wait for 1st command");
    JCheckBox opsw7 = new JCheckBox("OpSw 07: Reset Functions to Factory Default");
    JCheckBox opsw8 = new JCheckBox("OpSw 08: Double normal startup delay");
    JCheckBox opsw9 = new JCheckBox("OpSw 09: Turn off static outputs after 16sec");
    JCheckBox opsw10 = new JCheckBox("OpSw 10: DS64 accepts computer commands only");
    JCheckBox opsw11 = new JCheckBox("OpSw 11: Routes work from input lines");
    JCheckBox opsw12 = new JCheckBox("OpSw 12: Either input high causes toggle");
    JCheckBox opsw13 = new JCheckBox("OpSw 13: All eight inputs send sensor messages");
    JCheckBox opsw14 = new JCheckBox("OpSw 14: Switch commands from track only");
    JCheckBox opsw15 = new JCheckBox("OpSw 15: Outputs ignore inputs");
    JCheckBox opsw16 = new JCheckBox("OpSw 16: Disable routes");
    JCheckBox opsw17 = new JCheckBox("OpSw 17: Output 1 is crossing gate");
    JCheckBox opsw18 = new JCheckBox("OpSw 18: Output 2 is crossing gate");
    JCheckBox opsw19 = new JCheckBox("OpSw 19: Output 3 is crossing gate");
    JCheckBox opsw20 = new JCheckBox("OpSw 20: Output 4 is crossing gate");
    JCheckBox opsw21 = new JCheckBox("OpSw 21: Send turnout sensor messages (general sensor messages if off)");

    private final static Logger log = LoggerFactory.getLogger(DS64Panel.class.getName());

}
