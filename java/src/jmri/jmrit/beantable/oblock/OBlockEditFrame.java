package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

import javax.swing.*;
//import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to edit OBlock objects in tabbed table interface.
 * Adapted from AbstractAudioFrame + -ListenerFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse copyright (c) 2020
 */
public class OBlockEditFrame extends JmriJFrame {

    OBlockEditFrame frame = this;
    OBlockManager obm;

    JPanel main = new JPanel();
    private final JScrollPane scroll
            = new JScrollPane(main,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    OBlockTableModel model;

    // Common UI components for Add/Edit OBlock
    JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameOBlock")));
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(15);
    JButton addButton;
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);

    private final static String PREFIX = "OB";

    /**
     * Standard constructor
     *
     * @param title Title of this OBlockFrame
     * @param model OBlockTableModel holding OBlock data
     */
    public OBlockEditFrame(String title, OBlockTableModel model) {
        super(title);
        this.model = model;
        obm = InstanceManager.getDefault(OBlockManager.class);
    }

    /**
     * Layout the frame.
     */
    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(sysNameLabel);
        p.add(sysName);
        frame.getContentPane().add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(userNameLabel);
        p.add(userName);
        frame.getContentPane().add(p);

        p = new JPanel();
        JButton apply;
        p.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener(this::applyPressed);
        JButton ok;
        p.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
            frame.dispose();
        });
        JButton cancel;
        p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        frame.getContentPane().add(p);

        frame.add(scroll);

    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        sysName.setText(null);
        userName.setText(null);

        //this.newOBlock = true;
    }

    /**
     * Populate the OBlock frame with current values.
     *
     * @param a OBlock object to use
     */
    public void populateFrame(OBlock a) {
        sysName.setText(a.getSystemName());
        userName.setText(a.getUserName());
//        tempRow[LENGTHCOL] = twoDigit.format(0.0);
//        tempRow[UNITSCOL] = Bundle.getMessage("in");
//        tempRow[CURVECOL] = noneText;
//        tempRow[REPORT_CURRENTCOL] = Bundle.getMessage("Current");
//        tempRow[PERMISSIONCOL] = Bundle.getMessage("Permissive");
//        tempRow[SPEEDCOL] = "";
    }

    private void applyPressed(ActionEvent e) {
        String user = userName.getText().trim();
        if (user.equals("")) {
            user = null;
        }
        OBlock ob = obm.getOBlock(user);
        if (ob != null) {
//            //      try {
//            OBlock block = fromBlockComboBox.getItemAt(fromBlockComboBox.getSelectedIndex());
//            if (block != null) {
//                portal.setFromBlock(block, true);
//            }
//            block = toBlockComboBox.getItemAt(toBlockComboBox.getSelectedIndex());
//            if (block != null) {
//                ob.setToBlock(block, true);
//                //  } catch (IllegalArgumentException ex) {
//                //     JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PortalCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
//            }
        }
        // Notify changes
        model.fireTableDataChanged();
    }

    /**
     * Check System Name user input.
     *
     * @param entry string retrieved from text field
     * @param counter index of all similar (OBlock) items
     * @param prefix (Oblock/Portal/path/signal) system name prefix string to compare entry against
     * @return true if prefix doesn't match
     */
    protected boolean entryError(String entry, String prefix, String counter) {
        if (!entry.startsWith(prefix)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("OBlockCreateError", prefix),
                    Bundle.getMessage("OBlockCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            sysName.setText(prefix + counter);
            return true;
        }
        return false;
    }

    //private static final Logger log = LoggerFactory.getLogger(OBlockEditFrame.class);

}
