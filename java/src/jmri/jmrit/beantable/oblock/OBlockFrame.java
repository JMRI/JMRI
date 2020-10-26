package jmri.jmrit.beantable.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.beantable.oblock.OBlockTableModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Defines a GUI for editing OBlocks, Portals and Path objects. Based on AudioSourceFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class OBlockFrame extends AbstractOBlockFrame {

    //private boolean newOBlock;

    //private final Object lock = new Object();

    // UI components for Add/Edit OBlock
    JLabel errorSensorLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ErrorSensorCol")));
    JComboBox<String> assignedSensor = new JComboBox<>();

    //private final static String PREFIX = "OB"; // IOB?

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public OBlockFrame(String title, OBlockTableModel model) {
        super(title, model);
        layoutFrame();
    }

    @Override
    public void layoutFrame() {
        super.layoutFrame();
        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(errorSensorLabel);
        p.add(assignedSensor);
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
    @Override
    public void resetFrame() {
        userName.setText(null);
        //assignedBuffer.setSelectedIndex(0);

        //this.newOBlock = true;
    }

    /**
     * Populate the Edit OBlock frame with current values.
     */
    @Override
    public void populateFrame(OBlock a) {
        if (a == null) {
            throw new IllegalArgumentException("Null OBlock object");
        }
        super.populateFrame(a);
//        OBlock s = a;
//        OBlockManager om = InstanceManager.getDefault(OBlockManager.class);
//        this.newOBlock = false;
    }

    private void applyPressed(ActionEvent e) {
        String sName = sysName.getText();
        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
        // Notify changes
        model.fireTableDataChanged();
    }

    //private static final Logger log = LoggerFactory.getLogger(AudioSourceFrame.class);

}
