package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a GUI for editing OBlocks - Portal objects in the _tabbed OBlock Table interface.
 * Based on AudioSourceFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class PortalEditFrame extends JmriJFrame {

    //JPanel main = new JPanel();
    //private final JScrollPane scroll = new JScrollPane(main, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    PortalTableModel model;
    PortalManager pm;
    //private final Object lock = new Object();

    // UI components for Add/Edit Portal
    JLabel portalLabel = new JLabel(Bundle.getMessage("PortalNameLabel"), JLabel.TRAILING);
    JTextField portalUserName = new JTextField(15);
    JLabel fromBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromBlockName")), JLabel.TRAILING);
    JLabel toBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OppBlockName")), JLabel.TRAILING);
    private final NamedBeanComboBox<OBlock> fromBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<OBlock> toBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    JLabel statusBar = new JLabel(Bundle.getMessage("AddPortalStatusEnter"), JLabel.LEADING);

    private final PortalEditFrame frame = this;
    private Portal _portal;
    private boolean _newPortal = false;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public PortalEditFrame(@Nonnull String title, Portal portal, PortalTableModel model) {
        super(title);
        this.model = model;
        pm = InstanceManager.getDefault(PortalManager.class);
        layoutFrame();
        if (portal == null) {
            resetFrame();
            setTitle(Bundle.getMessage("TitleAddPortal"));
            _newPortal = true;
        } else {
            _portal = portal;
            populateFrame(portal);
        }
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setSize(300, 300);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        JPanel configGrid = new JPanel();
        GridLayout layout = new GridLayout(3, 2, 10, 0); // (int rows, int cols, int hgap, int vgap)
        configGrid.setLayout(layout);

        // row 1
        configGrid.add(portalLabel);
        JPanel p1 = new JPanel();
        p1.add(portalUserName, JPanel.CENTER_ALIGNMENT);
        configGrid.add(p1);
        // row 2
        fromBlockComboBox.addActionListener(e -> {
            if ((toBlockComboBox.getItemCount() > 0) && (fromBlockComboBox.getSelectedItem() != null) &&
                    (toBlockComboBox.getSelectedItem() != null) &&
                    (toBlockComboBox.getSelectedItem().equals(fromBlockComboBox.getSelectedItem()))) {
                log.debug("resetting ToBlock");
                toBlockComboBox.setSelectedIndex(0); // clear the other one
            }
        });
        configGrid.add(fromBlockLabel);
        configGrid.add(fromBlockComboBox);
        fromBlockComboBox.setAllowNull(true);
        // row 3
        toBlockComboBox.addActionListener(e -> {
            if ((toBlockComboBox.getItemCount() > 0) && (fromBlockComboBox.getSelectedItem() != null) &&
                    (toBlockComboBox.getSelectedItem() != null) &&
                    (toBlockComboBox.getSelectedItem().equals(fromBlockComboBox.getSelectedItem()))) {
                log.debug("resetting FromBlock");
                fromBlockComboBox.setSelectedIndex(0); // clear the other one
            }
        });
        configGrid.add(toBlockLabel);
        configGrid.add(toBlockComboBox);
        toBlockComboBox.setAllowNull(true);

        p.add(configGrid);
        p.add(Box.createVerticalGlue());

        p1 = new JPanel();
        p1.add(statusBar);
        p.add(p1);

        // put buttons at the bottom
        JPanel p2 = new JPanel();
        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> frame.dispose());
        JButton apply;
        p2.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener(this::applyPressed);
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
            frame.dispose();
        });
        p.add(p2, BorderLayout.SOUTH);

        //main.add(p);
        frame.getContentPane().add(p);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        portalUserName.setText(null);
        portalUserName.setBackground(Color.white);
        if (fromBlockComboBox.getItemCount() > 0) {
            fromBlockComboBox.setSelectedIndex(0);
            toBlockComboBox.setSelectedIndex(0); // uses same list so same check is OK
        }
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddPortalStatusEnter"));
        statusBar.setForeground(Color.gray);
        _newPortal = true;
    }

    /**
     * Populate the Edit Portal frame with current values.
     */
    public void populateFrame(Portal p) {
        if (p == null) {
            throw new IllegalArgumentException("Null OBlock object");
        }
        portalUserName.setText(p.getName());
        if (p.getFromBlockName() != null) {
            fromBlockComboBox.setSelectedItemByName(p.getFromBlockName());
        }
        if (p.getToBlockName() != null) {
            toBlockComboBox.setSelectedItemByName(p.getToBlockName());
        }
        _newPortal = false;
    }

    private void applyPressed(ActionEvent e) {
        String user = portalUserName.getText().trim();
        if (user.equals("")) {
            // warn/help bar red
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            portalUserName.setBackground(Color.red);
            return;
        } else {
            portalUserName.setBackground(Color.white);
        }
        if (_newPortal) {
            _portal = pm.createNewPortal(user);
            if (_portal == null) { // pm found an existing portal by the same name
                // warn/help bar red
                statusBar.setText(Bundle.getMessage("WarningSysNameInUse"));
                statusBar.setForeground(Color.red);
                portalUserName.setBackground(Color.red);
                return;
            } else {
                portalUserName.setBackground(Color.white);
            }
        } else {
            String msg = _portal.setName(user); // will check for duplicates
            if (msg != null ) {
                return;
            }
        }
        if (_portal != null) {
            try {
                OBlock block = fromBlockComboBox.getItemAt(fromBlockComboBox.getSelectedIndex());

                if (block != null) {
                    _portal.setFromBlock(block, true);
                }
                block = toBlockComboBox.getItemAt(toBlockComboBox.getSelectedIndex());
                if (block != null) {
                    _portal.setToBlock(block, true);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PortalCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);

            }
            // Notify changes
            model.fireTableDataChanged();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(PortalEditFrame.class);

}
