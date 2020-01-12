package jmri.jmrix.loconet.pm4;

import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrix.loconet.AbstractBoardProgPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying and programming a PM4 configuration.
 * <p>
 * The read and write require a sequence of operations, which we handle with a
 * state variable.
 * <p>
 * Programming of the PM4 is done via configuration messages, so the PM4 should
 * not be put into programming mode via the built-in pushbutton while this tool
 * is in use.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2004, 2007, 2010
 */
public class PM4Panel extends AbstractBoardProgPanel {

    public PM4Panel() {
        this(1);
    }

    public PM4Panel(int boardNum) {
        super(boardNum, false, "PM4x");

        appendLine(provideAddressing());  // add read/write buttons, address // NOI18N

        JPanel panec = new JPanel();
        panec.setLayout(new FlowLayout());
        panec.add(new JLabel(Bundle.getMessage("CurrentLabel")));
        panec.add(current);
        current.setSelectedIndex(1);
        appendLine(panec);

        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(Bundle.getMessage("SectionXSpeedLabel", 1)));
        pane1.add(slow1);
        //pane1.add(new JLabel(Bundle.getMessage("AutoReverseLabel")));
        pane1.add(rev1);
        appendLine(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel(Bundle.getMessage("SectionXSpeedLabel", 2)));
        pane2.add(slow2);
        //pane2.add(new JLabel(Bundle.getMessage("AutoReverseLabel")));
        pane2.add(rev2);
        appendLine(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel(Bundle.getMessage("SectionXSpeedLabel", 3)));
        pane3.add(slow3);
        //pane3.add(new JLabel(Bundle.getMessage("AutoReverseLabel")));
        pane3.add(rev3);
        appendLine(pane3);

        JPanel pane4 = new JPanel();
        pane4.setLayout(new FlowLayout());
        pane4.add(new JLabel(Bundle.getMessage("SectionXSpeedLabel", 4)));
        pane4.add(slow4);
        //pane4.add(new JLabel(Bundle.getMessage("AutoReverseLabel")));
        pane4.add(rev4);
        appendLine(pane4);

        appendLine(provideStatusLine());

        slow1.setSelectedIndex(1);
        slow2.setSelectedIndex(1);
        slow3.setSelectedIndex(1);
        slow4.setSelectedIndex(1);

        // add status
        appendLine(provideStatusLine());
        setStatus(Bundle.getMessage("Status1"));

        setTypeWord(0x70); // configure PM4 message type

