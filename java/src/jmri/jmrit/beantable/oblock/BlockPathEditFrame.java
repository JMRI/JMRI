package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Defines a GUI for editing OBlocks - Portal objects in the tabbed Table interface.
 * Based on AudioSourceFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class BlockPathEditFrame extends JmriJFrame {

    JPanel main = new JPanel();
    private final JScrollPane scroll
            = new JScrollPane(main,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    PortalTableModel model;
    PortalManager pm;

    //private final Object lock = new Object();

    // UI components for Add/Edit Portal
    JLabel portalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNamePortal")));
    JTextField userName = new JTextField(15);
    JLabel fromBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromBlockName")));
    JLabel toBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OppBlockName")));
    private final NamedBeanComboBox<OBlock> fromBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<OBlock> toBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    JLabel statusBar = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);

    private final static String PREFIX = "OB";
    private final BlockPathEditFrame frame = this;
    private boolean _newPortal;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BlockPathEditFrame(String title, PortalTableModel model) {
        super(title);
        this.model = model;
        pm = InstanceManager.getDefault(PortalManager.class);
        layoutFrame();
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(portalLabel);
        p.add(userName);
        p.add(fromBlockLabel);
        p.add(fromBlockComboBox);
        p.add(toBlockLabel);
        p.add(toBlockComboBox);
        main.add(p);

        p = new JPanel();
        p.add(statusBar);
        JButton cancel;
        p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        JButton apply;
        p.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener(this::applyPressed);
        JButton ok;
        p.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
            frame.dispose();
        });
        frame.getContentPane().add(p);

        frame.add(scroll);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        userName.setText(null);
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
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
        userName.setText(p.getName());
        fromBlockComboBox.setSelectedItemByName(p.getFromBlockName());
        toBlockComboBox.setSelectedItemByName(p.getToBlockName());
        _newPortal = false;
    }

    private void applyPressed(ActionEvent e) {
        String user = userName.getText().trim();
        if (user.equals("")) {
            // warn/help bar red
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            userName.setBackground(Color.red);
            return;
        } else {
            userName.setBackground(Color.white);
        }
        Portal portal = pm.providePortal(user);
        if (portal != null) {
            //      try {
            OBlock block = fromBlockComboBox.getItemAt(fromBlockComboBox.getSelectedIndex());
            if (block != null) {
                portal.setFromBlock(block, true);
            }
            block = toBlockComboBox.getItemAt(toBlockComboBox.getSelectedIndex());
            if (block != null) {
                portal.setToBlock(block, true);
            //  } catch (IllegalArgumentException ex) {
            //     JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PortalCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
        // Notify changes
        model.fireTableDataChanged();
    }

    private static final Logger log = LoggerFactory.getLogger(BlockPathEditFrame.class);

}
