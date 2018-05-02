package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;

/**
 * Pane for setting node configuration.
 * <p>
 * No actions active in buttons, as of JMRI 4.8
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class NodeConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    JSpinner numberSpinner;
    JButton setNN;
    JSpinner varNumberSpinner;
    JSpinner varValueSpinner;
    JButton read;
    JButton write;

    TrafficController tc;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("Title"));
        }
        return Bundle.getMessage("Title");
    }

    public NodeConfigToolPane() {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p1;

        // get event number
        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(new JLabel(Bundle.getMessage("LabelNodeNumber")));

        numberSpinner = new JSpinner(new SpinnerNumberModel(256, 256, 1000000, 1));
        p1.add(numberSpinner);
        numberSpinner.setToolTipText(Bundle.getMessage("ToolTipNodeNumber"));

        setNN = new JButton(Bundle.getMessage("ButtonSet"));
        setNN.setToolTipText(Bundle.getMessage("ToolTipSetNodeNumber"));
        setNN.addActionListener((ActionEvent e1) -> {
            //not yet functional
            JOptionPane.showMessageDialog(null, Bundle.getMessage("NotYetDialogString", Bundle.getMessage("Title")),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        });
        p1.add(setNN);

        p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderNodeNumber")));
        add(p1);

        // set node variables
        p1 = new JPanel();
        p1.setLayout(new java.awt.GridLayout(3, 2));

        p1.add(new JLabel(Bundle.getMessage("LabelVariableNumber")));
        varNumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
        //varnumber = new JTextField(5);
        varNumberSpinner.setToolTipText(Bundle.getMessage("ToolTipVariableNumber"));
        p1.add(varNumberSpinner);

        p1.add(new JLabel(Bundle.getMessage("LabelVariableValue")));
        varValueSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        // varvalue = new JTextField(5);
        varValueSpinner.setToolTipText(Bundle.getMessage("ToolTipVariableValue"));
        p1.add(varValueSpinner);

        read = new JButton(Bundle.getMessage("ButtonRead"));
        read.setToolTipText(Bundle.getMessage("ToolTipRead"));
        read.addActionListener((ActionEvent e2) -> {
            //not yet functional
            JOptionPane.showMessageDialog(null, Bundle.getMessage("NotYetDialogString", Bundle.getMessage("Title")),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        });
        p1.add(read);
        write = new JButton(Bundle.getMessage("ButtonWrite"));
        write.setToolTipText(Bundle.getMessage("ToolTipWrite"));
        write.addActionListener((ActionEvent e3) -> {
            //not yet functional
            JOptionPane.showMessageDialog(null, Bundle.getMessage("NotYetDialogString", Bundle.getMessage("Title")),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        });
        p1.add(write);

        p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderNodeVariables")));
        add(p1);

    }

    @Override
    public void reply(jmri.jmrix.can.CanReply m) {
    }

    @Override
    public void message(jmri.jmrix.can.CanMessage m) {
    }

    @Override
    public void dispose() {
        // disconnect from the CBUS
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("Title"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NodeConfigToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

}
