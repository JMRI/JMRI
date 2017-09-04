package jmri.jmrix.can.cbus.swing.configtool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.InstanceManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusMessage;

/**
 * Pane for user creation of Sensor, Turnouts and Lights (?) that are linked to CBUS
 * events.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class ConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    //static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle");

    static final int NRECORDERS = 6;
    CbusEventRecorder[] recorders = new CbusEventRecorder[NRECORDERS];

    public ConfigToolPane() {

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        for (int i = 0; i < recorders.length; i++) {
            recorders[i] = new CbusEventRecorder();
            p1.add(recorders[i]);
        }
        p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutEvents")));
        add(p1);

        // add sensor
        makeSensor = new MakeNamedBean("LabelEventActive", "LabelEventInactive") {

            @Override
            void create(String name) {
                if (memo != null) {
                    ((jmri.SensorManager) memo.get(jmri.SensorManager.class)).provideSensor("MS" + name);
                } else {
                    InstanceManager.sensorManagerInstance().provideSensor("MS" + name); // S for Sensor
                }
            }
        };
        makeSensor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleAddX", Bundle.getMessage("BeanNameSensor"))));
        add(makeSensor);

        // add turnout
        makeTurnout = new MakeNamedBean("LabelEventThrown", "LabelEventClosed") {
            @Override
            void create(String name) {
                if (memo != null) {
                    ((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class)).provideTurnout("MS" + name);
                } else {
                    InstanceManager.turnoutManagerInstance().provideTurnout("MT" + name); // T for Turnout
                }
            }
        };
        makeTurnout.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleAddX", Bundle.getMessage("BeanNameTurnout"))));
        add(makeTurnout);

    }

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
            return (memo.getUserName() + " " + Bundle.getMessage("ConfigTitle"));

        }
        return Bundle.getMessage("ConfigTitle");
    }

    MakeNamedBean makeSensor;
    MakeNamedBean makeTurnout;

    @Override
    public void reply(jmri.jmrix.can.CanReply m) {
        // forward to anybody waiting to capture
        makeSensor.reply(m);
        makeTurnout.reply(m);
        for (int i = 0; i < recorders.length; i++) {
            if (recorders[i].waiting()) {
                recorders[i].reply(m);
                break;
            }
        }
    }

    @Override
    public void message(jmri.jmrix.can.CanMessage m) {
        // forward to anybody waiting to capture
        makeSensor.message(m);
        makeTurnout.message(m);
        for (int i = 0; i < recorders.length; i++) {
            if (recorders[i].waiting()) {
                recorders[i].message(m);
                break;
            }
        }
    }

    @Override
    public void dispose() {
        // disconnect from the CBUS
        tc.removeCanListener(this);
    }

    /**
     * Class to build one NamedBean
     */
    class MakeNamedBean extends JPanel implements CanListener {

        JTextField f1 = new JTextField(20);
        JTextField f2 = new JTextField(20);

        JButton bc;

        JToggleButton b1 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));
        JToggleButton b2 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));

        /**
         * Create CBUS NamedBean using a JPanel for user interaction.
         *
         * @param name1 string for Label 1 in configuration pane
         * @param name2 string for Label 2 in configuration pane
         */
        MakeNamedBean(String name1, String name2) {
            // actions
            bc = new JButton(Bundle.getMessage("ButtonCreate"));
            bc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (f2.getText().equals("")) {
                        create(f1.getText());
                    } else {
                        create(f1.getText() + ";" + f2.getText());
                    }
                }
            });

            // GUI
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = 1;
            c.gridheight = 1;

            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(Bundle.getMessage(name1)), c);

            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(f1, c);

            c.gridx = 2;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(b1, c);
            b1.setToolTipText(Bundle.getMessage("CaptureNextTooltip"));

            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(Bundle.getMessage(name2)), c);

            c.gridx = 1;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(f2, c);

            c.gridx = 2;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            add(b2, c);
            b2.setToolTipText(Bundle.getMessage("CaptureNextTooltip"));

            c.gridx = 1;
            c.gridy = 2;
            c.anchor = GridBagConstraints.WEST;
            add(bc, c);
            bc.setToolTipText(Bundle.getMessage("CreateTooltip"));
        }

        void create(String name) {
        }

        @Override
        public void reply(jmri.jmrix.can.CanReply m) {
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
            }
        }

        @Override
        public void message(jmri.jmrix.can.CanMessage m) {
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
            }
        }
    }

    /**
     * Class to handle recording and presenting one event.
     */
    static class CbusEventRecorder extends JPanel implements CanListener {

        CbusEventRecorder() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(event);
            add(capture);

            event.setEditable(false);
            event.setDragEnabled(true);
            capture.setSelected(true);
        }

        JCheckBox capture = new JCheckBox(Bundle.getMessage("MsgCaptureNext"));
        JTextField event = new JTextField(20);

        boolean waiting() {
            return capture.isSelected();
        }

        @Override
        public void reply(jmri.jmrix.can.CanReply m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
            }
        }

        @Override
        public void message(jmri.jmrix.can.CanMessage m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
            }
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("ConfigTitle"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    ConfigToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

}
