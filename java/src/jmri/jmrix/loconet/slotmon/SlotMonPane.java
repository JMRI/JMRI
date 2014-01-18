// SlotMonPane.java

package jmri.jmrix.loconet.slotmon;

import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.*;

import jmri.util.JTableUtil;
import jmri.jmrix.loconet.*;

/**
 * Frame provinging a command station slot manager.
 * <P>
 * Slots 102 through 127 are normally not used for loco control,
 * so are shown separately.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision$
 */
public class SlotMonPane extends jmri.jmrix.loconet.swing.LnPanel {

    /**
     * Controls whether not-in-use slots are shown
     */
    javax.swing.JCheckBox 	showAllCheckBox = new javax.swing.JCheckBox();
    /**
     * Controls whether system slots (120-127) are shown
     */
    javax.swing.JCheckBox 	showSystemCheckBox = new javax.swing.JCheckBox();

    JButton estopAllButton  = new JButton("Estop All");
    
    //Added by Jeffrey Machacek 2013
    JButton clearAllButton = new JButton("Clear All Non-InUse Slots");
    SlotMonDataModel slotModel;
    JTable slotTable;
    JScrollPane slotScroll;
    
    public SlotMonPane() {
        super();
    }

    public void initComponents(jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        slotModel 	= new SlotMonDataModel(128,16, memo);
    	slotTable	= JTableUtil.sortableDataModel(slotModel);
    	slotScroll	= new JScrollPane(slotTable);

        // configure items for GUI
        showAllCheckBox.setText("Show unused slots");
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(false);
        showAllCheckBox.setToolTipText("if checked, even empty/idle slots will appear");

        showSystemCheckBox.setText("Show system slots");
        showSystemCheckBox.setVisible(true);
        showSystemCheckBox.setSelected(false);
        showSystemCheckBox.setToolTipText("if checked, slots reserved for system use will be shown");

        slotModel.configureTable(slotTable);

        // add listener object so checkboxes function
        showAllCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            slotModel.showAllSlots(showAllCheckBox.isSelected());
            slotModel.fireTableDataChanged();
            }
        });
        showSystemCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            slotModel.showSystemSlots(showSystemCheckBox.isSelected());
            slotModel.fireTableDataChanged();
            }
        });

        // add listener object so stop all button functions
        estopAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slotModel.estopAll();
            }
        });
        estopAllButton.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                slotModel.estopAll();
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {}
        });
        
        //Jeffrey 6/29/2013
        clearAllButton.addActionListener(new ActionListener() {
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
       
        if (pane1.getMaximumSize().height > 0 && pane1.getMaximumSize().width > 0)
            pane1.setMaximumSize(pane1.getPreferredSize());
    }

    public String getHelpTarget() { return "package.jmri.jmrix.loconet.slotmon.SlotMonFrame"; }
    public String getTitle() { 
        return getTitle(LocoNetBundle.bundle().getString("MenuItemSlotMonitor")); 
    }
    
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
            super(LocoNetBundle.bundle().getString("MenuItemSlotMonitor"), 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                SlotMonPane.class.getName(), 
                jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }
    
}
