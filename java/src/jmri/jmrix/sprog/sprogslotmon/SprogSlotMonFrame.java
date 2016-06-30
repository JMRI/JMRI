package jmri.jmrix.sprog.sprogslotmon;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import jmri.jmrix.sprog.SprogCommandStation;
import jmri.jmrix.sprog.SprogConstants;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.util.JTableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a command station slot manager.
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001 
 * @author  Andrew Crosland (C) 2006 ported to SPROG 2008 Use JmriJframe
 */
public class SprogSlotMonFrame extends jmri.util.JmriJFrame {

    /**
     * Controls whether not-in-use slots are shown
     */
    javax.swing.JCheckBox showAllCheckBox = new javax.swing.JCheckBox();

    JButton estopAllButton = new JButton("estop all");
    SprogSlotMonDataModel slotModel = null;

    JTable slotTable;
    JScrollPane slotScroll;

    JTextArea status = new JTextArea("Track Current: ---A");

    SprogSystemConnectionMemo _memo = null;

    public SprogSlotMonFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
        slotModel = new SprogSlotMonDataModel(SprogConstants.MAX_SLOTS, 8,_memo);

        slotTable = JTableUtil.sortableDataModel(slotModel);
        slotScroll = new JScrollPane(slotTable);

        // configure items for GUI
        showAllCheckBox.setText("Show unused slots");
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(true);
        showAllCheckBox.setToolTipText("if checked, even empty/idle slots will appear");

        slotModel.configureTable(slotTable);

        // add listener object so checkboxes function
        showAllCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slotModel.showAllSlots(showAllCheckBox.isSelected());
                slotModel.fireTableDataChanged();
            }
        });

        // add listener object so stop all button functions
        estopAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("Estop all button pressed");
                _memo.getCommandStation().estopAll();
            }
        });

        estopAllButton.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                _memo.getCommandStation().estopAll();
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
            }
        });

        // adjust model to default settings
        slotModel.showAllSlots(showAllCheckBox.isSelected());

        // general GUI config
        setTitle("SPROG Slot Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(showAllCheckBox);
        pane1.add(estopAllButton);
        pane1.add(status);

        getContentPane().add(pane1);
        getContentPane().add(slotScroll);

        pack();
        pane1.setMaximumSize(pane1.getSize());
        pack();

    }

    public void initComponents() {
        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.sprogslotmon.SprogSlotMonFrame", true);
    }

    /**
     * method to find the existing SprogSlotMonFrame object
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public final SprogSlotMonFrame instance() {
        return null;
    }

    public void update() {
        slotModel.fireTableDataChanged();
    }

    public void updateStatus(String a) {
        status.setText("Track Current: " + a + " A");
    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown) {
            return;
        }

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    public void dispose() {
        slotModel.dispose();
        slotModel = null;
        slotTable = null;
        slotScroll = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogSlotMonFrame.class.getName());

}
