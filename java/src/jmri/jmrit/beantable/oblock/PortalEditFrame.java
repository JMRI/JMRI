package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.jmrit.beantable.AudioTableAction;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.JmriJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Defines a GUI for editing OBlocks, Portals and Path objects in the tabbed Table interface.
 * Based on AudioSourceFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class PortalEditFrame extends JmriJFrame {

    JPanel main = new JPanel();
    private JScrollPane scroll
            = new JScrollPane(main,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    PortalTableModel model;

    //private final Object lock = new Object();

    // UI components for Add/Edit Portal
    JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNamePortal")));
    JComboBox<String> assignedBlock = new JComboBox<>();
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(15);

    //private final static String PREFIX = "OB"; // IOB?
    PortalEditFrame frame = this;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public PortalEditFrame(String title, PortalTableModel model) {
        super(title);
        this.model= model;
        layoutFrame();
    }

    public void layoutFrame() {
        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(blockLabel);
        p.add(assignedBlock);
        main.add(p);

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
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        userName.setText(null);
        //this.newPortal = true;
    }

    /**
     * Populate the Edit Portal frame with current values.
     */
    public void populateFrame(Portal p) {
        if (p == null) {
            throw new IllegalArgumentException("Null OBlock object");
        }
        Portal s = p;
        PortalManager pm = InstanceManager.getDefault(PortalManager.class);
        userName.setText(p.getName());
        //this.newPortal = false;
    }

    private void applyPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
        // Notify changes
        model.fireTableDataChanged();
    }

    //private static final Logger log = LoggerFactory.getLogger(PortalFrame.class);

}
