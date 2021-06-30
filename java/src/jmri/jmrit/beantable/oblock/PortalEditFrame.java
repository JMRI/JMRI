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

    JPanel main = new JPanel();
    //private final JScrollPane scroll = new JScrollPane(main, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    PortalTableModel model;
    PortalManager pm;
    //private final Object lock = new Object();

    // UI components for Add/Edit Portal
    private final JLabel portalLabel = new JLabel(Bundle.getMessage("PortalNameLabel"), JLabel.TRAILING);
    private final JTextField portalUserName = new JTextField(15);
    private final JLabel fromBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromBlockName")), JLabel.TRAILING);
    private final JLabel toBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OppBlockName")), JLabel.TRAILING);
    private final NamedBeanComboBox<OBlock> fromBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<OBlock> toBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final JLabel statusBar = new JLabel(Bundle.getMessage("AddPortalStatusEnter"), JLabel.LEADING);

    private final PortalEditFrame frame = this;
    private Portal _portal;
    private boolean _newPortal = false;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public PortalEditFrame(@Nonnull String title, Portal portal, PortalTableModel model) {
        super(title, true, true);
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
        frame.setSize(425, 225);

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
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * portalUserName.getFont().getSize())); // a bit smaller
        statusBar.setForeground(Color.gray);
        p1.add(statusBar);
        p.add(p1);

        // put buttons at the bottom
        JPanel p2 = new JPanel();
        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> frame.dispose());
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
        });
        p.add(p2, BorderLayout.SOUTH);

        main.add(p);
        frame.getContentPane().add(main);
        frame.setEscapeKeyClosesWindow(true);
        frame.getRootPane().setDefaultButton(ok);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        portalUserName.setText(null);
        portalUserName.setBackground(Color.white);
        if (fromBlockComboBox.getItemCount() > 0) {
            fromBlockComboBox.setSelectedIndex(0);
            toBlockComboBox.setSelectedIndex(0); // the combos use the same list so 1 check is sufficient
        }
        // reset statusBar text
        if (fromBlockComboBox.getItemCount() < 2) {
            status(Bundle.getMessage("NotEnoughBlocks"), true);
        } else {
            status(Bundle.getMessage("AddPortalStatusEnter"), false);
        }
        _newPortal = true;
    }

    /**
     * Populate the Edit Portal frame with current values.
     *
     * @param p existing Portal to copy the attributes from
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
            status(Bundle.getMessage("WarningSysNameEmpty"), true);
            portalUserName.setBackground(Color.red);
            return;
        }
        portalUserName.setBackground(Color.white);
        status(Bundle.getMessage("AddPortalStatusEnter"), false);
        if (fromBlockComboBox.getSelectedIndex() == -1 || toBlockComboBox.getSelectedIndex() == -1) {
            status(Bundle.getMessage("PortalNeedsBlock", user), true);
            return;
        }
        if (_newPortal) {
            _portal = pm.createNewPortal(user);
            if (_portal == null) { // pm found an existing portal by the same name
                // warn/help bar red
                status(Bundle.getMessage("WarningSysNameInUse"), true);
                portalUserName.setBackground(Color.red);
                return;
            }
        } else {
            String msg = _portal.setName(user); // will check for duplicates
            if (msg != null) {
                status(msg, true);
                return;
            }
        }
        try {
            OBlock block = fromBlockComboBox.getSelectedItem();
            if (block != null) { // could have been deleted in JMRI by now?
                // SametoFromBlock is prevented between comboboxes
                if (!_portal.setFromBlock(block, false)) {
                    String msg = Bundle.getMessage("BlockPathsConflict", fromBlockComboBox.getSelectedItemDisplayName(), _portal.getFromBlockName());
                    int val = model.verifyWarning(msg);
                    if (val == 2) {
                        status(msg, true);
                        return;
                    } else {
                        status(Bundle.getMessage("AddPortalStatusEnter"), false);
                    }
                }
                _portal.setFromBlock(block, true);
            }
            block = toBlockComboBox.getSelectedItem();
            if (block != null) {
                if (!_portal.setToBlock(block, false)) {
                    String msg = Bundle.getMessage("BlockPathsConflict", fromBlockComboBox.getSelectedItemDisplayName(), _portal.getFromBlockName());
                    int val = model.verifyWarning(msg);
                    if (val == 2) {
                        status(msg, true);
                        return;
                    } else {
                        status(Bundle.getMessage("AddPortalStatusEnter"), false);
                    }
                }
                _portal.setToBlock(block, true);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PortalCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            status(Bundle.getMessage("AddPortalFailed", user), true);
            return;
        }
        // Notify changes
        model.fireTableDataChanged();
        dispose();
    }

    void status(String message, boolean warn){
        statusBar.setText(message);
        statusBar.setForeground(warn ? Color.red : Color.gray);
    }

    private static final Logger log = LoggerFactory.getLogger(PortalEditFrame.class);

}
