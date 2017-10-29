package jmri.jmrix.loconet.slotmon;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Frame providing a command station slot manager.
 * <P>
 * Slots 102 through 127 are normally not used for loco control, so are shown
 * separately.
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class SlotMonPane extends jmri.jmrix.loconet.swing.LnPanel {

    /**
     * Controls whether not-in-use slots are shown
     */
    javax.swing.JCheckBox showAllCheckBox = new javax.swing.JCheckBox();
    /**
     * Controls whether system slots (120-127) are shown
     */
    javax.swing.JCheckBox showSystemCheckBox = new javax.swing.JCheckBox();

    JButton estopAllButton = new JButton(Bundle.getMessage("ButtonSlotMonEStopAll"));

    //Added by Jeffrey Machacek 2013
    JButton clearAllButton = new JButton(Bundle.getMessage("ButtonSlotMonClearAll"));
    SlotMonDataModel slotModel;
    JTable slotTable;
    JScrollPane slotScroll;

    public SlotMonPane() {
        super();
    }

    @Override
    public void initComponents(jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        slotModel = new SlotMonDataModel(128, 16, memo);
        slotTable = new JTable(slotModel);
        slotTable.setRowSorter(new TableRowSorter<>(slotModel));
        slotScroll = new JScrollPane(slotTable);

        // configure items for GUI
        showAllCheckBox.setText(Bundle.getMessage("TextSlotMonShowUnused"));
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(false);
        showAllCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowUnused"));

        showSystemCheckBox.setText(Bundle.getMessage("TextSlotMonShowSystem"));
        showSystemCheckBox.setVisible(true);
        showSystemCheckBox.setSelected(false);
        showSystemCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowSystem"));

        slotModel.configureTable(slotTable);

        // add listener object so checkboxes function
        showAllCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.showAllSlots(showAllCheckBox.isSelected());
                slotModel.fireTableDataChanged();
            }
        });
        showSystemCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.showSystemSlots(showSystemCheckBox.isSelected());
                slotModel.fireTableDataChanged();
            }
        });

        // add listener object so stop all button functions
        estopAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.estopAll();
            }
        });
        estopAllButton.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                slotModel.estopAll();
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });

        //Jeffrey 6/29/2013
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.clearAllSlots();
            }
        });

        // adjust model to default settings
        slotModel.showAllSlots(showAllCheckBox.isSelected());
        slotModel.showSystemSlots(showSystemCheckBox.isSelected());

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(showAllCheckBox);
        pane1.add(showSystemCheckBox);
        pane1.add(estopAllButton);
        pane1.add(clearAllButton);

        add(pane1);
        add(slotScroll);

        // set scroll size
        //pane1.setMaximumSize(new java.awt.Dimension(100,300));
        if (pane1.getMaximumSize().height > 0 && pane1.getMaximumSize().width > 0) {
            pane1.setMaximumSize(pane1.getPreferredSize());
        }
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.slotmon.SlotMonFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSlotMonitor"));
    }

    @Override
    public void dispose() {
        slotModel.dispose();
        slotModel = null;
        slotTable = null;
        slotScroll = null;
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemSlotMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    SlotMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

}
