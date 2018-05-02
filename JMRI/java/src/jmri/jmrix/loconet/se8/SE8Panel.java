package jmri.jmrix.loconet.se8;

import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display and modify an SE8c configuration.
 * <P>
 * The read and write require a sequence of operations, which we handle with a
 * superclass.
 * <P>
 * Programming of the SE8c is done via configuration messages, so the SE8c
 * should not be put into programming mode via the built-in pushbutton while
 * this tool is in use.
 * <P>
 * Throughout, the terminology is "closed" == true, "thrown" == false. Variables
 * are named for their closed state.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007, 2010
 */
public class SE8Panel extends jmri.jmrix.loconet.AbstractBoardProgPanel {

    public SE8Panel() {
        this(1);
    }

    public SE8Panel(int boardNum) {
        super(boardNum);

        appendLine(provideAddressing("SE8C"));  // add read/write buttons, address // NOI18N

        JPanel panel2;
        appendLine(fullmode);
        appendLine(twoaspects);
        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(new JLabel("Cables 1-4 are "));
        panel2.add(section1to4mode);
        appendLine(panel2);
        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(new JLabel("Cables 5-8 are "));
        panel2.add(section5to8mode);
        appendLine(panel2);
        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(new JLabel("4th aspect is "));
        panel2.add(fourthAspect);
        appendLine(panel2);
        appendLine(semaphore);
        appendLine(pulsed);
        appendLine(disableDS);
        appendLine(fromloconet);
        appendLine(disablelocal);
        appendLine(sigaddress);
        appendLine(bcastaddress);
        appendLine(semaddress);
        appendLine(setdefault);
        appendLine(exercise);

        appendLine(provideStatusLine());
        setStatus("The SE8C should be in normal mode (Don't push the buttons on the SE8C!)");

        setTypeWord(0x72);  // configure SE8 message type
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.se8.SE8Frame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSE8cProgrammer"));
    }

    /**
     * Copy from the GUI to the opsw array.
     * <p>
     * Used before write operations start
     */
    @Override
    protected void copyToOpsw() {
        opsw[1] = fullmode.isSelected();
        opsw[2] = twoaspects.isSelected();

        opsw[11] = semaphore.isSelected();
        opsw[12] = pulsed.isSelected();
        opsw[13] = disableDS.isSelected();
        opsw[14] = fromloconet.isSelected();
        opsw[15] = disablelocal.isSelected();
        opsw[17] = sigaddress.isSelected();
        opsw[18] = bcastaddress.isSelected();
        opsw[19] = semaddress.isSelected();
        opsw[20] = setdefault.isSelected();
        opsw[21] = exercise.isSelected();

        // value based
        int value = section1to4mode.getSelectedIndex();
        if ((value & 0x01) != 0) {
            opsw[5] = true;
        } else {
            opsw[5] = false;
        }
        if ((value & 0x02) != 0) {
            opsw[4] = true;
        } else {
            opsw[4] = false;
        }
        if ((value & 0x04) != 0) {
            opsw[3] = true;
        } else {
            opsw[3] = false;
        }

        value = section5to8mode.getSelectedIndex();
        if ((value & 0x01) != 0) {
            opsw[8] = true;
        } else {
            opsw[8] = false;
        }
        if ((value & 0x02) != 0) {
            opsw[7] = true;
        } else {
            opsw[7] = false;
        }
        if ((value & 0x04) != 0) {
            opsw[6] = true;
        } else {
            opsw[6] = false;
        }

        value = fourthAspect.getSelectedIndex();
        if ((value & 0x01) != 0) {
            opsw[10] = true;
        } else {
            opsw[10] = false;
        }
        if ((value & 0x02) != 0) {
            opsw[9] = true;
        } else {
            opsw[9] = false;
        }
    }

    @Override
    protected void updateDisplay() {
        // binary switches
        fullmode.setSelected(opsw[1]);
        twoaspects.setSelected(opsw[2]);
        semaphore.setSelected(opsw[11]);
        pulsed.setSelected(opsw[12]);
        disableDS.setSelected(opsw[13]);
        fromloconet.setSelected(opsw[14]);
        disablelocal.setSelected(opsw[15]);
        sigaddress.setSelected(opsw[17]);
        bcastaddress.setSelected(opsw[18]);
        semaddress.setSelected(opsw[19]);
        setdefault.setSelected(opsw[20]);
        exercise.setSelected(opsw[21]);

        // value-based switches
        int value = 0;
        if (opsw[5]) {
            value += 1;
        }
        if (opsw[4]) {
            value += 2;
        }
        if (opsw[3]) {
            value += 4;
        }
        section1to4mode.setSelectedIndex(value);

        value = 0;
        if (opsw[8]) {
            value += 1;
        }
        if (opsw[7]) {
            value += 2;
        }
        if (opsw[6]) {
            value += 4;
        }
        section5to8mode.setSelectedIndex(value);

        value = 0;
        if (opsw[10]) {
            value += 1;
        }
        if (opsw[9]) {
            value += 2;
        }
        fourthAspect.setSelectedIndex(value);

    }

    @Override
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
                return 7;
            case 7:
                return 8;
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
                return 0;   // done!
            default:
                log.error("unexpected state " + state);
                return 0;
        }
    }

    JCheckBox fullmode = new JCheckBox("Reserved (OpSw 1)");  // opsw 01
    JCheckBox twoaspects = new JCheckBox("Two aspects (one turnout address) per head");  // opsw 02
    JComboBox<String> section1to4mode = new JComboBox<String>(new String[]{
        "3 LEDs common anode", "3 LEDs common cathode",
        "3-wire searchlight common anode", "3-wire searchlight common cathode",
        "Reserved (4)", "Reserved (5)",
        "2-wire searchlight common anode", "2-wire searchlight common cathode"
    });  // opsw 3, 4, 5
    JComboBox<String> section5to8mode = new JComboBox<String>(new String[]{
        "3 LEDs common anode", "3 LEDs common cathode",
        "3-wire searchlight common anode", "3-wire searchlight common cathode",
        "Reserved (4)", "Reserved (5)",
        "2-wire searchlight common anode", "2-wire searchlight common cathode"
    });  // opsw 6, 7, 8
    JComboBox<String> fourthAspect = new JComboBox<String>(new String[]{
        "flashing yellow", "flashing red",
        "dark", "flashing green"
    });  // opsw 9, 10
    JCheckBox semaphore = new JCheckBox("Semaphore mode");  // opsw 11
    JCheckBox pulsed = new JCheckBox("Pulsed switch outputs");  // opsw 12
    JCheckBox disableDS = new JCheckBox("Disable DS input");  // opsw 13
    JCheckBox fromloconet = new JCheckBox("Enable switch command from loconet");  // opsw 14
    JCheckBox disablelocal = new JCheckBox("Disable local switch control");  // opsw 15
    JCheckBox sigaddress = new JCheckBox("Next switch command sets signal address");  // opsw 17
    JCheckBox bcastaddress = new JCheckBox("Next switch command sets broadcast address");  // opsw 18
    JCheckBox semaddress = new JCheckBox("Next switch command sets semaphore address");  // opsw 19
    JCheckBox setdefault = new JCheckBox("Restore factory default, including address");  // opsw 20
    JCheckBox exercise = new JCheckBox("Show LED exercise pattern");  // opsw 21

    private final static Logger log = LoggerFactory.getLogger(SE8Panel.class);

}