        panelToScroll();

    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.pm4.PM4Frame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemPM4Programmer"));
    }

    void setSpeedFromDisplay(int offset, JComboBox<String> box) {
        switch (box.getSelectedIndex()) {
            case 0:
                opsw[offset] = false;
                opsw[offset + 2] = true;      // true is closed
                return;
            default:
            case 1:
                opsw[offset] = false;
                opsw[offset + 2] = false;
                return;
            case 2:
                opsw[offset] = true;
                opsw[offset + 2] = true;
                return;
            case 3:
                opsw[offset] = true;
                opsw[offset + 2] = false;
                return;
        }
    }

    /**
     * Copy from the GUI to the opsw array.
     * <p>
     * Used before write operations start
     */
    @Override
    protected void copyToOpsw() {
        // copy over the display
        setSpeedFromDisplay(3, slow1);
        opsw[6] = rev1.isSelected();
        setSpeedFromDisplay(11, slow2);
        opsw[14] = rev2.isSelected();
        setSpeedFromDisplay(19, slow3);
        opsw[22] = rev3.isSelected();
        setSpeedFromDisplay(27, slow4);
        opsw[30] = rev4.isSelected();
        // get current limit
        // bit 9 is the low bit, but "closed" is a 0 in the calculation
        // bit 1 is the middle bit, closed is a 1
        // bit 2 is the MSB, closed is a 1
        int index = current.getSelectedIndex();
        opsw[2] = ((index & 0x04) != 0) ? true : false;
        opsw[1] = ((index & 0x02) != 0) ? true : false;
        opsw[9] = ((index & 0x01) != 0) ? false : true;

    }

    void setDisplaySpeed(int offset, JComboBox<String> box) {
        int index = 0;
        if (!opsw[offset + 2]) {
            index++;
        }
        if (opsw[offset]) {
            index += 2;
        }
        box.setSelectedIndex(index);
    }

    @Override
    protected void updateDisplay() {
        setDisplaySpeed(3, slow1);
        rev1.setSelected(opsw[6]);
        setDisplaySpeed(11, slow2);
        rev2.setSelected(opsw[14]);
        setDisplaySpeed(19, slow3);
        rev3.setSelected(opsw[22]);
        setDisplaySpeed(27, slow4);
        rev4.setSelected(opsw[30]);
        // display current limit
        // bit 9 is the low bit, but "closed" is a 0 in the calculation
        // bit 1 is the middle bit, closed is a 1
        // bit 2 is the MSB, closed is a 1
        int index = 0;
        if (opsw[2]) {
            index += 4;
        }
        if (opsw[1]) {
            index += 2;
        }
        if (!opsw[9]) {
            index += 1;
        }
        current.setSelectedIndex(index);
    }

    @Override
    protected int nextState(int state) {
        switch (state) {
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 5;
            case 5:
                return 6;
            case 6:
                return 9;
            case 9:
                return 11;
            case 11:
                return 13;
            case 13:
                return 14;
            case 14:
                return 19;
            case 19:
                return 21;
            case 21:
                return 22;
            case 22:
                return 27;
            case 27:
                return 29;
            case 29:
                return 30;
            case 30:
                return 0;   // done!
            default:
                log.error("unexpected state " + state); // NOI18N
                return 0;
        }
    }

    JComboBox<String> current = new JComboBox<String>(new String[]{
            Bundle.getMessage("CurrentXBox", "1.5"), Bundle.getMessage("CurrentXBox", "3"),
            Bundle.getMessage("CurrentXBox", "4.5"), Bundle.getMessage("CurrentXBox", "6"),
            Bundle.getMessage("CurrentXBox", "7.5"), Bundle.getMessage("CurrentXBox", "9"),
            Bundle.getMessage("CurrentXBox", "10.5"), Bundle.getMessage("CurrentXBox", "12")
    });

    JComboBox<String> slow1 = new JComboBox<String>(new String[]{
            Bundle.getMessage("Box1a"), Bundle.getMessage("Box1b"),
            Bundle.getMessage("Box1c"), Bundle.getMessage("Box1d")});
    JCheckBox rev1 = new JCheckBox(Bundle.getMessage("AutoReverseBox"));
    JComboBox<String> slow2 = new JComboBox<String>(new String[]{
            Bundle.getMessage("Box1a"), Bundle.getMessage("Box1b"),
            Bundle.getMessage("Box1c"), Bundle.getMessage("Box1d")});
    JCheckBox rev2 = new JCheckBox(Bundle.getMessage("AutoReverseBox"));
    JComboBox<String> slow3 = new JComboBox<String>(new String[]{
            Bundle.getMessage("Box1a"), Bundle.getMessage("Box1b"),
            Bundle.getMessage("Box1c"), Bundle.getMessage("Box1d")});
    JCheckBox rev3 = new JCheckBox(Bundle.getMessage("AutoReverseBox"));
    JComboBox<String> slow4 = new JComboBox<String>(new String[]{
            Bundle.getMessage("Box1a"), Bundle.getMessage("Box1b"),
            Bundle.getMessage("Box1c"), Bundle.getMessage("Box1d")});
    JCheckBox rev4 = new JCheckBox(Bundle.getMessage("AutoReverseBox"));

    private final static Logger log = LoggerFactory.getLogger(PM4Panel.class);

}
